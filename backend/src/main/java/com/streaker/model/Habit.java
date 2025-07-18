package com.streaker.model;

import com.streaker.utlis.enums.Frequency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Getter
@Setter
@Entity
@Table(name = "habits")
public class Habit {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Frequency frequency;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Relationships (no need for the bidirectional)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "streak_uuid", nullable = false)
    private Streak streak;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_uuid", nullable = false)
    private Category category;
}
