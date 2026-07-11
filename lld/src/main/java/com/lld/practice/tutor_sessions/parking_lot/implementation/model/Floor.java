package com.lld.practice.tutor_sessions.parking_lot.implementation.model;

import java.util.List;

public class Floor {
    private Integer id;
    private List<Slot> slot;

    public Floor(Integer id, List<Slot> slot) {
        this.id = id;
        this.slot = slot;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Slot> getSlot() {
        return slot;
    }

    public void setSlot(List<Slot> slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "id=" + id +
                ", slot=" + slot +
                '}';
    }
}
