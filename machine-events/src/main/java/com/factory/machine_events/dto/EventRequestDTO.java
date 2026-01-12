package com.factory.machine_events.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class EventRequestDTO {

    private String eventId;
    private Instant eventTime;
    private String machineId;
    private Long durationMs;
    private Integer defectCount;
    private String factoryId;
    private String lineId;
}
