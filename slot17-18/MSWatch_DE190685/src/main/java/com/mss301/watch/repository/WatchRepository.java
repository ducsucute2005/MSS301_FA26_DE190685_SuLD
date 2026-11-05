package com.mss301.watch.repository;

import com.mss301.watch.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchRepository extends JpaRepository<Watch, Integer> {
}
