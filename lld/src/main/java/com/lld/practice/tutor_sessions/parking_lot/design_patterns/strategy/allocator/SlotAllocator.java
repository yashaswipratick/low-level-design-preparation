package com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator;

import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Slot;

import java.util.List;
import java.util.Optional;

public interface SlotAllocator {

    Optional<Slot> allocate(List<Slot> candidates);
}
