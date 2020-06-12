package com.example.i3remote;

public class Host {
    String hostname;
    String user;
    String password;

    public Host(String hostname, String user, String password) {
        this.hostname = hostname;
        this.user = user;
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
