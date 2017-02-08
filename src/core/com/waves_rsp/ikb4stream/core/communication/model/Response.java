package com.waves_rsp.ikb4stream.core.communication.model;

import com.waves_rsp.ikb4stream.core.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Response {
    private final List<Event> events;
    private final Request request;

    public Response(List<Event> events, Request request) {
        Objects.requireNonNull(events);
        Objects.requireNonNull(request);

        this.events = events;
        this.request = request;
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public Request getRequest() {
        return request;
    }
}
