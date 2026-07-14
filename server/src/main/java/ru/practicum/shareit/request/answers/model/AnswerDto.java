package ru.practicum.shareit.request.answers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    private Long id;
    private Long itemId;

    @JsonProperty("name")
    private String itemName;
    private Long ownerId;
}