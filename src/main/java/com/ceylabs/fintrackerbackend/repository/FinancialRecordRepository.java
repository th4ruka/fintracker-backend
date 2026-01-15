package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.enums.PaymentStatus;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find by user
    List<FinancialRecord> findByUserId(Long userId);

    // Find by account
    List<FinancialRecord> findByAccountId(Long accountId);

    // Find by user and account
    List<FinancialRecord> findByUserIdAndAccountId(Long userId, Long accountId);

    // Find by record type
    List<FinancialRecord> findByRecordType(RecordType recordType);

    // Find by user and record type
    List<FinancialRecord> findByUserIdAndRecordType(Long userId, RecordType recordType);

    // Find by category
    List<FinancialRecord> findByCategoryId(Long categoryId);

    // Find by user and category
    List<FinancialRecord> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // Find by payment status
    List<FinancialRecord> findByPaymentStatus(PaymentStatus paymentStatus);

    // Find by user and payment status
    List<FinancialRecord> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus);

    // Find by date range
    List<FinancialRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by user and date range
    List<FinancialRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // Find by user, account and date range
    List<FinancialRecord> findByUserIdAndAccountIdAndRecordDateBetween(
            Long userId, Long accountId, LocalDate startDate, LocalDate endDate);

    // Find by user, record type and date range
    List<FinancialRecord> findByUserIdAndRecordTypeAndRecordDateBetween(
            Long userId, RecordType recordType, LocalDate startDate, LocalDate endDate);

    // Find uncategorized records
    List<FinancialRecord> findByCategoryIsNull();

    // Find uncategorized records by user
    List<FinancialRecord> findByUserIdAndCategoryIsNull(Long userId);

    // Find records created from template
    List<FinancialRecord> findByCreatedFromTemplateTrue();

    // Find records created from specific template
    List<FinancialRecord> findByTemplateId(Long templateId);

    // Find records by user created from template
    List<FinancialRecord> findByUserIdAndCreatedFromTemplateTrue(Long userId);

    // Find transfers involving an account (either from or to)
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.recordType = 'TRANSFER' AND (fr.account.id = :accountId OR fr.toAccount.id = :accountId)")
    List<FinancialRecord> findTransfersByAccountId(@Param("accountId") Long accountId);

    // Find transfers between two specific accounts
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.recordType = 'TRANSFER' AND " +
           "((fr.account.id = :fromAccountId AND fr.toAccount.id = :toAccountId) OR " +
           "(fr.account.id = :toAccountId AND fr.toAccount.id = :fromAccountId))")
    List<FinancialRecord> findTransfersBetweenAccounts(
            @Param("fromAccountId") Long fromAccountId,
            @Param("toAccountId") Long toAccountId);

    // Get total by user and record type
    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.user.id = :userId AND fr.recordType = :recordType")
    Double getTotalByUserAndRecordType(@Param("userId") Long userId, @Param("recordType") RecordType recordType);

    // Get total by user and record type within date range
    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.user.id = :userId AND fr.recordType = :recordType AND fr.recordDate BETWEEN :startDate AND :endDate")
    Double getTotalByUserAndRecordTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("recordType") RecordType recordType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get total by account and date range
    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.account.id = :accountId AND fr.recordDate BETWEEN :startDate AND :endDate")
    Double getTotalByAccountAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get total by category
    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.category.id = :categoryId")
    Double getTotalByCategory(@Param("categoryId") Long categoryId);

    // Get total by category and date range
    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.category.id = :categoryId AND fr.recordDate BETWEEN :startDate AND :endDate")
    Double getTotalByCategoryAndDateRange(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find recent records by user (ordered by record date and time)
    @Query("SELECT fr FROM FinancialRecord fr WHERE fr.user.id = :userId ORDER BY fr.recordDate DESC, fr.recordTime DESC")
    List<FinancialRecord> findRecentRecordsByUser(@Param("userId") Long userId);

    // Count records by user
    Long countByUserId(Long userId);

    // Count records by account
    Long countByAccountId(Long accountId);

    // Count records by category
    Long countByCategoryId(Long categoryId);
}
