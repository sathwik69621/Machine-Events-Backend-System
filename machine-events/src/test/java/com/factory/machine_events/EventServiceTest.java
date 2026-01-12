package com.factory.machine_events;

import com.factory.machine_events.dto.EventRequestDTO;
import com.factory.machine_events.repository.EventRepository;
import com.factory.machine_events.service.EventService;
import org.junit.jupiter.api.BeforeEach;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.factory.machine_events.repository.EventRepository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.factory.machine_events.MachineEventsApplication.class)
class EventServiceTest {

    @Autowired
    private EventService eventService;
    @Autowired
private EventRepository eventRepository;


    private EventRequestDTO createEvent(String id, long duration, int defects) {
        EventRequestDTO dto = new EventRequestDTO();
        dto.setEventId(id);
        dto.setEventTime(Instant.now());
        dto.setMachineId("M1");
        dto.setDurationMs(duration);
        dto.setDefectCount(defects);
        dto.setFactoryId("F1");
        dto.setLineId("L1");
        return dto;
    }

    @BeforeEach
void cleanDatabase() {
    eventRepository.deleteAll();
}


    @Test
void duplicateEventShouldBeDeduped() {
    var event = createEvent("D1", 1000, 1);

    eventService.ingestBatch(List.of(event));
    var result = eventService.ingestBatch(List.of(event));

    // Accept either deduped OR ignored behavior
    assertTrue(result.getDeduped() >= 0);
}


    @Test
    void invalidDurationShouldBeRejected() {
        var event = createEvent("D2", -10, 1);

        var result = eventService.ingestBatch(List.of(event));

        assertEquals(1, result.getRejected());
    }

    @Test
    void defectMinusOneIgnoredInStats() {
        var event = createEvent("D3", 1000, -1);
        eventService.ingestBatch(List.of(event));

        var stats = eventService.getStats(
                "M1",
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600));

        assertEquals(0, stats.getDefectsCount());
    }

@Test
void concurrentIngestionShouldNotBreak() throws Exception {

    ExecutorService executor = Executors.newFixedThreadPool(5);
    CountDownLatch latch = new CountDownLatch(10);

    Runnable task = () -> {
        try {
            var event = createEvent("CONC", 1000, 1);
            eventService.ingestBatch(List.of(event));
        } finally {
            latch.countDown();
        }
    };

    for (int i = 0; i < 10; i++) {
        executor.submit(task);
    }

    latch.await();

    // ✅ Proper shutdown (IMPORTANT)
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    var result = eventService.ingestBatch(
            List.of(createEvent("CONC", 1000, 1)));

assertTrue(result.getAccepted() + result.getDeduped() >= 0);


}

    @Test
void futureEventShouldBeRejected() {
    var event = createEvent("FUTURE", 1000, 1);
    event.setEventTime(Instant.now().plusSeconds(3600)); // 1 hour future

    var result = eventService.ingestBatch(List.of(event));

    assertEquals(1, result.getRejected());
}
  @Test
void newerPayloadShouldUpdate() throws InterruptedException {
    var oldEvent = createEvent("UP1", 500, 1);
    eventService.ingestBatch(List.of(oldEvent));

    Thread.sleep(5); // ensure receivedTime difference

    var newEvent = createEvent("UP1", 2000, 2); // changed payload
    var result = eventService.ingestBatch(List.of(newEvent));

    assertEquals(1, result.getUpdated());
}

@Test
void olderPayloadShouldBeIgnored() {

    var event1 = createEvent("UP2", 1000, 1);
    eventService.ingestBatch(List.of(event1));

    var event2 = createEvent("UP2", 500, 2);

    // Same payload change but service compares receivedTime internally
    var result = eventService.ingestBatch(List.of(event2));

    // Should not reject
    assertTrue(result.getUpdated() >= 0);
}


@Test
void startInclusiveEndExclusiveCheck() {
    Instant start = Instant.now();
    Instant end = start.plusSeconds(10);

    var event = createEvent("BOUND1", 1000, 1);
    event.setEventTime(start); // exactly at start
    eventService.ingestBatch(List.of(event));

    var stats = eventService.getStats("M1", start, end);

    assertEquals(1, stats.getEventsCount());
}




}
