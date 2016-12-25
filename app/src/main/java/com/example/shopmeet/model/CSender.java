package com.example.shopmeet.model;

import java.io.Serializable;

public class CSender implements Serializable {
    String id, name, email;
 
    public CSender() {
    }
 
    public CSender(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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
}
