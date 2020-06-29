package me.wedrowicz.architecturecheatsheet.eventsourcing

import me.wedrowicz.architecturecheatsheet.eventsourcing.car.Car
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.CarRented
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.HandOverCar
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.InMemoryCarRepository
import spock.lang.Specification

import java.time.Clock
import java.time.ZoneId

import static java.time.Instant.now

class InMemoryCarRepositoryTest extends Specification {

    def carRepository = new InMemoryCarRepository()

    def now = now()
    def car = new Car(Clock.fixed(now, ZoneId.systemDefault()), "car1")

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
