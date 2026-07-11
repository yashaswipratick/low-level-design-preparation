package com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.impl;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.SlotAllocator;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Slot;

import java.util.List;
import java.util.Optional;

public class FirstAvailableAllocator implements SlotAllocator {


    @Override
    public Optional<Slot> allocate(List<Slot> candidates) {
        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(0));
    }
}
