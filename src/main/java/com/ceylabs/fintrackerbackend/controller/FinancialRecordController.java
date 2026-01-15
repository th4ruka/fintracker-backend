package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordCreateRequest;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordUpdateRequest;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.FinancialRecord;
import com.ceylabs.fintrackerbackend.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

    @Autowired
    private FinancialRecordService financialRecordService;

    // Create a new financial record
    @PostMapping
    public ResponseEntity<FinancialRecordResponse> createRecord(@Valid @RequestBody FinancialRecordCreateRequest request) {
        FinancialRecordResponse response = financialRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all records by user with optional filters
    @GetMapping
    public ResponseEntity<List<FinancialRecordResponse>> getRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) RecordType recordType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

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

    // Get a specific record by ID
    @GetMapping("/{id}")
    public ResponseEntity<FinancialRecordResponse> getRecordById(@PathVariable Long id) {
        Optional<FinancialRecord> record = financialRecordService.getRecordById(id);
        return record.map(financialRecordService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a financial record
    @PutMapping("/{id}")
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordUpdateRequest request) {
        FinancialRecordResponse response = financialRecordService.updateRecord(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete a financial record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        financialRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // Get records by user and account
    @GetMapping("/user/{userId}/account/{accountId}")
    public ResponseEntity<List<FinancialRecordResponse>> getRecordsByUserAndAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        List<FinancialRecordResponse> records = financialRecordService.getRecordsByAccount(accountId);
        return ResponseEntity.ok(records);
    }

    // Get records by date range
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<FinancialRecordResponse>> getRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FinancialRecordResponse> records = financialRecordService.getRecordsByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(records);
    }
}
