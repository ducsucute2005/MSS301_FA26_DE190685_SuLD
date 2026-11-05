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
@Table(name = "SagaLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SagaId", nullable = false, length = 36)
    private String sagaId;

    @Column(name = "StepNo")
    private Integer stepNo;

    @Column(name = "StepName", length = 50)
    private String stepName;

    @Column(name = "Phase", length = 20)
    private String phase;

    @Column(name = "Result", length = 20)
    private String result;

    @Column(name = "Message", length = 1000)
    private String message;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}
