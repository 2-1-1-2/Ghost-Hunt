public class Player{
    private String username;//"id" dans le sujet
    private int score=0, nbGhostCaught=0;//le deuxieme a voir si c'est utile
    private int row=-1, col=-1;
    private boolean startOk=false;
    private Item item=null;

    Player(String username){
        this.username=username;
    }

    public String toStrinG(){
        return "PLAYR "+username+"***";
    }
    
    String getID(){
        return this.username;
    }

    /* AVANT QUE LA PARTIE COMMENCE */
    boolean sentStart(){
        return startOk;
    }

    void setStartStatus(boolean startOk){
        this.startOk=startOk;
    }
    
    void initializePosition(int row, int col){
        this.row=row;
        this.col=col;
    }

    /* PENDANT LA PARTIE */
    void catchGhost(Ghost ghost){
        this.score+=ghost.getPoints();
        this.nbGhostCaught++;
    }
    
    int getScore(){
        return this.score;
    }
    
    int getNbGhostsCaught(){
        return this.nbGhostCaught;
    }

    /* FONCTIONS DE DEPLACEMENT */
    int getRow(){
        return this.row;
    }
    
    int getCol(){
        return this.col;
    }
    
    int moveUp(){
        return --this.row;
    }
    
    int moveRight(){
        return ++this.col;
    }
    
    int moveDown(){
        return ++this.row;
    }
    
    int moveLeft(){
        return --this.col;
    }
    /* FIN FONCTIONS DE DEPLACEMENT */
}