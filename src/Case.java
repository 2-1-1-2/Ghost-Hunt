import java.awt.Color;

public class Case{
    private boolean isWall;
    private int nbPlayers=0;
    private Item item=null;
    private Ghost ghost=null;
    
    Case(boolean isWall){
        this.isWall=isWall;
    }

    boolean isWall(){
        return this.isWall;
    }
    
    /* EN RAPPORT AVEC LE JOUEUR */
    void addPlayer(){
        this.nbPlayers++;
    }
    
    void removePlayer(){
        this.nbPlayers--;
    }
    
    boolean havePlayer(){
        return this.nbPlayers>0;
    }
    
    
    /* EN RAPPORT AVEC LES FANTOMES */
    void addGhost(Ghost ghost){
        this.ghost=ghost;
    }

    //TODO: on suppose un fantome par case ?
    void removeGhost(){
        this.ghost=null;
    }

    boolean hasGhost(){
        return this.ghost!=null;
    }
    
    
    /* EN RAPPORT AVEC LES ITEMS */
    void dropItem(Item item){
        this.item=item;
    }

    void takeItem(){
        this.item=null;
    }

    boolean hasItem(){
        return this.item!=null;
    }
    
    
    /* EN RAPPORT AVEC LA PARTIE GRAPHIQUE */
    //TODO: item=dessin en plus sur la case et non couleur ?
    int getColor(){
        if(isWall()) return Color.BLACK.getRGB();
        if(hasGhost()) return Color.BLUE.darker().getRGB();
        if(havePlayer()) return Color.PINK.getRGB();
        return Color.WHITE.getRGB();
    }
}   