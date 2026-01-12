package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.LabelCreateRequest;
import com.ceylabs.fintrackerbackend.dto.LabelResponse;
import com.ceylabs.fintrackerbackend.model.Label;
import com.ceylabs.fintrackerbackend.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    // Create a new label
    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(@Valid @RequestBody LabelCreateRequest request) {
        LabelResponse response = labelService.createLabel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all labels by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LabelResponse>> getLabelsByUser(@PathVariable Long userId) {
        List<LabelResponse> labels = labelService.getLabelsByUser(userId);
        return ResponseEntity.ok(labels);
    }

    // Get a specific label by ID
    @GetMapping("/{id}")
    public ResponseEntity<LabelResponse> getLabelById(@PathVariable Long id) {
        Optional<Label> label = labelService.getLabelById(id);
        return label.map(labelService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a label
    @PutMapping("/{id}")
    public ResponseEntity<LabelResponse> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody LabelCreateRequest request) {
        LabelResponse response = labelService.updateLabel(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete a label
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }

    // Get used labels by user (labels attached to records)
    @GetMapping("/user/{userId}/used")
    public ResponseEntity<List<LabelResponse>> getUsedLabels(@PathVariable Long userId) {
        List<LabelResponse> labels = labelService.getUsedLabelsByUser(userId);
        return ResponseEntity.ok(labels);
    }

    // Get unused labels by user (labels not attached to any record)
    @GetMapping("/user/{userId}/unused")
    public ResponseEntity<List<LabelResponse>> getUnusedLabels(@PathVariable Long userId) {
        List<LabelResponse> labels = labelService.getUnusedLabelsByUser(userId);
        return ResponseEntity.ok(labels);
    }

    // Search labels by name
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<LabelResponse>> searchLabels(
            @PathVariable Long userId,
            @RequestParam String query) {
        List<LabelResponse> labels = labelService.searchLabelsByName(userId, query);
        return ResponseEntity.ok(labels);
    }

    // Get labels by color
    @GetMapping("/user/{userId}/color")
    public ResponseEntity<List<LabelResponse>> getLabelsByColor(
            @PathVariable Long userId,
            @RequestParam String color) {
        List<LabelResponse> labels = labelService.getLabelsByColor(userId, color);
        return ResponseEntity.ok(labels);
    }
}
