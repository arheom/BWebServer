package org.bwebserver;

public class ServerStartup {
    public static void main(String[] args){
        try {
            BWebServer.startServer();
        }catch (Exception ex){
            // TODO: proper exception handling
            System.err.println(ex.getMessage());
            try {
                BWebServer.stopServer();
            } catch (Exception e) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
