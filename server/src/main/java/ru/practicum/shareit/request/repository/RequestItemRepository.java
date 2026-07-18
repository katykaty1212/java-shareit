package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.RequestItem;

import java.util.List;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {

    List<RequestItem> findAllByRequestorIdOrderByCreatedDesc(Long ownerId);

    @Query("SELECT r FROM RequestItem r WHERE r.requestor.id != ?1 ORDER BY r.created DESC")
    List<RequestItem> findAllByRequestorIdNotOrderByCreatedDesc(Long userId);
}