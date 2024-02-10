package com.example.demoinflearnrestapi.events;

import com.example.demoinflearnrestapi.events.controller.EventController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EventResource extends EntityModel<Event> {

    public EventResource(Event event, Link... links) {

        super(event, List.of(links));
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}
