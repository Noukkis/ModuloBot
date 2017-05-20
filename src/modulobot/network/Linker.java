/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.network;

import botLib.CryptedSocket;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

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
        String[] keyString = new BufferedReader(new InputStreamReader(new FileInputStream("serv.key"))).readLine().split("\\|");
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(keyString[0]), new BigInteger(keyString[1]));
        key = KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public boolean tryToConnect(boolean isNew) {
        close();
        try {
            cryptedSocket = new CryptedSocket(serverSocket.accept(), key);
            println(isNew ? "new" : "old");
            println("Connected");
            return true;
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
            close();
            return false;
        }
    }

    public String readLine() {
        String msg = cryptedSocket.readLine();
        while (msg == null) {
            boolean ok;
            do {
                ok = tryToConnect(false);
            } while (!ok);
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
            if (cryptedSocket != null) {
                cryptedSocket.getSocket().close();
            }
        } catch (IOException ex) {
        }
    }
}
