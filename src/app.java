
import MyProtocol.MyProtocol;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class app extends HttpServlet {
    public app() throws Exception {
        MyProtocol mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServerException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>bbbbb</title></head>");
        out.println("</html>");
    }
}
