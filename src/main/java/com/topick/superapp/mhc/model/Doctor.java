package com.topick.superapp.mhc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "doctors")
public class Doctor {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private User users;

    @Nationalized
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Nationalized
    @Column(name = "specialty", length = 100)
    private String specialty;

    @Nationalized
    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Nationalized
    @Lob
    @Column(name = "bio")
    private String bio;

    @Column(name = "price_per_session", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

}