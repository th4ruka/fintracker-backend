package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    // Find by user
    List<Template> findByUserId(Long userId);

    // Find by record type
    List<Template> findByRecordType(RecordType recordType);

    // Find by user and record type
    List<Template> findByUserIdAndRecordType(Long userId, RecordType recordType);

    // Find by category
    List<Template> findByCategoryId(Long categoryId);

    // Find by user and category
    List<Template> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // Find templates without category
    List<Template> findByCategoryIsNull();

    // Find templates without category by user
    List<Template> findByUserIdAndCategoryIsNull(Long userId);

    // Find by account
    List<Template> findByAccountId(Long accountId);

    // Find by user and account
    List<Template> findByUserIdAndAccountId(Long userId, Long accountId);

    // Find transfer templates
    List<Template> findByRecordTypeAndToAccountIsNotNull(RecordType recordType);

    // Find transfer templates by user
    List<Template> findByUserIdAndRecordTypeAndToAccountIsNotNull(Long userId, RecordType recordType);

    // Find template by name and user (for duplicate checking)
    Optional<Template> findByNameAndUserId(String name, Long userId);

    // Check if template name exists for user
    boolean existsByNameAndUserId(String name, Long userId);

    // Find templates by name containing (search)
    List<Template> findByNameContainingIgnoreCase(String name);

    // Find templates by name containing and user
    List<Template> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // Get expense templates by user
    @Query("SELECT t FROM Template t WHERE t.user.id = :userId AND t.recordType = 'EXPENSE'")
    List<Template> findExpenseTemplatesByUser(@Param("userId") Long userId);

    // Get income templates by user
    @Query("SELECT t FROM Template t WHERE t.user.id = :userId AND t.recordType = 'INCOME'")
    List<Template> findIncomeTemplatesByUser(@Param("userId") Long userId);

    // Get transfer templates by user
    @Query("SELECT t FROM Template t WHERE t.user.id = :userId AND t.recordType = 'TRANSFER'")
    List<Template> findTransferTemplatesByUser(@Param("userId") Long userId);

    // Find templates by payment type
    @Query("SELECT t FROM Template t WHERE t.user.id = :userId AND t.paymentType = :paymentType")
    List<Template> findByUserIdAndPaymentType(
            @Param("userId") Long userId,
            @Param("paymentType") com.ceylabs.fintrackerbackend.enums.PaymentType paymentType);

    // Get most recently created templates by user
    @Query("SELECT t FROM Template t WHERE t.user.id = :userId ORDER BY t.createdDate DESC")
    List<Template> findRecentTemplatesByUser(@Param("userId") Long userId);

    // Get most used templates (templates that have been used to create records)
    @Query("SELECT t.templateId, COUNT(fr) as usage_count FROM FinancialRecord fr WHERE fr.templateId IS NOT NULL AND fr.user.id = :userId GROUP BY fr.templateId ORDER BY usage_count DESC")
    List<Object[]> findMostUsedTemplatesByUser(@Param("userId") Long userId);

    // Count templates by user
    Long countByUserId(Long userId);

    // Count templates by user and record type
    Long countByUserIdAndRecordType(Long userId, RecordType recordType);

    // Count how many times a template has been used
    @Query("SELECT COUNT(fr) FROM FinancialRecord fr WHERE fr.templateId = :templateId")
    Long countTemplateUsage(@Param("templateId") Long templateId);
}
