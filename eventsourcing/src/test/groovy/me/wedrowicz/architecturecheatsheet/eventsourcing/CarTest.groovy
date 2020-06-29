package me.wedrowicz.architecturecheatsheet.eventsourcing

import me.wedrowicz.architecturecheatsheet.eventsourcing.car.Car
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.CarBroken
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.CarNotAvailable
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.CarNotBroken
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.CarRented
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.DifferentClient
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.HandOverCar
import me.wedrowicz.architecturecheatsheet.eventsourcing.car.RentCar
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CarTest extends Specification {

    def now = Instant.now()
    @Subject
    def car = new Car(Clock.fixed(now, ZoneId.systemDefault()), "car1")

    def "available car can be rented"() {
        when:
        car.rent(new RentCar("client1"))
        then:
        !car.available
    }

    def "rented car cannot be rented again"() {
        given:
        car.handle(new CarRented("client1", now))
        when:
        car.rent(new RentCar("client2"))
        then:
        thrown(CarNotAvailable)
    }

    def "handed over car is ready for next rental"() {
        given:
        car.handle(new CarRented("client1", now))
        when:
        car.handOver(new HandOverCar("client1"))
        then:
        car.available
    }

    def "only the same client can hand over car"() {
        given:
        car.handle(new CarRented("client1", now))
        when:
        car.handOver(new HandOverCar("client2"))
        then:
        thrown(DifferentClient)
    }

    def "broken car should be not available for rent and current rental should be ended"() {
        given:
        car.handle(new CarRented("client1", now))
        when:
        car.markAsBroken()
        then:
        !car.available
        car.currentRental == null
    }

    def "repaired car should be available for rent"() {
        given:
        car.handle(new CarBroken())
        when:
        car.repairCar()
        then:
        car.available
    }

    def "only broken car can be repaired"() {
        when:
        car.repairCar()
        then:
        thrown(CarNotBroken)
    }
}
