package me.wedrowicz.architecturecheatsheet.eventsourcing.car

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.EnrichedEvent
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.time.Clock
import java.time.Instant

class EventStoreDBCarRepository : CarRepository {
    private val restTemplate = RestTemplateBuilder().build()

    override fun save(car: Car) {
        val headers = HttpHeaders()
        headers["Content-Type"] = "application/vnd.eventstore.events+json"
        car.getLastLoadedEventId()?.let { headers["ES-ExpectedVersion"] = it }
        headers.setBasicAuth("admin", "changeit")
        val body = car.getImmutablePendingEvents()
            .map { EnrichedEvent.fromDomain(it) }
            .map { EventDto.fromEnrichedEvent(it) }
        val request = HttpEntity(body, headers)
        try {
            restTemplate.exchange("http://127.0.0.1:2113/streams/${car.id}", HttpMethod.POST, request, Void::class.java)
        } catch (exception: HttpClientErrorException) {
            if (exception.statusCode != HttpStatus.BAD_REQUEST || exception.message != "Wrong expected EventNumber") {
                throw IllegalStateException("Dirty write")
            }
        }

    }

    override fun findBy(carId: String): Car? {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        val headers = HttpHeaders()
        headers["Content-Type"] = "application/vnd.eventstore.events+json"
        headers.setBasicAuth("admin", "changeit")
        val request = HttpEntity(null, headers)
        val events = restTemplate.exchange(
            "http://127.0.0.1:2113/streams/${carId}?embed=body",
            HttpMethod.GET,
            request,
            EventsCollectionDto::class.java
        )
            .body!!.entries.map { it.toEnrichedEvent(objectMapper) }.reversed()
        println(events)
        return Car.recreateFrom(events, Car(Clock.systemDefaultZone(), carId))
    }

    fun clear(vararg ids: String) {
        val headers = HttpHeaders()
        headers.setBasicAuth("admin", "changeit")
        val request = HttpEntity(null, headers)

        for (id in ids) {
            restTemplate.exchange("http://127.0.0.1:2113/streams/$id", HttpMethod.DELETE, request, Void::class.java)
        }
    }
}

data class EventDto(
    val eventId: String,
    val eventType: String,
    val data: Any
) {
    companion object {
        fun fromEnrichedEvent(event: EnrichedEvent): EventDto = EventDto(event.id, event.eventName, event.event)
    }
}

data class EventsCollectionDto(
    val entries: List<EventEntry>
)

data class EventEntry(
    val eventId: String,
    val eventType: String,
    val updated: Instant,
    val data: String,
    val eventNumber: Long
) {
    fun toEnrichedEvent(objectMapper: ObjectMapper): EnrichedEvent {
        val eventClass = when (eventType) {
            CarRented::class.simpleName -> CarRented::class.java
            CarHandedOver::class.simpleName -> CarHandedOver::class.java
            CarBroken::class.simpleName -> CarBroken::class.java
            CarRepaired::class.simpleName -> CarRepaired::class.java
            else -> throw IllegalArgumentException("Not recognized event: $eventType")
        }
        return EnrichedEvent(eventNumber.toString(), updated, eventType, objectMapper.readValue(data, eventClass))
    }
}
