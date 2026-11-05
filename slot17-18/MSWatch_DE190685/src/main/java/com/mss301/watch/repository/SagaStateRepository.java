package com.mss301.watch.repository;

import com.mss301.watch.entity.SagaState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaStateRepository extends JpaRepository<SagaState, Long> {

    Optional<SagaState> findBySagaId(String sagaId);

    Page<SagaState> findAllByOrderByIdDesc(Pageable pageable);
}
