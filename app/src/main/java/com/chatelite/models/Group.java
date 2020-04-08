package com.chatelite.models;

public class Group {
    private String name;
    private String photo;
    private String admin;

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Group() {

    }

    public Group(String name, String photo) {
        this.name = name;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }



    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setName(String name) {
        this.name = name;
    }


}
