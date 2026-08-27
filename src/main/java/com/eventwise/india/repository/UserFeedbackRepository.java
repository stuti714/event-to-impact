package com.eventtoimpact.india.repository;

import com.eventtoimpact.india.model.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    long countByUsername(String username);
}
