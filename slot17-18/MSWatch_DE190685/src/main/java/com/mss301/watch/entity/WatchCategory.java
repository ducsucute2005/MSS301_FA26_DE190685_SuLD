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

@Entity
@Table(name = "WatchCategories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer categoryId;

    @Column(name = "CategoryName", length = 100)
    private String categoryName;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "MovementType", length = 50)
    private String movementType;

    @Column(name = "PriceRange", length = 100)
    private String priceRange;
}
