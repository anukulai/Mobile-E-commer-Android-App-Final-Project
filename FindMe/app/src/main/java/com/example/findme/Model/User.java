package com.example.findme.Model;

public class User {
    public String email,name,phone,circlename,userId, code;
    //   private int image;

    public User(){}



    public User(String email, String name, String phone,String circlename,String userId,String code){
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.circlename = circlename;
        this.userId = userId;
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {

        return code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCirclename() {
        return circlename;
    }

    public void setCirclename(String circlename) {
        this.circlename = circlename;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }


    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
