import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class racquetFinderFilter implements Filter {
    ServletContext cont;

    public void init(FilterConfig arg) throws ServletException {
        this.cont = arg.getServletContext();
        this.cont.log("Filter Initialized");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        //figure out type of search
        String newUri = "";
        //check rating search
        if(uri.endsWith(".5") || uri.endsWith(".0")) {
            //find the rating searched
            String ratingStr = uri.substring(uri.length() - 3, uri.length());
            double rating = Double.valueOf(ratingStr);
            ServletContext context = req.getServletContext();
            Comparator comp = new Comparator(rating);
            //save rating to session and set other types of searches to null
            context.setAttribute("rating", comp);
            context.setAttribute("both", null);
            context.setAttribute("type", null);

            //forward to allRacquets servlet to get racquets from database based on search
            RequestDispatcher forw = req.getRequestDispatcher("/allRacquets");
            forw.forward(req, resp);
        }

        //check type search
        else if(uri.endsWith("Control") || uri.endsWith("Power")) {
            String type = "";
            //find the type searched
            if(uri.endsWith("Control")) {
                newUri = uri.substring(0, uri.indexOf('C') - 1);
                type = "control";
            }
            else if(uri.endsWith("Power")) {
                newUri = uri.substring(0, uri.indexOf('P') - 1);
                type = "power";
            }

            //check if both type and rating are searched
            if(newUri.endsWith(".5") || newUri.endsWith(".0")) {
                //figure out rating searched
                String ratingStr = newUri.substring(newUri.length() - 3, newUri.length());
                double rating = Double.valueOf(ratingStr);
                ServletContext context = req.getServletContext();
                Comparator comp = new Comparator(rating, type);
                //save rating and type to session and set other types of searches to null
                context.setAttribute("both", comp);
                context.setAttribute("rating", null);
                context.setAttribute("type", null);
            }
            else{
                //save type to session and set other types of searches to null
                ServletContext context = req.getServletContext();
                Comparator comp = new Comparator(type);
                context.setAttribute("type", comp);
                context.setAttribute("both", null);
                context.setAttribute("rating", null);
            }

            //forward to allRacquets servlet to get racquets from database based on search
            RequestDispatcher forw = req.getRequestDispatcher("/allRacquets");
            forw.forward(req, resp);
        }
        else{
            //if not specific search, set all types of searches to null
            ServletContext context = req.getServletContext();
            context.setAttribute("rating", null);
            context.setAttribute("both", null);
            context.setAttribute("type", null);
            //go to allRacquets servlet and return all racquets
            chain.doFilter(req, resp);
        }
    }

    public void destroy(){}
}
