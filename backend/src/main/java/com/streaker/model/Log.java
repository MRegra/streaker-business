package com.streaker.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "completed")
    private Boolean completed = Boolean.FALSE;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    @ManyToOne(optional = false)
    @JoinColumn(name = "habit_uuid", nullable = false)
    private Habit habit;

}
