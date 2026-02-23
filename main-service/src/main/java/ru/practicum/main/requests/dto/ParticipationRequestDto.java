package ru.practicum.main.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestDto {

    private Long id;

    private Long event;

    private Long requester;

    private String status;

    private String created; // формат yyyy-MM-dd HH:mm:ss
}