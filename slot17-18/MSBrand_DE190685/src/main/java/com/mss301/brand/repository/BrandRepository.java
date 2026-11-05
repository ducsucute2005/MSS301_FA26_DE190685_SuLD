package com.mss301.brand.repository;

import com.mss301.brand.entity.WatchBrand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<WatchBrand, Integer> {
}
