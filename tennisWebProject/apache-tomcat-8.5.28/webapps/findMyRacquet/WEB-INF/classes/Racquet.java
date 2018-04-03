public class Racquet {
  String name;
  double rating;
  String type;
  double price;

  public Racquet() {
  }

  public Racquet(String n){
    name = n;
  }

  public Racquet(String n, double r, String t, double p) {
    name = n;
    rating = r;
    type = t;
    price = p;
  }

  public String getName(){
    return name;
  }
  public double getRating(){
    return rating;
  }
  public String getType(){
    return type;
  }
  public double getPrice(){ return price; }

}
