package com.mss301.watch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Watches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WatchID")
    private Integer watchId;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CategoryID")
    private WatchCategory category;

    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "CaseMaterial", length = 100)
    private String caseMaterial;

    @Column(name = "StrapMaterial", length = 100)
    private String strapMaterial;

    @Column(name = "WaterpMaterial", length = 50)
    private String waterpMaterial;

    @Column(name = "Price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "ReleaseDate")
    private LocalDate releaseDate;

    @Column(name = "Stock")
    private Integer stock;
}
