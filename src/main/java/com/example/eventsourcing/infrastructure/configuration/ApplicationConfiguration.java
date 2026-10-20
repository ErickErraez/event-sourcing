package com.example.eventsourcing.infrastructure.configuration;

import com.example.eventsourcing.application.port.out.EventStore;
import com.example.eventsourcing.application.usecase.OrderApplicationService;
import com.example.eventsourcing.infrastructure.persistence.InMemoryEventStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    EventStore eventStore() {
        return new InMemoryEventStore();
    }

    @Bean
    OrderApplicationService orderApplicationService(EventStore eventStore) {
        return new OrderApplicationService(eventStore);
    }
}