package me.wedrowicz.architecturecheatsheet.eventsourcing.shared

abstract class EventBasedClass<T>(private val id: String) {
    private val pendingEvents = mutableListOf<DomainEvent>()
    private var lastLoadedEventId: String? = null

    fun getImmutablePendingEvents(): List<DomainEvent> = pendingEvents.toList()
    fun getLastLoadedEventId() = lastLoadedEventId
    fun flushEvents() {
        pendingEvents.clear()
    }

    fun handle(event: DomainEvent): T {
        pendingEvents.add(event)
        println("Handle event for aggregate ${this.javaClass.simpleName}(${this.id}): $event")

        return applyEvent(event)
    }

    fun handle(event: EnrichedEvent): T {
        lastLoadedEventId = event.id
        return applyEvent(event.event)
    }

    protected abstract fun applyEvent(event: DomainEvent): T
}
