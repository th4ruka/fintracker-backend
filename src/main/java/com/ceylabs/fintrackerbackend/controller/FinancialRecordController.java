package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordCreateRequest;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordUpdateRequest;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.FinancialRecord;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

    @Autowired
    private FinancialRecordService financialRecordService;

    /**
     * Create a new financial record for the authenticated user
     */
    @PostMapping
    public ResponseEntity<FinancialRecordResponse> createRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FinancialRecordCreateRequest request) {
        // Override userId from request with authenticated user's ID for security
        request.setUserId(userDetails.getId());
        FinancialRecordResponse response = financialRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all records for the authenticated user with optional filters
     */
    @GetMapping
    public ResponseEntity<List<FinancialRecordResponse>> getRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) RecordType recordType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userDetails.getId();
        List<FinancialRecordResponse> records;

        // Apply filters based on provided parameters
        if (startDate != null && endDate != null) {
            records = financialRecordService.getRecordsByUserAndDateRange(userId, startDate, endDate);
        } else if (accountId != null) {
            records = financialRecordService.getRecordsByAccount(accountId);
        } else if (categoryId != null) {
            records = financialRecordService.getRecordsByCategory(categoryId);
        } else if (recordType != null) {
            records = financialRecordService.getRecordsByType(userId, recordType);
        } else {
            records = financialRecordService.getRecordsByUser(userId);
        }

        return ResponseEntity.ok(records);
    }

    /**
     * Get a specific record by ID (with ownership verification)
     */
    @GetMapping("/{id}")
    public ResponseEntity<FinancialRecordResponse> getRecordById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<FinancialRecord> record = financialRecordService.getRecordById(id);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!record.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(financialRecordService.mapToResponse(record.get()));
    }

    /**
     * Update a financial record (with ownership verification)
     */
    @PutMapping("/{id}")
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordUpdateRequest request) {
        Optional<FinancialRecord> record = financialRecordService.getRecordById(id);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!record.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FinancialRecordResponse response = financialRecordService.updateRecord(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a financial record (with ownership verification)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<FinancialRecord> record = financialRecordService.getRecordById(id);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!record.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        financialRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get records by account for the authenticated user
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<FinancialRecordResponse>> getRecordsByAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accountId) {
        List<FinancialRecordResponse> records = financialRecordService.getRecordsByAccount(accountId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get records by date range for the authenticated user
     */
    @GetMapping("/range")
    public ResponseEntity<List<FinancialRecordResponse>> getRecordsByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FinancialRecordResponse> records = financialRecordService.getRecordsByUserAndDateRange(
                userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(records);
    }
}
