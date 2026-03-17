package dev.tmmc.ulms.objects.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Librarian {
    private int id;
    private String name;
    private String email;
    private String staff_id;
    @JsonIgnore
    private String password_hash;

    public Librarian() {}

    public Librarian(int id, String name, String email, String staff_id, String password_hash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.staff_id = staff_id;
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

    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
}
