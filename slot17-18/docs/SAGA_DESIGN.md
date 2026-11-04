# Saga Design - Watch Management

## Participants and ownership

| Service | Role | Database | Saga-owned data |
|---|---|---|---|
| MSWatch | Orchestrator | DBWatch | `Watches`, `SagaStates`, `SagaLogs` |
| MSBrand | Participant | DBBrand | `BrandWatches` |

Each service changes only its own database. MSWatch coordinates the workflow and
calls MSBrand's internal API for brand assignments.

## CREATE_WATCH

| # | Local transaction | Service / database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `CREATE_WATCH`: insert the watch and commit | MSWatch / DBWatch | `DELETE_WATCH`: delete the newly created watch if it exists | Yes |
| 2 | `ASSIGN_BRAND`: upsert the watch-to-brand assignment and commit | MSBrand / DBBrand | `UNASSIGN_BRAND`: remove the assignment if it exists | Yes |

The watch insert commits before MSBrand is called. If assigning the brand fails,
the failed assignment step has no compensation; the orchestrator compensates the
committed `CREATE_WATCH` step by deleting the created watch.

## UPDATE_WATCH

| # | Local transaction | Service / database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `UPDATE_WATCH`: save the new watch values and commit | MSWatch / DBWatch | `RESTORE_WATCH`: write back the saved `before` snapshot | Yes |
| 2 | `ASSIGN_BRAND`: upsert the assignment to the requested brand and commit | MSBrand / DBBrand | `ASSIGN_BRAND_BACK`: upsert the saved previous brand assignment | Yes |

The orchestrator captures the previous watch values and brand before starting
the update. If the assignment committed and a later error occurs, it restores
the previous brand assignment first, then restores the watch snapshot. If the
assignment itself fails before commit, only the committed watch update is
compensated.

## DELETE_WATCH

| # | Local transaction | Service / database | Compensation | Idempotent |
|---|---|---|---|---|
| 1 | `UNASSIGN_BRAND`: remove the watch's brand assignment and commit | MSBrand / DBBrand | `ASSIGN_BRAND_BACK`: upsert the saved previous brand assignment | Yes |
| 2 | `DELETE_WATCH`: hard-delete the watch and commit | MSWatch / DBWatch | None - **PIVOT** | N/A |

The network operation is first, while it can still be compensated. Hard deletion
is last because the generated identity makes it impossible to restore the
deleted row with its original `WatchID` reliably. Once the pivot commits, later
fault injection is skipped and the Saga remains `COMPLETED`.

## State machine

```text
PENDING -> EXECUTING -> COMPLETED
              |
              +-> FAILED -> COMPENSATING -> COMPENSATED
                                  |
                                  +-> COMPENSATING (manual intervention required)
```

- `PENDING`: Saga record created, no business step started.
- `EXECUTING`: local transactions and participant calls are being run in order.
- `COMPLETED`: every required step, including the pivot if present, committed.
- `FAILED`: a step failed before the pivot; the orchestrator is about to
  compensate completed steps.
- `COMPENSATING`: compensations are running in reverse order. If a
  compensation still fails after three attempts, remain in this state for
  manual intervention.
- `COMPENSATED`: all completed, compensatable steps were successfully undone.

Only a step confirmed committed is eligible for compensation. Compensation runs
in LIFO order and is retried up to three times. Each retry must be safe to
repeat. The orchestrator is not wrapped in one `@Transactional` transaction;
each database operation is its own local transaction, and Saga state/log writes
use their own transaction.

## Failure example: create a watch with brand 999

```text
MSWatch / DBWatch                 MSBrand / DBBrand
       |                                  |
       | CREATE_WATCH: INSERT (id=11)     |
       | COMMIT                           |
       |                                  |
       | ASSIGN_BRAND PUT /brands/999/... |
       |--------------------------------->| 404 Brand not found
       |                                  |
       | FAILED -> COMPENSATING           |
       | DELETE_WATCH id=11               |
       | COMMIT                           |
       |                                  |
       | COMPENSATED                      |
       +-- HTTP 400, failedStep=ASSIGN_BRAND
```

The insert was committed before the participant returned 404; this is a
compensating transaction, not a database rollback. The deleted identity is not
reused, so the next successful watch may receive ID 12.

## Transaction choice

`@Transactional` on MSWatch can control DBWatch's local transaction, but cannot
roll back an HTTP request that MSBrand has already committed to DBBrand.
Wrapping the orchestrator in one transaction would also hide the committed
steps that the Saga must compensate.

Two-phase commit would require both services and their databases to participate
in a distributed coordinator and hold resources across both phases. MSBrand is
accessed over REST and does not participate in XA; a coordinator also adds
availability and recovery concerns. The Saga instead uses short local
transactions, accepts temporary inconsistency, and restores consistency through
explicit, idempotent compensation.
