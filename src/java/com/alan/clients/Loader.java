package com.alan.clients;

public class Loader {
    public static Loader INSTANCE = new Loader();
    public void loadClient(){
        Client.INSTANCE.initRise();
    }
}
