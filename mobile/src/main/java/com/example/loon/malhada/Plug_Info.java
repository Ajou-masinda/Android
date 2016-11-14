package com.example.loon.malhada;

/**
 * Created by loon on 2016-11-14.
 */

public class Plug_Info {
    private  int id, IR;
    private  String location, name;

    public Plug_Info() {
    }
    public Plug_Info(int id, String name, String location, int IR){
        this.id = id;
        this.name = name;
        this.location = location;
        this.IR = IR;
    }
    public Plug_Info(String name, String location, int IR){
        this.name = name;
        this.location = location;
        this.IR = IR;
    }
    public int getId() {
        return id;
    }
    public int getIR() {
        return IR;
    }
    public String getLocation() {
        return location;
    }
    public String getName() {
        return name;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setIR(int IR) {
        this.IR = IR;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setName(String name) {
        this.name = name;
    }
}
