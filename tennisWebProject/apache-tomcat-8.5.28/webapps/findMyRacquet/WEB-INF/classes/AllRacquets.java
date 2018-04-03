public class AllRacquets {
    Racquet[] myRacquets;

    //create empty array to load for database output
    public AllRacquets() {
        myRacquets = new Racquet[20];
    }

    //return array for searches
    public Racquet[] getArray(){
        return myRacquets;
    }

    //add racquet from database to array
    public Racquet addRacquet(Racquet mine) {
        int x = 0;
        while (myRacquets[x] != null && x != 20) {
            x++;
        }
        if(x != 20) {
            myRacquets[x] = mine;
            return mine;
        }
        else {
            return null;
        }
    }

}