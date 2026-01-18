package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.TemplateCreateRequest;
import com.ceylabs.fintrackerbackend.dto.TemplateResponse;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.Template;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * Create a new template for the authenticated user
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TemplateCreateRequest request) {
        // Override userId from request with authenticated user's ID for security
        request.setUserId(userDetails.getId());
        TemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all templates for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getTemplates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TemplateResponse> templates = templateService.getTemplatesByUser(userDetails.getId());
        return ResponseEntity.ok(templates);
    }

    /**
     * Get a specific template by ID (with ownership verification)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);

        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!template.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(templateService.mapToResponse(template.get()));
    }

    /**
     * Update a template (with ownership verification)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TemplateCreateRequest request) {
        Optional<Template> template = templateService.getTemplateById(id);

        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!template.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a template (with ownership verification)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);

        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!template.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get templates by record type for the authenticated user
     */
    @GetMapping("/type/{recordType}")
    public ResponseEntity<List<TemplateResponse>> getTemplatesByType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable RecordType recordType) {
        List<TemplateResponse> templates = templateService.getTemplatesByUserAndType(
                userDetails.getId(), recordType);
        return ResponseEntity.ok(templates);
    }

    /**
     * Search templates by name for the authenticated user
     */
    @GetMapping("/search")
    public ResponseEntity<List<TemplateResponse>> searchTemplates(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String query) {
        List<TemplateResponse> templates = templateService.searchTemplatesByName(userDetails.getId(), query);
        return ResponseEntity.ok(templates);
    }

    /**
     * Get template usage count (with ownership verification)
     */
    @GetMapping("/{id}/usage-count")
    public ResponseEntity<Long> getTemplateUsageCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);

        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!template.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long usageCount = templateService.getTemplateUsageCount(id);
        return ResponseEntity.ok(usageCount);
    }

    /**
     * Create a financial record from a template (with ownership verification)
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<FinancialRecordResponse> createRecordFromTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long toAccountId) {
        Optional<Template> template = templateService.getTemplateById(id);

        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!template.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FinancialRecordResponse response = templateService.createRecordFromTemplate(
                id, amount, recordDate, accountId, toAccountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
