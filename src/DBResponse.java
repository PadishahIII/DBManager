import jakarta.servlet.http.HttpServlet;

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

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.PreparedStatement;

import MyProtocol.MyProtocol;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DBResponse extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/education";
    static final String USER = "root";
    static final String PASS = "914075";
    private Connection conn = null;
    private Statement stmt = null;
    //private MyProtocol mp;

    public DBResponse() {
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

        String[] uStrings = request.getRequestURI().split("/");
        String op = uStrings[uStrings.length - 1];
        if (op.equals("getTables")) {
            //处理申请表名的请求
            /**
            * 返回内容：
            * {
            *  "num":数量
            *  "tblnames":"tbl1;tbl2;..."
            *  "colnames":"col1_1,col1_2..;col2_1,col2_2..."
            * }
            */
            try {
                JSONObject res = new JSONObject();
                StringBuffer tblnames = new StringBuffer();
                StringBuffer colnames = new StringBuffer();
                String sql = "show tables;";
                ResultSet sql_res = stmt.executeQuery(sql);
                int num = 0;
                while (sql_res.next()) {
                    String tbl_name = sql_res.getString(1);
                    tblnames.append(tbl_name);
                    tblnames.append(";");
                    num++;
                }
                String[] tblStrings = tblnames.toString().split(";");
                for (String str : tblStrings) {
                    if (!str.isEmpty()) {
                        String col_sql = "select column_name from information_schema.columns where table_schema='education' and table_name='"
                                + str + "';";
                        ResultSet col_sql_res = stmt.executeQuery(col_sql);
                        while (col_sql_res.next()) {
                            String col_name = col_sql_res.getString(1);
                            colnames.append(col_name);
                            colnames.append(",");
                        }
                        colnames.deleteCharAt(colnames.length() - 1);
                        colnames.append(";");
                    }
                }
                res.put("num", num);
                res.put("tblnames", tblnames.toString());
                res.put("colnames", colnames.toString());

                out.println(res.toJSONString());

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } //if url
        else if (op.equals("queryAllData")) {
            /**
            * 查询指定表的所有元组
            * 返回结果：
            * {
            *  "num":9   //元组个数
            *  "col1":"aa;bb;cc;"
            *  "col2":"dd;ee;"
            *  ...
            * }
            */
            try {
                String tblname = request.getParameter("tblname");
                JSONObject res = new JSONObject();

                String sql = "select * from " + tblname;
                ResultSet sql_res = stmt.executeQuery(sql);
                while (sql_res.next()) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
}
