package com.factory.machine_events.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StatsResponseDTO {

    private String machineId;
    private Instant start;
    private Instant end;

    private long eventsCount;
    private long defectsCount;
    private double avgDefectRate;
    private String status;
}
