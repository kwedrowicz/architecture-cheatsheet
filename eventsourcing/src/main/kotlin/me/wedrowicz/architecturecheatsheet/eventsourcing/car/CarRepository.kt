package me.wedrowicz.architecturecheatsheet.eventsourcing.car

interface CarRepository {
    fun save(car: Car)
    fun findBy(carId: String): Car?
}
