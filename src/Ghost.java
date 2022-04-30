public class Ghost{
    private int row, col;
    private int points;
    private boolean wasCatched=false;

    //generer une position random a partir du labyrinthe
    //verifier si la case generee en random n'a ni joueur ni mur
    Ghost(int row, int col, int points){
        this.row=row;
        this.col=col;
        this.points=points;
    }
    
    int getPoints(){
        return this.points;
    }
    
    //TODO: la fonction qui appelle hadCatch() doit distribuer les points au joueur
    synchronized int hadCatch(){
        if(!this.wasCatched){
            this.wasCatched=true;
            return this.points;
        }
        return 0;
    }
}