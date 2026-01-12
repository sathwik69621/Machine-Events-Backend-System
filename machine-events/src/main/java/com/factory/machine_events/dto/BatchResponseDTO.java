package com.factory.machine_events.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchResponseDTO {

    private int accepted;
    private int deduped;
    private int updated;
    private int rejected;

    private List<RejectionDTO> rejections = new ArrayList<>();
}
