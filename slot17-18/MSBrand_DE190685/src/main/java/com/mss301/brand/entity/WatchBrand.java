package com.mss301.brand.entity;

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

@Entity
@Table(name = "WatchBrands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "BrandName", length = 200)
    private String brandName;

    @Column(name = "CountryOfOrigin", length = 50)
    private String countryOfOrigin;
}
