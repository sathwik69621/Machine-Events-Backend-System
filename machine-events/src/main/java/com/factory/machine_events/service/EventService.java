package com.factory.machine_events.service;
import com.factory.machine_events.MachineEventsApplication;

import com.factory.machine_events.dto.BatchResponseDTO;
import com.factory.machine_events.dto.EventRequestDTO;
import com.factory.machine_events.dto.RejectionDTO;
import com.factory.machine_events.dto.StatsResponseDTO;
import com.factory.machine_events.dto.TopDefectLineDTO;
import com.factory.machine_events.entity.EventEntity;
import com.factory.machine_events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

@Transactional
public BatchResponseDTO ingestBatch(List<EventRequestDTO> events) {

    BatchResponseDTO response = new BatchResponseDTO();

    for (EventRequestDTO dto : events) {
        try {
            validate(dto);

            Instant now = Instant.now();

            EventEntity incoming = mapToEntity(dto);
            incoming.setReceivedTime(now);

            eventRepository.findByEventId(dto.getEventId())
                    .ifPresentOrElse(existing -> {

                        // Compare payload (excluding receivedTime)
                        boolean samePayload =
                                Objects.equals(existing.getEventTime(), incoming.getEventTime()) &&
                                Objects.equals(existing.getMachineId(), incoming.getMachineId()) &&
                                Objects.equals(existing.getDurationMs(), incoming.getDurationMs()) &&
                                Objects.equals(existing.getDefectCount(), incoming.getDefectCount()) &&
                                Objects.equals(existing.getFactoryId(), incoming.getFactoryId()) &&
                                Objects.equals(existing.getLineId(), incoming.getLineId());

                        if (samePayload) {
                            // Deduplication
                            response.setDeduped(response.getDeduped() + 1);
                        } else {
                            // Payload differs → update based on receivedTime
                            if (incoming.getReceivedTime()
                                    .isAfter(existing.getReceivedTime())) {

                                existing.setEventTime(incoming.getEventTime());
                                existing.setMachineId(incoming.getMachineId());
                                existing.setDurationMs(incoming.getDurationMs());
                                existing.setDefectCount(incoming.getDefectCount());
                                existing.setFactoryId(incoming.getFactoryId());
                                existing.setLineId(incoming.getLineId());
                                existing.setReceivedTime(incoming.getReceivedTime());

                                eventRepository.save(existing);
                                response.setUpdated(response.getUpdated() + 1);
                            } else {
                                // Older update ignored
                                response.setDeduped(response.getDeduped() + 1);
                            }
                        }

                    }, () -> {
                        // New event
                        eventRepository.save(incoming);
                        response.setAccepted(response.getAccepted() + 1);
                    });
//// the below catch block reduces the noise
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
    // Duplicate insert due to race condition → treat as dedupe
    response.setDeduped(response.getDeduped() + 1);

} catch (Exception ex) {
    response.setRejected(response.getRejected() + 1);
    response.getRejections()
            .add(new RejectionDTO(dto.getEventId(), ex.getMessage()));
}

    }

    return response;
}


    private void validate(EventRequestDTO dto) {
        long duration = dto.getDurationMs();

        if (duration < 0 || duration > 21600000) {
            throw new RuntimeException("INVALID_DURATION");
        }

        if (dto.getEventTime().isAfter(Instant.now().plusSeconds(15 * 60))) {
            throw new RuntimeException("EVENT_TIME_IN_FUTURE");
        }
    }

    private EventEntity mapToEntity(EventRequestDTO dto) {
        return EventEntity.builder()
                .eventId(dto.getEventId())
                .eventTime(dto.getEventTime())
                .machineId(dto.getMachineId())
                .durationMs(dto.getDurationMs())
                .defectCount(dto.getDefectCount())
                .factoryId(dto.getFactoryId())
                .lineId(dto.getLineId())
                .build();
    }

    public StatsResponseDTO getStats(String machineId, Instant start, Instant end) {

    var events = eventRepository
            .findByMachineIdAndEventTimeBetween(machineId, start, end);

    long eventsCount = events.size();

    long defectsCount = events.stream()
            .filter(e -> e.getDefectCount() != -1)
            .mapToLong(EventEntity::getDefectCount)
            .sum();

    double windowHours =
            (end.toEpochMilli() - start.toEpochMilli()) / 3600000.0;

    double avgDefectRate =
            windowHours > 0 ? defectsCount / windowHours : 0;

    String status =
            avgDefectRate < 2.0 ? "Healthy" : "Warning";

    return StatsResponseDTO.builder()
            .machineId(machineId)
            .start(start)
            .end(end)
            .eventsCount(eventsCount)
            .defectsCount(defectsCount)
            .avgDefectRate(avgDefectRate)
            .status(status)
            .build();
}

    public List<TopDefectLineDTO> getTopDefectLines(
        String factoryId,
        Instant from,
        Instant to,
        int limit) {

    var events = eventRepository
            .findByFactoryIdAndEventTimeBetween(factoryId, from, to);

    return events.stream()
            .filter(e -> e.getDefectCount() != -1)
            .collect(java.util.stream.Collectors.groupingBy(
                    EventEntity::getLineId
            ))
            .entrySet()
            .stream()
            .map(entry -> {

                String lineId = entry.getKey();
                var list = entry.getValue();

                long totalDefects = list.stream()
                        .mapToLong(EventEntity::getDefectCount)
                        .sum();

                long eventCount = list.size();

                double defectsPercent =
                        eventCount == 0 ? 0 :
                                (totalDefects * 100.0) / eventCount;

                return TopDefectLineDTO.builder()
                        .lineId(lineId)
                        .totalDefects(totalDefects)
                        .eventCount(eventCount)
                        .defectsPercent(
                                Math.round(defectsPercent * 100.0) / 100.0
                        )
                        .build();
            })
            .sorted((a, b) ->
                    Long.compare(b.getTotalDefects(), a.getTotalDefects()))
            .limit(limit)
            .toList();
}


    }
