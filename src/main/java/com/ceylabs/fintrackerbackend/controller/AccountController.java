package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.AccountCreateRequest;
import com.ceylabs.fintrackerbackend.dto.AccountResponse;
import com.ceylabs.fintrackerbackend.dto.AccountUpdateRequest;
import com.ceylabs.fintrackerbackend.model.Account;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Get all accounts for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getCurrentUserAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccountsByUser(userDetails.getId()));
    }

    /**
     * Create a new account for the authenticated user
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountCreateRequest request) {
        // Override userId from request with authenticated user's ID for security
        request.setUserId(userDetails.getId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get account by ID (with ownership verification)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Account> account = accountService.getAccountById(id);

        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!account.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(accountService.mapEntityToResponse(account.get()));
    }

    /**
     * Update account (with ownership verification)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {
        Optional<Account> account = accountService.getAccountById(id);

        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!account.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete account (with ownership verification)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Account> account = accountService.getAccountById(id);

        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!account.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
