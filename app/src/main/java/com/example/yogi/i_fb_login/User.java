package com.example.yogi.i_fb_login;

/**
 * Created by YOGI on 19-07-2018.
 */

public class User {
    private String image_url;
    private String name;
    private String email;
    private String location;

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User(){}

    public User(String url,String name,String email,String location)
    {
        this.image_url = url;
        this.name = name;
        this.email = email;
        this.location = location;
    }


}
