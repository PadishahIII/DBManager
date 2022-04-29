import jakarta.servlet.http.HttpServlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.PreparedStatement;

import org.apache.tomcat.util.http.FastHttpDateFormat;

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
    static final String LogPath = "D:\\Tomcat\\webapps\\DBManager\\log\\sqlLog.txt";
    private Connection conn = null;
    private Statement stmt = null;
    //private MyProtocol mp;
    private PrintStream log;

    public DBResponse() throws FileNotFoundException {
        init_mysql();
        File logfile = new File(LogPath);
        log = new PrintStream(logfile);
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
                List<String> tblnameList = getTableNames();
                int num = tblnameList.size();
                for (String str : tblnameList) {
                    tblnames.append(str);
                    tblnames.append(";");
                }
                tblnames.deleteCharAt(tblnames.length() - 1);

                for (String str : tblnameList) {
                    if (!str.isEmpty()) {
                        List<String> colnameList = getColumnNames(str);
                        for (String colname : colnameList) {
                            colnames.append(colname);
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
            *  "0":"aa;bb;cc;"
            *  "1":"dd;ee;ff;"
            *  ...
            * }
            */
            try {
                String tblname = request.getParameter("tblname");
                System.out.println("query: " + tblname);
                JSONObject res = new JSONObject();

                List<String> dataList = queryAllData(tblname);
                res.put("num", dataList.size());
                Integer index = 0;
                for (String i : dataList) {
                    res.put(index.toString(), i);
                    index++;
                }
                out.println(res.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String[] uStrings = request.getRequestURI().split("/");
        String op = uStrings[uStrings.length - 1];
        if (op.equals("insert")) {
            //向指定表插入一条数据
            try {
                String tblname = request.getParameter("tblname");
                Map<String, String> data_map = new HashMap<>();
                Map<String, String[]> temp_map = request.getParameterMap();
                for (String key : temp_map.keySet()) {
                    data_map.put(key, temp_map.get(key)[0]);
                }

                if (!insertIntoTable(tblname, data_map, out)) {
                    out.println("插入失败，数据格式有误");
                    return;
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out.println("插入失败，数据格式有误");
            } //try
        } //if url
    }

    /**
     * 获取所有表的名字
     * @return
     * @throws SQLException
     */
    public List<String> getTableNames() throws SQLException {
        List<String> tblnameList = new ArrayList<>();
        String sql = "show tables;";
        ResultSet sql_res = stmt.executeQuery(sql);
        while (sql_res.next()) {
            String tbl_name = sql_res.getString(1);
            if (!tbl_name.isEmpty())
                tblnameList.add(tbl_name);
        }
        writeLog(sql);
        return tblnameList;
    }

    /**
     * 获取指定表名的所有列名
     * @param tblname
     * @return
     * @throws SQLException
     */
    public List<String> getColumnNames(String tblname) throws SQLException {
        List<String> colnameList = new ArrayList<>();
        String sql = "select column_name from information_schema.columns where table_schema='education' and table_name=?";
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setObject(1, tblname);
        ResultSet col_sql_res = pstmt.executeQuery();
        while (col_sql_res.next()) {
            String col_name = col_sql_res.getString(1);
            if (!col_name.isEmpty()) {
                colnameList.add(col_name);
            }
        }
        writeLog(sql);
        return colnameList;
    }

    /**
     * 查询指定表的所有元组
     * @param tblname
     * @return
     * @throws SQLException
     */
    public List<String> queryAllData(String tblname) throws SQLException {
        List<String> dataList = new ArrayList<>();
        int colnum = getColumnNames(tblname).size();

        String sql = "select * from " + tblname;
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        //pstmt.setString(1, tblname);
        ResultSet sql_res = pstmt.executeQuery(sql);

        StringBuffer line = new StringBuffer();
        while (sql_res.next()) {
            for (int j = 1; j <= colnum; j++) {
                String coldata = sql_res.getString(j);
                line.append(coldata);
                line.append(";");
            }
            line.deleteCharAt(line.length() - 1);
            dataList.add(line.toString());
        }
        writeLog(sql);
        return dataList;
    }

    public boolean insertIntoTable(String tblname, Map<String, String> data_map, PrintWriter out) throws SQLException {
        if (!getTableNames().contains(tblname)) {
            out.println("插入失败，没有指定表名的相关数据:" + tblname);
            return false;
        }
        List<String> colList = getColumnNames(tblname);
        StringBuffer col_strs = new StringBuffer();
        StringBuffer val_strs = new StringBuffer();
        col_strs.append("(");
        val_strs.append("('");
        for (String colname : colList) {
            String value = data_map.get(colname);
            if (value == null || value.isEmpty()) {
                out.println("插入失败，缺少列:" + colname);
                return false;
            }
            col_strs.append(colname + ",");
            val_strs.append(value + "','");
        }
        col_strs.deleteCharAt(col_strs.length() - 1);
        val_strs.deleteCharAt(val_strs.length() - 1);
        val_strs.deleteCharAt(val_strs.length() - 1);

        col_strs.append(")");
        val_strs.append(")");

        String sql = "insert into " + tblname + " " + col_strs + " values " + val_strs;
        int lineno = stmt.executeUpdate(sql);
        if (lineno != 1) {
            out.println("插入失败，数据格式有误");
            return false;
        }
        out.println("插入成功!");

        writeLog(sql);
        return true;
    }

    private void writeLog(String data) {
        Timestamp tm = new Timestamp(new Date().getTime());
        log.println("[" + tm.toString() + "]:" + data);
    }
}
