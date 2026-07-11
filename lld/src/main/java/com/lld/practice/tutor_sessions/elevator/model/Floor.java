package com.lld.practice.tutor_sessions.elevator.model;

public class Floor {

    private Integer id;

    public Floor(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "id=" + id +
                '}';
    }
}
