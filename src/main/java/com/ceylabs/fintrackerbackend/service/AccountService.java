package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.model.Account;
import com.ceylabs.fintrackerbackend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    private UserService userService;

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account createAccount(String name, BigDecimal balance, Long userId) {
        Account account = new Account();
        account.setName(name);
        account.setBalance(balance);
        // Set the user based on the userId (Assume UserService exists to fetch user by ID)
        account.setUser(userService.getUserById(userId));
        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}

