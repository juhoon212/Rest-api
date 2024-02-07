package com.example.demoinflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;



    @Test
    @DisplayName("정상적으로 이벤트를 생성하는 test")
    public void createEvent() throws Exception {

        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API DEVELOpment with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2010, 11, 7, 11, 11, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2010, 11, 8, 13, 11, 20))
                .beginEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 11))
                .endEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 12))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("free").value(Matchers.not(false)))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT));

    }

    @Test
    @DisplayName("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEventBad() throws Exception {

        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API DEVELOpment with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2010, 11, 7, 11, 11, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2010, 11, 8, 13, 11, 20))
                .beginEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 11))
                .endEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 12))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력 값이 비어있는 경우 에러가 발생하는 테스트")
    public void CreateEvent_Bad_request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API DEVELOpment with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2010, 11, 7, 11, 11, 30))
                .closeEnrollmentDateTime(LocalDateTime.of(2010, 11, 8, 13, 11, 20))
                .beginEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 11))
                .endEventDateTime(LocalDateTime.of(2010, 11, 25, 20, 30, 12))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());

    }

    @Test
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // Given
        Event event1 = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();
        // When
        event1.update();

        // Then
        assertThat(event1.isFree()).isEqualTo(isFree);

        // Given
        Event event2 = Event.builder()
                .basePrice(100)
                .maxPrice(0)
                .build();
        // When
        event2.update();

        // Then
        assertThat(event2.isFree()).isFalse();

        // Given
        Event event3 = Event.builder()
                .basePrice(0)
                .maxPrice(100)
                .build();
        // When
        event3.update();

        // Then
        assertThat(event3.isFree()).isFalse();
    }

    @Test
    @ParameterizedTest
    @MethodSource("paramsForTestOffline")
    public void testOffline() {

        // Given
        Event event = Event.builder()
                .location("강남역 네이버 D2 스타텁 팩토리")
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isTrue();

        // Given
        event = Event.builder()
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isFalse();
    }

    private static Stream<Arguments> paramsForTestOffline() {

        return Stream.of(
                Arguments.of(0, 0, true),
                Arguments.of(100, 0, false),
                Arguments.of(0, 100, false),
                Arguments.of(100, 200, false)
        );
    }
}
