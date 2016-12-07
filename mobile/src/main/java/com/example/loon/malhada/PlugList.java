package com.example.loon.malhada;

/**
 * Created by loon on 2016-11-15.
 */

public class PlugList {
    private String name;
    private String serial;
    private int register;
    private int status;

    public void setName(String name) {this.name = name;}
    public void setSerial(String serial) {this.serial = serial;}
    public void setRegister(int register) {this.register = register;}
    public void setStatus(int status) {this.status = status;}

    public String getName() {return name;}
    public String getSerial() {return serial;}
    public int getRegister() {return register;}
    public int getStatus() {return status;}
}
