public class Item{
    private int type; //0=bombe ; 1=lampe ; 2=radar

    Item(int type){
        this.type=type;
    }
    
    boolean isBombe(){ //enleve premier ghost de l'adversaire touche
        return this.type==0;
    }
    
    boolean isLampe(){ //verifie s'il y a des murs aux 4 coins
        return this.type==1;
    }
    
    boolean isRadar(){ //verifie s'il y a des ghosts dans un rayon de 2 cases
        return this.type==2;
    }
    
    String checkItem(){
        switch(type){
            case 0: return "BOMBE***";
            case 1: return "LAMPE***";
            case 2: return "RADAR***";
            default: return "NOITM***";
        }
    }
}