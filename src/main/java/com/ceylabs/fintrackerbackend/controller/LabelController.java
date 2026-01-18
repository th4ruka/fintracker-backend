package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.LabelCreateRequest;
import com.ceylabs.fintrackerbackend.dto.LabelResponse;
import com.ceylabs.fintrackerbackend.model.Label;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    /**
     * Create a new label for the authenticated user
     */
    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LabelCreateRequest request) {
        // Override userId from request with authenticated user's ID for security
        request.setUserId(userDetails.getId());
        LabelResponse response = labelService.createLabel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all labels for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<LabelResponse>> getLabels(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LabelResponse> labels = labelService.getLabelsByUser(userDetails.getId());
        return ResponseEntity.ok(labels);
    }

    /**
     * Get a specific label by ID (with ownership verification)
     */
    @GetMapping("/{id}")
    public ResponseEntity<LabelResponse> getLabelById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Label> label = labelService.getLabelById(id);

        if (label.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!label.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(labelService.mapToResponse(label.get()));
    }

    /**
     * Update a label (with ownership verification)
     */
    @PutMapping("/{id}")
    public ResponseEntity<LabelResponse> updateLabel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LabelCreateRequest request) {
        Optional<Label> label = labelService.getLabelById(id);

        if (label.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!label.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LabelResponse response = labelService.updateLabel(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a label (with ownership verification)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Label> label = labelService.getLabelById(id);

        if (label.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        if (!label.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get used labels for the authenticated user (labels attached to records)
     */
    @GetMapping("/used")
    public ResponseEntity<List<LabelResponse>> getUsedLabels(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LabelResponse> labels = labelService.getUsedLabelsByUser(userDetails.getId());
        return ResponseEntity.ok(labels);
    }

    /**
     * Get unused labels for the authenticated user (labels not attached to any record)
     */
    @GetMapping("/unused")
    public ResponseEntity<List<LabelResponse>> getUnusedLabels(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LabelResponse> labels = labelService.getUnusedLabelsByUser(userDetails.getId());
        return ResponseEntity.ok(labels);
    }

    /**
     * Search labels by name for the authenticated user
     */
    @GetMapping("/search")
    public ResponseEntity<List<LabelResponse>> searchLabels(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String query) {
        List<LabelResponse> labels = labelService.searchLabelsByName(userDetails.getId(), query);
        return ResponseEntity.ok(labels);
    }

    /**
     * Get labels by color for the authenticated user
     */
    @GetMapping("/color")
    public ResponseEntity<List<LabelResponse>> getLabelsByColor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String color) {
        List<LabelResponse> labels = labelService.getLabelsByColor(userDetails.getId(), color);
        return ResponseEntity.ok(labels);
    }
}
