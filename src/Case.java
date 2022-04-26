public class Case{
    private boolean isWall;
    private int nbPlayers=0;
    private Item item=null;
    private Ghost ghost=null;
    private boolean needRefresh=false;
    
    Case(boolean isWall){
        this.isWall=isWall;
    }

    boolean isWall(){
        return this.isWall;
    }
    
    
    
    //le joueur p se rajoute dans la case lui-meme : p appelle case[][].addPlayer()
    void addPlayer(){
        this.needRefresh=true;
        this.nbPlayers++;
    }
    
    void removePlayer(){
        this.needRefresh=true;
        this.nbPlayers--;
    }
    
    boolean havePlayer(){
        return this.nbPlayers>0;
    }
    
    
    
    void addGhost(Ghost ghost){
        this.needRefresh=true;
        this.ghost=ghost;
    }

    void removeGhost(){
        this.needRefresh=true;
        this.ghost=null;
    }

    boolean hasGhost(){
        return this.ghost!=null;
    }
    
    

    void dropItem(Item item){
        this.needRefresh=true;
        this.item=item;
    }

    void takeItem(){
        this.needRefresh=true;
        this.item=null;
    }

    boolean hasItem(){
        return this.item!=null;
    }
}   