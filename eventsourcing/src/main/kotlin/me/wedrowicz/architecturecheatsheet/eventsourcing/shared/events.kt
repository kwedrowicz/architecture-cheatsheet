package me.wedrowicz.architecturecheatsheet.eventsourcing.shared

import java.time.Instant
import java.util.UUID

interface DomainEvent
data class EnrichedEvent(val id: String, val timestamp: Instant, val eventName: String, val event: DomainEvent) {
    companion object {
        fun fromDomain(event: DomainEvent): EnrichedEvent =
            EnrichedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.javaClass.simpleName,
                event
            )
    }
}
