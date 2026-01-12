package com.factory.machine_events.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_machine_time", columnList = "machineId,eventTime")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Instant receivedTime;

    @Column(nullable = false)
    private String machineId;

    private String factoryId;

    private String lineId;

    private long durationMs;

    private int defectCount;
}
