package com.mss301.account.entity;

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
@Table(name = "StoreAccounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountId;

    @Column(name = "Username", nullable = false, length = 20)
    private String username;

    @Column(name = "Email", nullable = false, length = 100)
    private String email;

    @Column(name = "Password", nullable = false, length = 100)
    private String password;

    @Column(name = "Role")
    private Integer role;

    @Column(name = "IsActive", columnDefinition = "BIT DEFAULT 1")
    @Builder.Default
    private Boolean isActive = true;
}
