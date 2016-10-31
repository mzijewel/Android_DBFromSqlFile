package com.jewel.dbfromsql.model;

/**
 * Created by Jewel on 5/12/2016.
 */
public class MPerson {
    private int id;
    private String name;
    private String phone;

    public MPerson() {
    }

    public MPerson(String name, String phone) {
        this.name = name;
        this.phone = phone;
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
