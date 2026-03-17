package dev.tmmc.ulms.objects.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Student {
    private int id;
    private String name;
    private String email;
    private String university_id;
    private String phone;
    @JsonIgnore
    private String password_hash;

    public Student() {}

    public Student(int id, String name, String email, String university_id, String phone, String password_hash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.university_id = university_id;
        this.phone = phone;
        this.password_hash = password_hash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getUniversity_id() {
        return university_id;
    }

    public void setUniversity_id(String university_id) {
        this.university_id = university_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
}
