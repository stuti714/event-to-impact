package com.eventtoimpact.india.repository;

import com.eventtoimpact.india.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByStartTimeAsc();
    List<Event> findByEndTimeAfterOrderByStartTimeAsc(LocalDateTime time);
}
