import java.util.LinkedList;

public class Player{
    private String username;//"id" dans le sujet
    private int score=0;
    private int row=-1, col=-1;
    private boolean startOk=false;
    private LinkedList<Ghost> caughtGhosts=new LinkedList<Ghost>();
    private Item item=null;

    Player(String username){
        this.username=username;
    }

    public String toStrinG(){
        return "PLAYR "+username+"***";
    }
    
    //username row col (score)***
    String currentInfo(boolean withScore){
        return username+" "+Server.intToNChar(row, 3)+" "+Server.intToNChar(col, 3)
                +((withScore)?" "+Server.intToNChar(score, 4):"")+"***";
    }
    
    //SCORE username score row col+++
    String currentInfoCatch(){
        return "SCORE "+username+" "+Server.intToNChar(score, 4)+" "
            +Server.intToNChar(row, 3)+" "+Server.intToNChar(col, 3)+"+++";
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
    
    void initialize(int row, int col){
        this.score=0;
        this.row=row;
        this.col=col;
        setStartStatus(false);
        caughtGhosts.clear();
    }
    
    
    /* EN RAPPORT AVEC LES FANTOMES */
    void catchGhost(Ghost ghost){
        this.score+=ghost.catchGhost();
        this.caughtGhosts.add(ghost);
    }
    
    //perd le premier ghost suite a une attaque
    Ghost loseGhost(){
        if(this.caughtGhosts.isEmpty()) return null;
        Ghost res=caughtGhosts.remove();
        this.score-=res.loseGhost(this.row, this.col);
        return res;
    }
    
    
    /* EN RAPPORT AVEC LES ITEMS */
    boolean noItem(){
        return this.item==null;
    }
    
    //si a deja un item, renvoie false
    boolean getItem(Item item){
        if(!noItem()) return false;
        this.item=item;
        return true;
    }
    
    boolean stoleItem(Item item){
        if(item==null) return false;
        this.item=item;
        return true;
    }
    
    Item dropItem(){
        if(noItem()) return null;
        Item tmp=this.item;
        this.item=null;
        return tmp;
    }
    
    String checkItem(){
        if(noItem()) return "NOITM***";
        return this.item.checkItem();
    }
    
    boolean hasBombe(){
        return item.isBombe();
    }
    
    boolean hasLampe(){
        return item.isLampe();
    }
    
    boolean hasRadar(){
        return item.isRadar();
    }
    
    
    /* EN RAPPORT AVEC LE SCORE */
    int getScore(){
        return this.score;
    }
    
    int getNbGhostsCaught(){
        return this.caughtGhosts.size();
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