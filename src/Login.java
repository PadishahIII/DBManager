import static org.junit.Assert.*;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.rmi.ServerException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Decoder;

public class Login extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/education_admins";
    static final String USER = "root";
    static final String PASS = "914075";
    static final String PRIV_KEY = "MIICXAIBAAKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4Awhe" +
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
    private Connection conn = null;
    private Statement stmt = null;

    public Login() {
        init_mysql();
    }

    protected void init_mysql() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServerException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>bbbbb</title></head>");
        out.println("</html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            Base64.Decoder base64dec = Base64.getDecoder();
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            String payload_raw = URLDecoder.decode(request.getParameter("payload"), "UTF-8");
            String payload_json_enc = base64dec.decode(payload_raw)
                    .toString();
            String mac_raw = URLDecoder.decode(request.getParameter("mac"), "UTF-8");
            String mac = base64dec.decode(mac_raw).toString();
            String key_raw = URLDecoder.decode(request.getParameter("key"), "UTF-8");
            String key = base64dec.decode(key_raw).toString();
            String iv_raw = URLDecoder.decode(request.getParameter("iv"), "UTF-8");
            String iv = base64dec.decode(iv_raw).toString();
            byte[] mac_cal = md.digest((key_raw + iv_raw + payload_raw).getBytes());
            //验证完整性
            assertArrayEquals(mac_cal, mac_raw.getBytes());
            //RSA解密
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(PRIV_KEY.getBytes());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate((pkcs8EncodedKeySpec));
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] key_dec = cipher.doFinal(key.getBytes());

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
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String str = "123456";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] data = md.digest(str.getBytes());
        String hex = bytes2HexString(data);
        System.out.println((hex));
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "SELECT password from admininfo where username='admin';";
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                //int id = res.getInt(("id"));
                String password = res.getString("password");
                byte[] password_src = hexString2Bytes(password);
                assertArrayEquals(password_src, data);
            }
            res.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
    * @Title:bytes2HexString
    * @Description:字节数组转16进制字符串
    * @param b
    * 字节数组
    * @return 16进制字符串
    * @throws
    */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(String.format("%02X", b[i]));
        }
        return result.toString();
    }

    /**
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     * @param src
     * 16进制字符串
     * @return 字节数组
     * @throws
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

}
