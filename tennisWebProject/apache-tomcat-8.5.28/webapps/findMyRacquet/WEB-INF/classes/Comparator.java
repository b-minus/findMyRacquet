public class Comparator {
    double rating;
    String type;

    public Comparator() {
    }

    public Comparator(double r) {
        rating = r;
    }

    public Comparator(String t){
        type = t;
    }

    public Comparator(double r, String t) {
        rating = r;
        type = t;
    }

    public double getRating() { return rating; }
    public String getType() { return type; }
}