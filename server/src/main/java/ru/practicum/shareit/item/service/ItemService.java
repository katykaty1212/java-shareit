package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemWithDetails;

import java.util.List;

public interface ItemService {
    List<Item> findAllItemsByUser(Long ownerId);

    List<ItemWithDetails> findAllItemsByUserWithDetails(Long ownerId);

    Item findItemById(Long itemId);

    Item createItem(Item item, Long ownerId, Long requestId);

    Item updateItem(Item newItemData, Long itemId, Long ownerId);

    void deleteItem(Long itemId, Long ownerId);

    List<Item> searchItems(String text);

    Comment addComment(Long itemId, Long userId, String text);
}