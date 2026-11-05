package com.mss301.watch.repository;

import com.mss301.watch.entity.WatchCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchCategoryRepository extends JpaRepository<WatchCategory, Integer> {

    List<WatchCategory> findAllByOrderByCategoryIdAsc();
}
