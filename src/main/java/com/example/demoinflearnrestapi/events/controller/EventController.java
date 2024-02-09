package com.example.demoinflearnrestapi.events.controller;

import com.example.demoinflearnrestapi.events.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {

        EntityModel<Errors> errorResource = EntityModel.of(errors);
        errorResource.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));

        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errorResource);
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errorResource);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = this.eventRepository.save(event);

        // TODO HATEOAS 적용-1
        // TODO Location 헤더 정보에 URI 담기
        // TODO ControllerLinkBuilder(2.1.0.RELEASE) -> WebMvcLinkBuilder(2.2.5 RELEASE)

        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class)
                .slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();

        // TODO HATEOAS 적용 - 2
        EntityModel<Event> eventResource = EntityModel.of(newEvent);
        eventResource.add(selfLinkBuilder.withSelfRel());
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventResource);
    }
}
