package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    Collection<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(int requestId);

    Collection<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(int requestId);
}
