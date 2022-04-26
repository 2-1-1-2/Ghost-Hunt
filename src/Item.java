public class Item{
    private int row, col;
    //TODO : augmenter la liste d'items 
    private int type; //0=bombe ; 1=lampe

    //generer une position random a partir du labyrinthe
    //verifier si la case generee en random n'a ni joueur ni mur
    Item(Case[][] maze, int type){
        do{
            row=(int)(Math.random()*maze.length);
            col=(int)(Math.random()*maze[0].length);
        }
        while(maze[row][col].isWall() || maze[row][col].havePlayer());
        maze[row][col].dropItem(this);
        
        this.type=type;
    }
    
    int getType(){
        return this.type;
    }
}