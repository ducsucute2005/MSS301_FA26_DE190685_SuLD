package com.mss301.watch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SagaStates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SagaId", nullable = false, unique = true, length = 36)
    private String sagaId;

    @Column(name = "SagaType", length = 20)
    private String sagaType;

    @Column(name = "WatchID")
    private Integer watchId;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "CurrentStep", length = 50)
    private String currentStep;

    @Column(name = "Payload", columnDefinition = "NVARCHAR(MAX)")
    private String payload;

    @Column(name = "FailureReason", length = 1000)
    private String failureReason;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
