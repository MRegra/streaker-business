package com.streaker.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Reward {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int pointsRequired;

    // Relationships (no need for the bidirectional)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean unlocked = false;

    @Column
    private Instant unlockedAt;
}