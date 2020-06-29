package me.wedrowicz.architecturecheatsheet.eventsourcing.car

import me.wedrowicz.architecturecheatsheet.eventsourcing.shared.DomainEvent
import java.time.Instant

data class CarRented(val clientId: String, val time: Instant) : DomainEvent
data class CarHandedOver(val clientId: String, val time: Instant) : DomainEvent

class CarBroken : DomainEvent {
    override fun toString(): String = "CarBroken"
}

class CarRepaired : DomainEvent {
    override fun toString(): String = "CarRepaired"
}
