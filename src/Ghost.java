public class Ghost{
    private int row, col;
    private int points;

    //generer une position random a partir du labyrinthe
    //verifier si la case generee en random n'a ni joueur ni mur
    Ghost(Case[][] maze, int points){
        do{
            row=(int)(Math.random()*maze.length);
            col=(int)(Math.random()*maze[0].length);
        }
        while(maze[row][col].isWall() || maze[row][col].havePlayer());
        maze[row][col].addGhost(this);
        
        this.points=points;
    }
    
    int getPoints(){
        return this.points;
    }
}