public class FavRacquets {
    Racquet[] myRacquets;

    //create empty racquet array for favorites
    public FavRacquets() {
        myRacquets = new Racquet[20];
    }

    //return array of favorite racquets
    public Racquet[] getArray(){
        return myRacquets;
    }

    //add a racquet to favorites list
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

    //delete a racquet from favorites list
    public Racquet deleteRacquet(Racquet notMine) {
        for(int x = 0; x < 20; x++){
            if(myRacquets[x] != null && myRacquets[x].getName().equals(notMine.getName())){
                myRacquets[x] = null;
                return notMine;
            }
        }
        return null;
    }

}