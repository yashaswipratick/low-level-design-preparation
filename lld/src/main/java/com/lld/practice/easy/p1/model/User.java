package com.lld.practice.easy.p1.model;

public class User {

    private String id;
    private String name;
    private String email;
    private Long mobileNo;

    public User() {
    }

    public User(String id, String name, String email, Long mobileNo) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(Long mobileNo) {
        this.mobileNo = mobileNo;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobileNo=" + mobileNo +
                '}';
    }
}
