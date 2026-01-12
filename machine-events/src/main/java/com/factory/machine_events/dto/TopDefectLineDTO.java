package com.factory.machine_events.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDefectLineDTO {

    private String lineId;
    private long totalDefects;
    private long eventCount;
    private double defectsPercent;
}
