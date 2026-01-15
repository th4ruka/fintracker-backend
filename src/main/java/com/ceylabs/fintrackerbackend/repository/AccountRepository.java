package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    // Find by account type
    List<Account> findByAccountType(AccountType accountType);

    // Find by user and account type
    List<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);

    // Find accounts included in statistics
    List<Account> findByExcludeFromStatisticsFalse();

    // Find by user excluded from statistics
    List<Account> findByUserIdAndExcludeFromStatisticsFalse(Long userId);
}
