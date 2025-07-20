package com.streaker.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "streaks")
public class Streak {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(name = "current_count", nullable = false)
    private int currentCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "last_check")
    private LocalDate lastCheck;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Relationships (no need for the bidirectional)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;
}
