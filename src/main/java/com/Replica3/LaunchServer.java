package com.Replica3;

public class LaunchServer {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            try {
                AllServerTemplate AtwaterServer = new AllServerTemplate("ATW");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                AllServerTemplate VerdunServer = new AllServerTemplate("VER");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                AllServerTemplate OutremontServer = new AllServerTemplate("OUT");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }
}
