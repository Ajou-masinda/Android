package com.example.loon.malhada;

/**
 * Created by loon on 2016-11-14.
 */

public class Plug_Info {
    private  int id;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;
    private int vendor;
    private int status;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public int getVendor() {
        return vendor;
    }

    public void setVendor(int vendor) {
        this.vendor = vendor;
    }

    private int register;
    private  String location;
    private  String name;
    private  String serial;
    public Plug_Info() {
    }
    public Plug_Info(int id, String name, String location, String serial, int status,int type, int vendor, int register){
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
        this.serial=serial;
        this.type = type;
        this.vendor = vendor;
        this.register = register;
    }
    public Plug_Info(String name, String location, String serial, int status,int type, int vendor, int register){
        this.name = name;
        this.location = location;
        this.status = status;
        this.type = type;
        this.serial=serial;
        this.vendor = vendor;
        this.register = register;
    }
    public int getId() {
        return id;
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
    public void setLocation(String location) {
        this.location = location;
    }
    public void setName(String name) {
        this.name = name;
    }
}
