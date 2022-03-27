public class Pion {
    private String pseudo;
    private int x;
    private int y;

    public Pion(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int get_x(){
        return this.x;
    }

    public int get_y(){
        return this.y;
    }

    public void set_x(int x){
        this.x = x;
    }

    public void set_y(int y){
        this.y = y;
    }
    
}
