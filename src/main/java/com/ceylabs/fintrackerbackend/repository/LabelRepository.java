package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    // Find by user
    List<Label> findByUserId(Long userId);

    // Find label by name and user (for duplicate checking)
    Optional<Label> findByNameAndUserId(String name, Long userId);

    // Check if label name exists for user
    boolean existsByNameAndUserId(String name, Long userId);

    // Find labels by name containing (search)
    List<Label> findByNameContainingIgnoreCase(String name);

    // Find labels by name containing and user
    List<Label> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // Find labels by color
    List<Label> findByColor(String color);

    // Find labels by user and color
    List<Label> findByUserIdAndColor(Long userId, String color);

    // Get labels used in financial records (labels that are actually being used)
    @Query("SELECT DISTINCT l FROM FinancialRecord fr JOIN fr.labels l WHERE l.user.id = :userId")
    List<Label> findUsedLabelsByUser(@Param("userId") Long userId);

    // Get unused labels (labels not attached to any record)
    @Query("SELECT l FROM Label l WHERE l.user.id = :userId AND l.id NOT IN (SELECT DISTINCT lbl.id FROM FinancialRecord fr JOIN fr.labels lbl)")
    List<Label> findUnusedLabelsByUser(@Param("userId") Long userId);

    // Count labels by user
    Long countByUserId(Long userId);

    // Count used labels by user
    @Query("SELECT COUNT(DISTINCT l) FROM FinancialRecord fr JOIN fr.labels l WHERE l.user.id = :userId")
    Long countUsedLabelsByUser(@Param("userId") Long userId);
}
