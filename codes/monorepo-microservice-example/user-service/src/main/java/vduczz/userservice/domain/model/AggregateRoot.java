package vduczz.userservice.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot {

    // ____________________ Aggregate identity ____________________ //
    public abstract UUID getAggregateId();

    public abstract String getAggregateType();


    // ____________________ Domain Event ____________________ //
    private final List<Object> events = new ArrayList<>();

    protected void registerEvent(Object event) {
        events.add(event);
    }

    public List<Object> pullEvents() {
        List<Object> events = List.copyOf(this.events);
        this.events.clear();
        return events;
    }
}
