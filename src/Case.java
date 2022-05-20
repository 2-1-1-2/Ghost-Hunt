import java.awt.Color;
import java.util.LinkedList;

public class Case{
    private boolean isWall;
    private LinkedList<Player> players=new LinkedList<Player>();
    private Item item=null; //on suppose un item par case
    private Ghost ghost=null; //on suppose un fantome par case
    
    Case(boolean isWall){
        this.isWall=isWall;
    }

    boolean isWall(){
        return this.isWall;
    }
    
    /* EN RAPPORT AVEC LE JOUEUR */
    boolean hasPlayer(){
        return !this.players.isEmpty();
    }
    
    void addPlayer(Player p){
        this.players.add(p);
    }
    
    void removePlayer(Player p){
        this.players.remove(p);
    }
    
    
    /* EN RAPPORT AVEC LES FANTOMES */
    boolean hasGhost(){
        return this.ghost!=null;
    }
    
    void setGhost(Ghost ghost){
        this.ghost=ghost;
    }
    
    Ghost removeGhost(){
        Ghost tmp=ghost;
        this.ghost=null;
        return tmp;
    }

    Ghost catchGhost(Player p){
        if(this.ghost==null) return null;
        p.catchGhost(ghost);
        return removeGhost();
    }
    
    
    /* EN RAPPORT AVEC LES ITEMS */
    boolean hasItem(){
        return this.item!=null;
    }

    boolean getItem(Player p){
        if(!this.hasItem() || !p.getItem(item)) return false;
        this.item=null;
        return true;
    }
    
    void dropItem(Item item){
        this.item=item;
    }

    LinkedList<Ghost> attack(){
        LinkedList<Ghost> res=new LinkedList<Ghost>();
        Ghost tmp;
        for(Player p: this.players){
            tmp=p.loseGhost();
            if(tmp!=null) res.add(tmp);
        }
        return res;
    }
    
    Item stole(){
        Item item;
        for(Player p: players){
            item=p.dropItem();
            if(item!=null) return item;
        }
        return null;
    }
    
    
    /* EN RAPPORT AVEC LA PARTIE GRAPHIQUE */
    int getColor(){
        if(isWall()) return Color.BLACK.getRGB();
        if(hasGhost()) return Color.BLUE.darker().getRGB();
        if(hasPlayer()) return Color.PINK.getRGB();
        if(hasItem()){
            if(item.isBombe()) return Color.RED.darker().getRGB();
            if(item.isLampe()) return Color.RED.brighter().brighter().brighter().getRGB();
            if(item.isRadar()) return Color.RED.getRGB();
        }
        return Color.WHITE.getRGB();
    }
}   