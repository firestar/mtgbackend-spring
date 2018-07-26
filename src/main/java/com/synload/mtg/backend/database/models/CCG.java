package com.synload.mtg.backend.database.models;

public class CCG {
    private String name;
    public CCG(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
