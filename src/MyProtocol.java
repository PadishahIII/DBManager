import static org.junit.Assert.assertArrayEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MyProtocol {
    public MyProtocol() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private String PRIV_KEY = "MIICXAIBAAKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4Awhe" +
            "BDje4RTdbDXqnhZSSS8MtziszfWPhvf1q3SnpkTa7G9+U8p835UG7SQSH6f3mOrJ" +
            "WMHqyYzDyOnNKc/V5am72D6qNgjwlr5FAHj2O3RstdaIcRW+HPuNjNNGqwIDAQAB" +
            "AoGBAK76oKRSGc0+mAd0N8wUoM4SPZZR/y8MkE1o3w7K+tH7z032zffUvpbsq0co" +
            "7JpjkLJQBZqo72r6IsW8EcTHS42rWMCt9uhDQrcJeQMToAY20TazfukIWOOMgLhn" +
            "MRLyL/g4ewocXHq/EXUF2uElFNy3Ti58+RHLUIjU40f8nJOxAkEA1/CldWDcM6M6" +
            "rzA6qvoibars5Epfn+6ExvtdpOQPDo5vkZOFQ3DtLNsyfpQVEsCdqFkpJVkNoIS5" +
            "qcKLrlDxCQJBANUNFSu7fKJbotNe7zL3borhFc65qVMkTUNtKhNgF8YzXFI0RIE5" +
            "Nhx4iPntikWD7ZHpUDBnd7lbS003zupGCxMCQGJi/sAwVaQhZweTDegA99bH3g9V" +
            "46PW5SBUPyJ11nZnZ2YItNs5hJa/eI47oi5dHHgrx5eAr7jHQGCch0/xCSECQA0A" +
            "Cl2ryBQkIVBih5gFjyI8T9dYbuOa4HgPzjR2dZzrf2OoutFjy1B7bmhJvVk2jqWL" +
            "pg/+EEkoL/UbRa337i8CQEvMm8SJOFYnj1YJ9jBWI80GG/3wG28+cwUglUvysypz" +
            "EUyktxfiogP76QBpRf6nG/DL0vDnGg8FnEJP5Yg20v4=";
    private String PUB_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4AwheBDje4RTdbDXqnhZSSS8MtziszfWPhvf1q3SnpkTa7G9+U8p835UG7SQSH6f3mOrJWMHqyYzDyOnNKc/V5am72D6qNgjwlr5FAHj2O3RstdaIcRW+HPuNjNNGqwIDAQAB";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    //RSA密钥长度
    private static final int KEY_SIZE = 1024;
    //AES
    private static final String AES_ALGORI = "AES/CBC/PKCS7Padding";

    public String decode(String payload, String key, String iv, String mac) throws UnsupportedEncodingException {
        try {
            Base64.Decoder base64dec = Base64.getDecoder();
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            String payload_raw = URLDecoder.decode(payload, "UTF-8");
            String payload_json_enc = base64dec.decode(payload_raw)
                    .toString();
            String mac_raw = URLDecoder.decode(mac, "UTF-8");
            String mac_basedec = base64dec.decode(mac_raw).toString();
            String key_raw = URLDecoder.decode(key, "UTF-8");
            String key_basedec = base64dec.decode(key_raw).toString();
            String iv_raw = URLDecoder.decode(iv, "UTF-8");
            String iv_basedec = base64dec.decode(iv_raw).toString();
            byte[] mac_cal = md.digest((key_raw + iv_raw + payload_raw).getBytes());
            //验证完整性
            assertArrayEquals(mac_cal, mac_raw.getBytes());
            //RSA解密
            byte[] key_dec = decryptByPrivateKey(key_basedec.getBytes());
            byte[] iv_dec = decryptByPrivateKey(iv_basedec.getBytes());
            //AES CBC解密
            byte[] payload_json = decryptByAES(payload_json_enc.getBytes(), key_dec, iv_dec);
            return payload_json.toString();
        } catch (NoSuchAlgorithmException ae) {
            ae.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return new String("Error");
        }
    }

    public byte[] decryptByPrivateKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(PRIV_KEY.getBytes());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate((pkcs8EncodedKeySpec));
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] key_dec = cipher.doFinal(data);
        return key_dec;
    }

    public byte[] encryptByPublicKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(PUB_KEY.getBytes());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic((x509EncodedKeySpec));
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] key_dec = cipher.doFinal(data);
        return key_dec;
    }

    public byte[] decryptByAES(byte[] data, byte[] key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(AES_ALGORI);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    //生成指定大小的密钥对
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put("RSAPublicKey", publicKey);
        keyMap.put("RSAPrivateKey", privateKey);
        return keyMap;
    }

    public static byte[] getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPrivateKey");
        return key.getEncoded();//base64
    }

    public static byte[] getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPublicKey");
        return key.getEncoded();
    }

    public byte[] getMyPrivateKey() {
        return PRIV_KEY.getBytes();
    }

    public byte[] getMyPublicKey() {
        return PUB_KEY.getBytes();
    }

    //由文件设置公钥
    public PublicKey loadPublicKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(spec);
        PUB_KEY = publicKey.getEncoded().toString();
        return publicKey;
    }

    public PrivateKey loadPrivateKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);
        PRIV_KEY = privateKey.getEncoded().toString();
        return privateKey;
    }

    public byte[] encryptByAES(byte[] data, byte[] key, byte[] iv)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(AES_ALGORI, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public String getAeString(int len) {
        String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
        int maxpos = chars.length();
        String res = "";
        for (int i = 0; i < len; i++) {
            res += chars.charAt((int) Math.floor(Math.random() * maxpos));
        }
        return res;
    }
}
