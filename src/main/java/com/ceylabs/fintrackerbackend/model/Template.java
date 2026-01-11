package com.ceylabs.fintrackerbackend.model;

import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Template name is required")
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String name;

    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private BigDecimal amount; // Optional - can be set when using template

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RecordType recordType;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Optional

    @ManyToMany
    @JoinTable(
        name = "template_label",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // Optional - for pre-selecting account

    @Column(length = 500)
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentType paymentType; // Optional

    @Size(max = 100)
    @Column(length = 100)
    private String payer; // Optional - who paid/received

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    // For transfer templates
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount; // Used only for TRANSFER type

    // Constructors
    public Template() {}

    public Template(String name, RecordType recordType, User user) {
        this.name = name;
        this.recordType = recordType;
        this.user = user;
        this.createdDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    @PrePersist
    private void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
        validateTransferFields();
    }

    @PreUpdate
    private void onUpdate() {
        validateTransferFields();
    }

    private void validateTransferFields() {
        if (recordType == RecordType.TRANSFER) {
            if (toAccount == null) {
                throw new IllegalStateException("To account is required for transfer templates");
            }
            if (account == null) {
                throw new IllegalStateException("From account is required for transfer templates");
            }
            if (account.equals(toAccount)) {
                throw new IllegalStateException("Cannot transfer to the same account");
            }
        } else {
            // Clear toAccount for non-transfer types
            toAccount = null;
        }
    }
}
