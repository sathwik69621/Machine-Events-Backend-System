package com.factory.machine_events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RejectionDTO {
    private String eventId;
    private String reason;
}
