
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import MyProtocol.MyProtocol;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Login extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/education_admins";
    static final String USER = "root";
    static final String PASS = "914075";
    private Connection conn = null;
    private Statement stmt = null;
    private MyProtocol mp;

    public Login() throws Exception {
        init_mysql();
        mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
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

            System.out.println("payload:" + request.getParameter("payload"));
            System.out.println("key:" + request.getParameter("key"));
            System.out.println("iv:" + request.getParameter("iv"));
            System.out.println("mac:" + request.getParameter("mac"));
            String payload_json = mp.decode(request.getParameter("payload"), request.getParameter("key"),
                    request.getParameter("iv"), request.getParameter("mac"));
            System.out.println(payload_json);
            out.println(payload_json);

        } catch (Exception e) {
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
                //assertArrayEquals(password_src, data);
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
