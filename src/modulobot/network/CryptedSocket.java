
package modulobot.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;

/**
 *
 * @author vesyj
 */
public class CryptedSocket {

    Cipher encryptCipher;
    Cipher decryptCipher;
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;

    public CryptedSocket(Socket socket, Key rsaKey) throws IOException, GeneralSecurityException {
        this.socket = socket;
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        Cipher rsaCipher = Cipher.getInstance("RSA");

        //Create the encrypt Cipher, initialize it with a key and send the key to the other side
        encryptCipher = Cipher.getInstance("AES");
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        Key encryptAesKey = keygen.generateKey();
        rsaCipher.init(Cipher.WRAP_MODE, rsaKey);
        String ciphered = new String(Base64.getEncoder().encode(rsaCipher.wrap(encryptAesKey)), "UTF-8");
        writer.println(ciphered);
        writer.flush();
        encryptCipher.init(Cipher.ENCRYPT_MODE, encryptAesKey);
        
        //Create the decrypt Cipher and initialize it with the key received by the other side
        decryptCipher = Cipher.getInstance("AES");
        rsaCipher.init(Cipher.UNWRAP_MODE, rsaKey);
        Key key = rsaCipher.unwrap(Base64.getDecoder().decode(reader.readLine().getBytes("UTF-8")), "AES", Cipher.SECRET_KEY);
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    public boolean println(String str) {
        boolean res = false;
        try {
            byte[] bytes = encryptCipher.doFinal(str.getBytes("UTF-8"));
            writer.println(new String(Base64.getEncoder().encode(bytes), "UTF-8"));
            writer.flush();
        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
        }
        return res;
    }

    public String readLine() {
        String res = null;
        try {
            byte[] bytes = decryptCipher.doFinal(Base64.getDecoder().decode(reader.readLine().getBytes("UTF-8")));
            res = new String(bytes, "UTF-8");
        } catch (IOException | IllegalBlockSizeException | BadPaddingException ex) {
        }
        return res;
    }

    public Socket getSocket() {
        return socket;
    }
}
