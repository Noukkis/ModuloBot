/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 *
 * @author Jordan
 */
public class Linker {

    int port;
    ServerSocket serverSocket;
    Key key;
    CryptedSocket cryptedSocket;

    public Linker(int port) throws Exception {
        this.port = port;
        serverSocket = new ServerSocket(port);
        key = (Key) new ObjectInputStream(new FileInputStream("serv.key")).readObject();
    }

    public boolean tryToConnect(boolean isNew) {
        close();
        try {            
            cryptedSocket = new CryptedSocket(serverSocket.accept(), key);
            println(isNew ? "new" : "old");
            println("Connected");
            return true;
        } catch (IOException | GeneralSecurityException ex) {
            close();
            return false;
        }
    }

    public String readLine() {
        String msg = cryptedSocket.readLine();
        while(msg == null){
            boolean ok;
            do {
                 ok = tryToConnect(false);
            } while(!ok);
            msg = cryptedSocket.readLine();
        }
        return msg;
    }

    public void println(String str) {
        for (String line : str.split("\n")) {
            cryptedSocket.println(line);
        }
    }

    public void close() {
        try {
            if(cryptedSocket != null){
                cryptedSocket.getSocket().close();
            }
        } catch (IOException ex) {
        }
    }
}
