package com.mss301.account.repository;

import com.mss301.account.entity.StoreAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<StoreAccount, Integer> {

    Optional<StoreAccount> findByUsername(String username);
}
