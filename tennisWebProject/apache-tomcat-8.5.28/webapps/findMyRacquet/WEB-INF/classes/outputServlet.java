import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class outputServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        ObjectMapper om = new ObjectMapper();

        try {
            //get racquets saved from database into session
            ServletContext context = getServletContext();
            AllRacquets all = (AllRacquets)context.getAttribute("all");
            Racquet[] op = all.getArray();
            String output = "";

            //output in JSON
            out.println("[ ");

            //if search wasn't empty, output first racquet from search
            if(op[0] != null){
                output = om.writeValueAsString(op[0]);
                out.print("   " + output);
            }

            //output rest of racquets from search
            for(int x = 1; x < 19; x++) {
                if(op[x] != null){
                    output = om.writeValueAsString(op[x]);
                    out.println(",");
                    out.print("   " + output);
                }
            }

            //end JSON array
            out.println("\n]");
        } finally {
            out.close();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //take in JSON input
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String input;
        while((input = reader.readLine()) != null){
            builder.append(input);
        }
        //convert JSON to a String
        String data = builder.toString();

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        ObjectMapper om = new ObjectMapper();

        try{
            //get racquet requested from input
            ServletContext context = getServletContext();
            FavRacquets mine = null;
            String output = "";

            Racquet found = (Racquet)context.getAttribute("mine");

            //if no favorite racquets, create list
            if(context.getAttribute("myRacquets") == null) {
                mine = new FavRacquets();
            }
            //if already favorites list exists, get the list from session
            else{
                mine = (FavRacquets)context.getAttribute("myRacquets");
            }

            //add racquet to favorites list
            output = om.writeValueAsString(mine.addRacquet(found));
            out.println(output);

            context.setAttribute("myRacquets", mine);
        }
        finally{
            out.close();
        }
    }

}