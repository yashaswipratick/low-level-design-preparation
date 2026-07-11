package com.lld.practice.tutor_sessions.parking_lot.implementation.model;

public class Slot {

    private Integer slotNumber;
    private SlotStatus status;
    private SlotType slotType;

    public Slot(Integer slotNumber, SlotStatus status, SlotType slotType) {
        this.slotNumber = slotNumber;
        this.status = status;
        this.slotType = slotType;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public void setSlotType(SlotType slotType) {
        this.slotType = slotType;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "slotNumber=" + slotNumber +
                ", status=" + status +
                ", slotType=" + slotType +
                '}';
    }
}
