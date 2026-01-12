package com.factory.machine_events.repository;

import com.factory.machine_events.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    Optional<EventEntity> findByEventId(String eventId);

    List<EventEntity> findByMachineIdAndEventTimeBetween(
            String machineId,
            Instant start,
            Instant end
    );
    List<EventEntity> findByFactoryIdAndEventTimeBetween(
        String factoryId,
        Instant start,
        Instant end
);

}