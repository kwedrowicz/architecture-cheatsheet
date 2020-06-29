package me.wedrowicz.architecturecheatsheet.eventsourcing.car

class CarNotAvailable: Exception("Car not available")
class CarNotRented: Exception("Car not rented")
class DifferentClient: Exception("Different client than in previous action")
class CarNotBroken: Exception("Car not broken")
