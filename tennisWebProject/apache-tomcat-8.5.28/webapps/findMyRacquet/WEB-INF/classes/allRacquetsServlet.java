import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class allRacquetsServlet extends HttpServlet {
    static final String JDBC_Driver = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/racquetsdb";
    static final String user = "me";
    static final String pw = "OSLaker8397!";

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    ObjectMapper om = new ObjectMapper();
    Connection conn = null;
    Statement stmt = null;

    try{
        //set up database connection
        Class.forName(JDBC_Driver);

        conn = DriverManager.getConnection(DB_URL, user, pw);
        stmt = conn.createStatement();

        //find racquets based on rating search
        ServletContext context = getServletContext();
        Comparator comp = (Comparator)context.getAttribute("rating");

        String sqlStr = "";

        if(comp != null) {
            //search database based on rating
            sqlStr = "select * from racquets where rating = " + comp.getRating();
        }
        else {
            //find racquets based on rating and type search
            comp = (Comparator)context.getAttribute("both");
            if(comp != null) {
                //search database based on rating and type
                sqlStr = "select * from racquets where rating = " + comp.getRating() + " AND type = '" + comp.getType() + "'";
            }
            else {
                //find racquets based on type search
                comp = (Comparator)context.getAttribute("type");
                if(comp != null) {
                    //search database basde on type
                    sqlStr = "select * from racquets where type = '" + comp.getType() + "'";
                }
                else{
                    //if no specific search, get all racquets from database
                    sqlStr = "select * from racquets";
                }
            }
        }

        //get search from database
        ResultSet rset = stmt.executeQuery(sqlStr);

        AllRacquets all = new AllRacquets();

        //for each racquet in search
        while (rset.next()) {
            String name = rset.getString("name");
            double rating = rset.getDouble("rating");
            String type = rset.getString("type");
            double price = rset.getDouble("price");

            //create racquet instance and add to array for output
            Racquet current = new Racquet(name, rating, type, price);
            all.addRacquet(current);
        }
        //save array in session
        context.setAttribute("all", all);

        stmt.close();
        conn.close();
        rset.close();

        //send to output servlet
        RequestDispatcher forw = request.getRequestDispatcher("/output");
        forw.forward(request, response);
    }
    //SQL Exception catch
    catch(SQLException se){
        se.printStackTrace();
        out.close();
    }
    //Class Exception catch
    catch(Exception e){
        e.printStackTrace();
        out.close();
    }
    finally{
        out.close();
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    //set up to read input JSON
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = request.getReader();
    String input;
    while((input = reader.readLine()) != null){
        builder.append(input);
    }
    //build input as a String
    String data = builder.toString();

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    ObjectMapper om = new ObjectMapper();

    try{
        //create racquet based on input
        Racquet rac = om.readValue(data, Racquet.class);

        //set up database connection
        Class.forName(JDBC_Driver);

        Connection conn = null;
        Statement stmt = null;

        conn = DriverManager.getConnection(DB_URL, user, pw);
        stmt = conn.createStatement();

        //search for racquets with the same name
        String sqlStr = "select * from racquets where name = '" + rac.getName() + "'";
        ResultSet rset = stmt.executeQuery(sqlStr);

        rset.next();

        //create racquet from search information
        String name = rset.getString("name");
        double rating = rset.getDouble("rating");
        String type = rset.getString("type");
        double price = rset.getDouble("price");

        Racquet found = new Racquet(name, rating, type, price);

        //save racquet in session
        ServletContext context = getServletContext();
        context.setAttribute("mine", found);

        stmt.close();
        conn.close();
        rset.close();

        //send to output servlet
        RequestDispatcher forw = request.getRequestDispatcher("/output");
        forw.forward(request, response);
    }
    //catch SQL exceptions
    catch(SQLException se){
        se.printStackTrace();
        out.close();
    }
    //catch class exceptions
    catch(Exception e) {
        e.printStackTrace();
        out.close();
    }
    finally{
        out.close();
    }
  }

}
