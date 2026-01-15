package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.LabelCreateRequest;
import com.ceylabs.fintrackerbackend.dto.LabelResponse;
import com.ceylabs.fintrackerbackend.model.Label;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserService userService;

    // Create a new label
    @Transactional
    public LabelResponse createLabel(LabelCreateRequest request) {
        // Validate user exists
        User user = userService.getUserById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User with ID " + request.getUserId() + " does not exist"));

        // Check for duplicate label name for this user
        if (labelRepository.existsByNameAndUserId(request.getName(), request.getUserId())) {
            throw new IllegalStateException("Label with name '" + request.getName() + "' already exists for this user");
        }

        // Create label entity
        Label label = new Label();
        label.setUser(user);
        label.setName(request.getName());
        label.setColor(request.getColor());

        // Save label
        Label savedLabel = labelRepository.save(label);

        return mapToResponse(savedLabel);
    }

    // Update an existing label
    @Transactional
    public LabelResponse updateLabel(Long labelId, LabelCreateRequest request) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new IllegalStateException("Label with ID " + labelId + " does not exist"));

        // Validate label belongs to user
        if (!label.getUser().getId().equals(request.getUserId())) {
            throw new IllegalStateException("Label does not belong to the specified user");
        }

        // Check for duplicate name (excluding current label)
        Optional<Label> existingLabel = labelRepository.findByNameAndUserId(request.getName(), request.getUserId());
        if (existingLabel.isPresent() && !existingLabel.get().getId().equals(labelId)) {
            throw new IllegalStateException("Label with name '" + request.getName() + "' already exists for this user");
        }

        // Update fields
        label.setName(request.getName());
        label.setColor(request.getColor());

        // Save updated label
        Label updatedLabel = labelRepository.save(label);

        return mapToResponse(updatedLabel);
    }

    // Delete a label
    @Transactional
    public void deleteLabel(Long labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new IllegalStateException("Label with ID " + labelId + " does not exist"));

        // Note: The label will be removed from all associated financial records
        // This is handled by the ManyToMany relationship cascade settings

        labelRepository.delete(label);
    }

    // Get label by ID
    public Optional<Label> getLabelById(Long labelId) {
        return labelRepository.findById(labelId);
    }

    // Get all labels by user
    public List<LabelResponse> getLabelsByUser(Long userId) {
        return labelRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get used labels by user
    public List<LabelResponse> getUsedLabelsByUser(Long userId) {
        return labelRepository.findUsedLabelsByUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get unused labels by user
    public List<LabelResponse> getUnusedLabelsByUser(Long userId) {
        return labelRepository.findUnusedLabelsByUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Search labels by name
    public List<LabelResponse> searchLabelsByName(Long userId, String searchTerm) {
        return labelRepository.findByUserIdAndNameContainingIgnoreCase(userId, searchTerm).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get labels by color
    public List<LabelResponse> getLabelsByColor(Long userId, String color) {
        return labelRepository.findByUserIdAndColor(userId, color).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method: Map entity to response DTO
    public LabelResponse mapToResponse(Label label) {
        LabelResponse response = new LabelResponse();
        response.setId(label.getId());
        response.setUserId(label.getUser().getId());
        response.setName(label.getName());
        response.setColor(label.getColor());
        response.setCreatedDate(label.getCreatedDate());
        return response;
    }
}
