package com.jewel.dbmanager.model;

import com.jewel.dbmanager.DB;

/**
 * Created by Jewel on 5/12/2016.
 */
public class MPerson {

    @DB(key = DB.KEY.PRIMARY)
    private int id;
    private int roll;
    private int age;
    private String name;
    private String phone;
    private String test;
    private String des;

    public MPerson() {
    }

    public MPerson(String name, String phone, int roll, int age, String des) {
        this.name = name;
        this.phone = phone;
        this.roll = roll;
        this.age = age;
        this.des = des;

    }

    public MPerson(String name, String phone, int roll, String test) {
        this.name = name;
        this.phone = phone;
        this.roll = roll;
        this.test = test;

    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
