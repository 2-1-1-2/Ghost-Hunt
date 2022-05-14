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
        int nRow, nCol, limit=0;
        //si aucune case libre autour en 100 essaies, alors teleportation
        do{
            do{
                if(limit>100) nRow=(int)(Math.random()*game.getWidth());
                else nRow=(int)(Math.random()*points*2+2)+row-points;
            }
            while(nRow<0 || nRow>=game.getWidth());
            do{
                if(limit>100) nCol=(int)(Math.random()*game.getHeight());
                else nCol=(int)(Math.random()*points*2+2)+col-points;
            }
            while(nCol<0 || nCol>=game.getHeight());
            limit++;
        }
        while(game.notValidForGhost(nRow, nCol));
        game.moveGhost(this, row, col, nRow, nCol);
        this.row=nRow;
        this.col=nCol;
        //TODO: arranger octets
        //TODO: multi-diffuse toDiffuse
        game.diffuse("GHOST "+row+" "+col+"+++");
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