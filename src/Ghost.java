public class Ghost implements Runnable{
    private int row, col;
    private int points;
    private boolean wasCaught=false;
    private Game game;

    Ghost(int row, int col, int points, Game game){
        this.row=row;
        this.col=col;
        this.points=points;
        this.game=game;
    }
    
    synchronized int catchGhost(){
        if(!this.wasCaught){
            this.wasCaught=true;
            this.row=-1;
            this.col=-1;
            return this.points;
        }
        return 0;
    }
    
    synchronized int loseGhost(int row, int col){
        if(this.wasCaught){
            this.wasCaught=false;
            this.row=row;
            this.col=col;
            this.moveGhost();
            return this.points;
        }
        return 0;
    }
    
    void moveGhost(){
        int[] newPos=game.moveGhost(this, this.row, this.col, this.points);
        this.row=newPos[0];
        this.col=newPos[1];
    }
    
    public void run(){
        while(true){
            try{
                Thread.sleep((10-this.points)*1000);
                if(!this.wasCaught) this.moveGhost();
            }
            catch(Exception e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }
}