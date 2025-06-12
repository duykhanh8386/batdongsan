package com.BTL.Springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 20)
    private String transactionCode;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private Customer buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Customer seller;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Employee agent;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "commission", nullable = false)
    private Double commission;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "pending";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "transaction")
    private List<Payment> payments;
}
