import static org.junit.Assert.*;
import org.junit.Test;
import java.text.ParseException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.ServerException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Decoder;

public class test {
    @Test
    public void URLDecTest() throws UnsupportedEncodingException {
        String raw = URLDecoder.decode(
                "https://cn.bing.com/search?q=java+url_decode&qs=CT&pq=java+url&sk=CT1&sc=6-8&cvid=3CFFF5849FB94738BE3EA3E04569C497&FORM=QBRE&sp=2",
                "UTF-8");
        System.out.println(raw);
    }

    @Test
    public void MyProtocolAESTest() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException {
        MyProtocol mp = new MyProtocol();
        //AES test
        String aes_key = mp.getAeString(32);
        String aes_iv = mp.getAeString(16);
        String data_ini = "powershell";
        byte[] data_enc = mp.encryptByAES(data_ini.getBytes(), aes_key.getBytes(), aes_iv.getBytes());
        byte[] data_dec = mp.decryptByAES(data_enc, aes_key.getBytes(), aes_iv.getBytes());
        assertArrayEquals(data_dec, data_ini.getBytes());

    }

    @Test
    public void MyProtocolRSATest() throws Exception {
        MyProtocol mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
        byte[] data_ini = "powershell".getBytes();
        byte[] data_enc = mp.encryptByPublicKey(data_ini);

        Map<String, Object> keyMap = MyProtocol.initKey();
        byte[] pubKey = MyProtocol.getPublicKey(keyMap);
        System.out.println(pubKey.toString());
        System.out.println(pubKey.length);
        System.out.println(MyProtocol.getPrivateKey(keyMap).length);
        System.out.println(mp.getMyPublicKey().length);
        System.out.println(mp.getMyPrivateKey().length);

        byte[] data_dec = mp.decryptByPrivateKey(data_enc);
        assertArrayEquals(data_dec, data_ini);

    }
}
