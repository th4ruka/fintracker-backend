package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.TemplateCreateRequest;
import com.ceylabs.fintrackerbackend.dto.TemplateResponse;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.Template;
import com.ceylabs.fintrackerbackend.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    // Create a new template
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all templates by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TemplateResponse>> getTemplatesByUser(@PathVariable Long userId) {
        List<TemplateResponse> templates = templateService.getTemplatesByUser(userId);
        return ResponseEntity.ok(templates);
    }

    // Get a specific template by ID
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);
        return template.map(templateService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a template
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete a template
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // Get templates by user and record type
    @GetMapping("/user/{userId}/type/{recordType}")
    public ResponseEntity<List<TemplateResponse>> getTemplatesByUserAndType(
            @PathVariable Long userId,
            @PathVariable RecordType recordType) {
        List<TemplateResponse> templates = templateService.getTemplatesByUserAndType(userId, recordType);
        return ResponseEntity.ok(templates);
    }

    // Search templates by name
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<TemplateResponse>> searchTemplates(
            @PathVariable Long userId,
            @RequestParam String query) {
        List<TemplateResponse> templates = templateService.searchTemplatesByName(userId, query);
        return ResponseEntity.ok(templates);
    }

    // Get template usage count
    @GetMapping("/{id}/usage-count")
    public ResponseEntity<Long> getTemplateUsageCount(@PathVariable Long id) {
        Long usageCount = templateService.getTemplateUsageCount(id);
        return ResponseEntity.ok(usageCount);
    }

    // Create a financial record from a template
    @PostMapping("/{id}/use")
    public ResponseEntity<FinancialRecordResponse> createRecordFromTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long toAccountId) {
        FinancialRecordResponse response = templateService.createRecordFromTemplate(
                id, amount, recordDate, accountId, toAccountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
