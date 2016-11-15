package com.example.loon.malhada;

/**
 * Created by loon on 2016-11-14.
 */

public class Plug_Info {
    private  int id, IR, condition;
    private  String location, name, ip;
    public Plug_Info() {
    }
    public Plug_Info(int id, String name, String location, int IR, String ip, int condition){
        this.id = id;
        this.name = name;
        this.location = location;
        this.IR = IR;
        this.ip = ip;
        this.condition = condition;
    }
    public Plug_Info(String name, String location, int IR, String ip, int condition){
        this.name = name;
        this.location = location;
        this.IR = IR;
        this.ip = ip;
        this.condition = condition;
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
    public String getIp() { return ip; }
    public int isCondition() {return condition;}
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
    public void setCondition(int condition) {this.condition = condition;}
    public void setIp(String ip) {this.ip = ip;}
}
