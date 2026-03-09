package com.reto.catalog_service.repository;

import com.reto.catalog_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    @Modifying
    @Query(value = """
    INSERT INTO processed_events(event_id, processed_at)
    VALUES (:eventId, NOW())
    ON CONFLICT (event_id) DO NOTHING
    """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") String eventId);
}