package me.wedrowicz.architecturecheatsheet.eventsourcing.car

import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.EnrichedEvent
import java.time.Clock

class InMemoryCarRepository: CarRepository {
    private val events = mutableMapOf<String, MutableList<EnrichedEvent>>()

    override fun save(car: Car) {
        events.putIfAbsent(car.id, mutableListOf())
        val eventsForGivenId = events[car.id]!!
        synchronized(eventsForGivenId) {
            if (eventsForGivenId.isNotEmpty() && eventsForGivenId.last().id != car.getLastLoadedEventId()) {
                throw IllegalStateException("Dirty write")
            }

            eventsForGivenId.addAll(car.getImmutablePendingEvents().map { EnrichedEvent.fromDomain(it) })
        }
        car.flushEvents()
    }

    override fun findBy(carId: String): Car? = events[carId]?.let { Car.recreateFrom(it,
        Car(Clock.systemDefaultZone(), carId)
    ) }
}
