import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class myRacquetsServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    ObjectMapper om = new ObjectMapper();

    try{
      //get favorite racquets array from session
      ServletContext context = getServletContext();
      FavRacquets favs = (FavRacquets)context.getAttribute("myRacquets");
      Racquet[] mine = favs.getArray();

      //output in JSON format
      out.println("[");

      //if first spot is filled, output first racquet
      if(mine[0] != null){
        String output = om.writeValueAsString(mine[0]);
        out.print("   " + output);
      }

      //output rest of favorite racquets if they exist
      for(int x = 1; x < 19; x++){
        if(mine[x] != null){
          String output = om.writeValueAsString(mine[x]);
          out.println(",");
          out.print("   " + output);
        }
      }
      //finish JSON array
      out.println("\n]");
    }
    finally{
      out.close();
    }
  }

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    //take in JSON input
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = request.getReader();
    String input;
    while((input = reader.readLine()) != null) {
      builder.append(input);
    }
    //set input as a String
    String data = builder.toString();

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    ObjectMapper om = new ObjectMapper();

    try{
      //create racquet based on input
      Racquet delete = om.readValue(data, Racquet.class);

      //search through favorites list, and delete if it exists
      ServletContext context = getServletContext();
      FavRacquets mine = (FavRacquets) context.getAttribute("myRacquets");
      String output = om.writeValueAsString(mine.deleteRacquet(delete));
      out.println(output);
      context.setAttribute("myRacquets", mine);
    }
    finally{
      out.close();
    }
  }

}
