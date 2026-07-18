package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comments.model.Comment;

import java.util.List;

@Getter
@AllArgsConstructor
public class ItemWithDetails {
    private final Item item;
    private final List<Booking> bookings;
    private final List<Comment> comments;
}