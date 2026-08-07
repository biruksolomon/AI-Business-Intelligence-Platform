package com.company.aibiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactEmail;

    private String phone;

    /** Historical on-time delivery rate, 0.0 - 1.0. Used by the AI supplier-ranking endpoint. */
    @Column(nullable = false)
    private Double reliabilityScore;

    @Column(nullable = false)
    private Double averageDeliveryDays;
}