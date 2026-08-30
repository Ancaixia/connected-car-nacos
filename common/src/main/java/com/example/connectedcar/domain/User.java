package com.example.connectedcar.domain;

/** 登录用户（在 auth 微服务与 query 网关间传递）。 */
public class User {

    private String username;
    private String vin;
    private String name;
    private String role;

    public User() {
    }

    public User(String username, String vin, String name, String role) {
        this.username = username;
        this.vin = vin;
        this.name = name;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
