package me.wedrowicz.architecturecheatsheet.eventsourcing.car

data class RentCar(val clientId: String)
data class HandOverCar(val clientId: String)
