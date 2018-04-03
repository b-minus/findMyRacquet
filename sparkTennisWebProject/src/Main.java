import static spark.Spark.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;


public class Main {
    private static final String JDBC_Driver = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/racquetsdb";
    private static final String user = "me";
    private static final String pw = "OSLaker8397!";

    private static double rating = 0.0;
    private static String type = "none";

    private static String sqlString = "";
    private static Racquet found = null;

    private static String output = "";

    private static FavRacquets mine = new FavRacquets();
    private static AllRacquets all;

    //get the Type based on search
    static String getType(String uri){
        String t = "";
        if (uri.endsWith("Control")) {
            t = "control";
        } else if(uri.endsWith("Power")) {
            t = "power";
        } else{
            t = "error";
        }
        return t;
    }

    //get the Rating based on search
    static String getRating(String newuri){
        String ratingStr = "";
        if(newuri.endsWith(".0") || newuri.endsWith(".5")){
            ratingStr = newuri.substring(newuri.length()-3, newuri.length());
        } else{
            ratingStr = "error";
        }
        return ratingStr;
    }

    //get table from database based on search
    static String connectToDB() {
        Connection conn = null;
        Statement stmt = null;

        try {
            //set up database connection
            Class.forName(JDBC_Driver);

            conn = DriverManager.getConnection(DB_URL, user, pw);
            stmt = conn.createStatement();

            String sqlStr = "";
            if (rating == 0.0 && type.equals("none")) {
                sqlStr = "select * from racquets";
            } else if (rating == 0.0) {
                sqlStr = "select * from racquets where type = '" + type + "'";
            } else if (type.equals("none")) {
                sqlStr = "select * from racquets where rating = " + rating;
            } else {
                sqlStr = "select * from racquets where type = '" + type + "' AND rating = " + rating;
            }

            //get search from database
            ResultSet rset = stmt.executeQuery(sqlStr);
            all = new AllRacquets();

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

            stmt.close();
            conn.close();
            rset.close();

            return sqlStr;
        }
        //SQL Exception catch
        catch (SQLException se) {
            se.printStackTrace();
            return "error";
        }
        //Class Exception catch
        catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    //get output based on table from database
    static String getOutput(){
        ObjectMapper om = new ObjectMapper();

        //get racquets saved from database into session
        Racquet[] op = all.getArray();
        String output = "";

        //output in JSON
        String out = "[ ";

        try {
            //if search wasn't empty, output first racquet from search
            if (op[0] != null) {
                output = om.writeValueAsString(op[0]);
                out = out + "\n   " + output;
            }

            //output rest of racquets from search
            for (int x = 1; x < 19; x++) {
                if (op[x] != null) {
                    output = om.writeValueAsString(op[x]);
                    out = out + ",\n";
                    out = out + "   " + output;
                }
            }
        }
        catch(JsonProcessingException e){
            return "error";
        }

        //end JSON array
        out = out + "\n]";
        return out;
    }


    public static void main(String[] args) {

        //Controllers
        //Type and Rating Search
        before("/allRacquets/Rating/*/Type/*", (req, res) -> {
            String uri = req.uri();
            type = getType(uri);
            if(type.equals("error")){
                res.status(404);
                output = null;
            }
            else {
                String newuri = uri.substring(0, 23);
                String ratingStr = getRating(newuri);
                if (ratingStr.equals("error")) {
                    res.status(404);
                    output = null;
                } else {
                    rating = Double.valueOf(ratingStr);
                }
            }
        });

        //Rating Search
        before("/allRacquets/Rating/*", (req, res) -> {
            String uri = req.uri();
            String ratingStr = getRating(uri);
            if(ratingStr.equals("error")){
                res.status(404);
                output = null;
            }
            else {
                rating = Double.valueOf(ratingStr);
                type = "none";
            }
        });

        //Type Search
        before("/allRacquets/Type/*", (req, res) -> {
            String uri = req.uri();
            type = getType(uri);
            if(type.equals("error")){
                res.status(404);
                output = null;
            }
            else {
                rating = 0.0;
            }
        });

        //Models
        //Save Racquet
        before("/allRacquets/save", (req, res) -> {
            //read input JSON
            String data = req.body();

            ObjectMapper om = new ObjectMapper();

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

            found = new Racquet(name, rating, type, price);

            //save racquet in session
            mine.addRacquet(found);

            stmt.close();
            conn.close();
            rset.close();
        });

        //All Searches
        before("/allRacquets/*", (req, res) -> {
            String uri = req.uri();
            if(uri.endsWith("allRacquets/")) {
                type = "none";
                rating = 0.0;
            }
            sqlString = connectToDB();
        });

        //View
        //All Searches
        get("/allRacquets/*", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            output = getOutput();
            if(output.equals("error")){
                res.status(500);
                output = null;
            }
            return output;
        });

        //Save a Racquet to Favorites
        post("/allRacquets/save", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(found);
        });

        //Save List of Racqcuets
        get("/myRacquets/*", (req, res) -> {
            ObjectMapper om = new ObjectMapper();

            //output in JSON format
            output = "[";

            //if first spot is filled, output first racquet
            if(mine.getArray()[0] != null){
                String out = om.writeValueAsString(mine.getArray()[0]);
                output = output + "\n   " + out;
            }

            //output rest of favorite racquets if they exist
            for(int x = 1; x < 19; x++){
                if(mine.getArray()[x] != null){
                    String out = om.writeValueAsString(mine.getArray()[x]);
                    output = output + ",\n";
                    output = output + "   " + out;
                }
            }
            //finish JSON array
            output = output + "\n]";
            return output;
        });

        //Delete a Racquet from Saved List
        delete("/myRacquets/*", (req, res) -> {
            //read JSON input
            String data = req.body();

            res.type("application/json; charset=UTF-8");

            ObjectMapper om = new ObjectMapper();

            //create racquet based on input
            Racquet delete = om.readValue(data, Racquet.class);

            //search through favorites list, and delete if it exists
            String output = om.writeValueAsString(mine.deleteRacquet(delete));
            return output;
        });
    }
}