package ru.practicum.shareit.request.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemRequestDto {
    @NotBlank(message = "Описание вещи не может быть пустым.")
    @Size(min = 1, max = 1000)
    private String description;
}