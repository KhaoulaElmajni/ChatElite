package com.example.chatchat;

public class Contacts {
    public String name, status, image ;

    public Contacts(){

    }

    public Contacts(String image,String name, String status) {
        this.image = image;
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}