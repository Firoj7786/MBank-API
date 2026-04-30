package com.mbank.repository;

import com.mbank.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    boolean existsByEventId(String eventId);
}
