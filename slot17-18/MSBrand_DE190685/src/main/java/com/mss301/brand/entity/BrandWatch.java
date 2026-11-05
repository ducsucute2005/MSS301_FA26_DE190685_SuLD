package com.mss301.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "BrandWatches",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_BrandWatches_WatchID",
                columnNames = "WatchID"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "WatchID", nullable = false)
    private Integer watchId;

    @Column(name = "BrandID", nullable = false)
    private Integer brandId;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
