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
    boolean[] addPlayer(Player p){ //0=ghost ; 1=item ; 2=bombe explosee
        boolean[] res=new boolean[3];
        res[1]=this.getItem(p); //recupere l'item de la case si possible
        if(!this.hasPlayer()) res[0]=this.catchGhost(p);
        else{
            if((res[2]=!p.noItem() && p.hasBombe())){ //attaque avec la bombe
                for(Player victim : this.players) p.attackPlayer(victim);
                removeBombe(p);
                res[1]|=this.getItem(p); //recupere l'item de la case si possible
            }
            if(p.noItem()) //vole l'item du premier joueur qui en a un
                for(Player victim : players)
                    if((res[1]|=p.stoleItem(victim.dropItem()))) break;
        }
        this.players.add(p);
        return res;
    }
    
    void removePlayer(Player p){
        this.players.remove(p);
    }
    
    boolean hasPlayer(){
        return !this.players.isEmpty();
    }
    
    
    /* EN RAPPORT AVEC LES FANTOMES */
    void setGhost(Ghost ghost){
        this.ghost=ghost;
    }
    
    void removeGhost(){
        this.ghost=null;
    }

    boolean catchGhost(Player p){
        if(this.ghost==null) return false;
        p.catchGhost(ghost);
        removeGhost();
        return true;
    }

    boolean hasGhost(){
        return this.ghost!=null;
    }
    
    
    /* EN RAPPORT AVEC LES ITEMS */
    void dropItem(Item item){
        this.item=item;
    }

    boolean getItem(Player p){
        if(!this.hasItem() || !p.getItem(item)) return false;
        this.item=null;
        return true;
    }
    
    void removeBombe(Player p){
        this.item=null;
        p.removeBombe();
    }

    boolean hasItem(){
        return this.item!=null;
    }
    
    
    /* EN RAPPORT AVEC LA PARTIE GRAPHIQUE */
    //TODO: item=dessin en plus sur la case et non couleur ?
    int getColor(){
        if(isWall()) return Color.BLACK.getRGB();
        if(hasGhost()) return Color.BLUE.darker().getRGB();
        if(hasPlayer()) return Color.PINK.getRGB();
        return Color.WHITE.getRGB();
    }
}   