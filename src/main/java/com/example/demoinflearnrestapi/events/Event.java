package com.example.demoinflearnrestapi.events;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "id")
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // 이게 없으면 온라인 모임
    private int basePrice; // optional
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @Enumerated(value = EnumType.STRING) // Enum 타입을 스트링으로 저장
    private EventStatus eventStatus = EventStatus.DRAFT;


    public void update() {

        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }

        // Update offline
        if (this.location == null) {
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
}
