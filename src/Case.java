public class Case{
    int type;//0=vide, 1=mur, 2=joueur
    int nbPlayers;//peut etre utile
    Item item;
    Ghost ghost;

    Case(){}//case vide

    Case(int type){
        this.type=type;
    }

    Case(Item item){
        this.item=item;
    }

    boolean isWall(){
        return this.type==1;
    }

    void removeGhost(){
        this.ghost=null;
    }

    boolean hasGhost(){
        return this.ghost!=null;
    }

    void takeItem(){
        this.item=null;
    }

    void dropItem(Item item){
        this.item=item;
    }

    boolean hasItem(){
        return this.item!=null;
    }
}   