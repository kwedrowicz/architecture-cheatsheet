package me.wedrowicz.architecturecheatsheet.eventsourcing.car

import spock.lang.Specification

import java.time.Clock
import java.time.ZoneId

import static java.time.Instant.now

class EventStoreDBCarRepositoryTest extends Specification {
    def carRepository = new EventStoreDBCarRepository()

    def now = now()
    def carId = "car1"
    def car = new Car(Clock.fixed(now, ZoneId.systemDefault()), carId)

    def setup() {
        carRepository.clear(carId)
    }

    def "should load saved car"() {
        given:
        car.handle(new CarRented("client1", now))
        when:
        carRepository.save(car)
        and:
        def savedCar = carRepository.findBy(car.id)
        then:
        !savedCar.available
        and:
        savedCar.getImmutablePendingEvents() == []
    }

    def "should block dirty writes"() {
        given:
        car.handle(new CarRented("client1", now))
        carRepository.save(car)
        def loadedCar1 = carRepository.findBy(car.id)
        def loadedCar2 = carRepository.findBy(car.id)
        loadedCar1.handOver(new HandOverCar("client1"))
        carRepository.save(loadedCar1)
        when:
        loadedCar2.handOver(new HandOverCar("client1"))
        carRepository.save(loadedCar2)
        then:
        thrown(IllegalStateException)
    }
}
