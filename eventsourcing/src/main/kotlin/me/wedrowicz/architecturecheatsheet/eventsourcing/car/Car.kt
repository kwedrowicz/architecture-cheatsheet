package me.wedrowicz.architecturecheatsheet.eventsourcing.car

import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.DomainEvent
import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.EnrichedEvent
import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.EventBasedClass
import java.time.Clock
import java.time.Instant
import java.util.UUID

class Car(private val clock: Clock = Clock.systemDefaultZone(), val id: String = UUID.randomUUID().toString()) :
    EventBasedClass<Car>(id) {
    private var currentRental: Rental? = null
    private var available = true
    private var isBroken = false

    fun rent(command: RentCar) {
        if (!available) {
            throw CarNotAvailable()
        }

        handle(
            CarRented(
                command.clientId,
                clock.instant()
            )
        )
    }

    fun handOver(command: HandOverCar) {
        if (available) {
            throw CarNotRented()
        }
        if (command.clientId != currentRental?.clientId) {
            throw DifferentClient()
        }

        handle(
            CarHandedOver(
                command.clientId,
                clock.instant()
            )
        )
    }

    fun markAsBroken() {
        handle(CarBroken())
    }

    fun repairCar() {
        if (!isBroken) {
            throw CarNotBroken()
        }

        handle(CarRepaired())
    }

    override fun applyEvent(event: DomainEvent): Car = when (event) {
        is CarRented -> apply(event)
        is CarHandedOver -> apply(event)
        is CarBroken -> apply(event)
        is CarRepaired -> apply(event)
        else -> throw IllegalArgumentException()
    }

    private fun apply(event: CarHandedOver): Car {
        available = true
        currentRental = null
        return this
    }

    private fun apply(event: CarRented): Car {
        available = false
        currentRental =
            Rental(event.clientId, event.time)
        return this
    }

    private fun apply(event: CarBroken): Car {
        available = false
        currentRental = null
        isBroken = true
        return this
    }

    private fun apply(event: CarRepaired): Car {
        available = true
        isBroken = false
        return this
    }

    companion object {
        fun recreateFrom(events: List<EnrichedEvent>, initialState: Car): Car = events.fold(initialState, Car::handle)
    }
}

data class Rental(val clientId: String, val startedAt: Instant)
