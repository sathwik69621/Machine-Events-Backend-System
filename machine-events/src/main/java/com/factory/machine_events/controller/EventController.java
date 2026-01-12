package com.factory.machine_events.controller;

import com.factory.machine_events.dto.BatchResponseDTO;
import com.factory.machine_events.dto.EventRequestDTO;
import com.factory.machine_events.dto.StatsResponseDTO;
import com.factory.machine_events.dto.TopDefectLineDTO;
import com.factory.machine_events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;


import java.util.List;



@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    @GetMapping("/stats")
public StatsResponseDTO getStats(
        @RequestParam String machineId,
        @RequestParam Instant start,
        @RequestParam Instant end) {

    return eventService.getStats(machineId, start, end);
}

    @GetMapping("/stats/top-defect-lines")
public List<TopDefectLineDTO> topDefectLines(
        @RequestParam String factoryId,
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam(defaultValue = "10") int limit) {

    return eventService.getTopDefectLines(factoryId, from, to, limit);
}


    @PostMapping("/batch")
    public BatchResponseDTO ingest(@RequestBody List<EventRequestDTO> events) {
        return eventService.ingestBatch(events);
    }
}
