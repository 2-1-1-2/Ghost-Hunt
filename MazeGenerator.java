import java.util.Collections;
import java.util.Random;


import java.util.Arrays;
 
/*
 * source algo labyrinthe : random.nextInt(20-10) + 10; 
 */
public class MazeGenerator {
	private final int x;
	private final int y;
	private final int[][] maze;
    /*
    private int p.get_x();
    private int p.get_y();
    */
    private Pion[] perso;


	public MazeGenerator(int x, int y, int nb_pion) {
		this.x = x;
		this.y = y;
		maze = new int[this.x][this.y];
		generateMaze(0, 0);
        perso  = new Pion[nb_pion];
        for (int i = 0; i < nb_pion; i++) {
            Random random = new Random();
            perso[i] = new Pion(random.nextInt(x), random.nextInt(y));
            System.out.println("coordonnées perso n°"+i+" : ["+perso[i].get_x()+" ; "+perso[i].get_y()+"]");
            
        }
        
	}
 
	public void display() {
		for (int i = 0; i < y; i++) {
			// draw the north edge
			for (int j = 0; j < x; j++) {
				System.out.print((maze[j][i] & 1) == 0 ? "+---" : "+   ");
			}
			System.out.println("+");
			// draw the west edge
			for (int j = 0; j < x; j++) {
				System.out.print((maze[j][i] & 8) == 0 ? "| " : "  ");
                String pion_aff = "  ";
                for (int k = 0; k < perso.length; k++) {
                    if (perso[k].get_x() == j && perso[k].get_y() == i) {
                        if(pion_aff == "  "){
                            pion_aff = k+" ";
                        }
                        else{
                            pion_aff = "* ";
                            break;
                        }
                        
                    }
                    
                }
                
				System.out.print(pion_aff);
				//System.out.print( ( p.get_x() == j && p.get_y() == i)? "1 " : "  ");
				//System.out.print( ( (maze[j][i] & 8) == 0 && !(p.get_x() == j && p.get_y() == i))? "  " : "");
                
			}
			System.out.println("|");
		}
		// draw the bottom line
		for (int j = 0; j < x; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}

    public void go_up(Pion p){
        if (!((maze[p.get_x()][p.get_y()] & 1) == 0)){
            p.set_y(p.get_y()-1);
            System.out.println("up");
        }
        else{
            System.out.println("on ne peut pas monter");
        }
    }

    

    public void go_down(Pion p){
        if (p.get_y() < y - 1 && !((maze[p.get_x()][p.get_y()+1] & 1) == 0)){
            p.set_y(p.get_y()+1);
            System.out.println("down");
        }
        else{

            System.out.println("on ne peut pas down");
        }
    }

    

    public void go_left(Pion p){
        if (p.get_x() > 0 && !((maze[p.get_x()][p.get_y()] & 8) == 0)){
            p.set_x(p.get_x()-1);
            System.out.println("left");
        }
        else{

            System.out.println("on ne peut pas aller à gauche");
        }
    }

    

    public void go_right(Pion p){
        if (p.get_x() < x - 1 && !((maze[p.get_x()+1][p.get_y()] & 8) == 0)){
            p.set_x(p.get_x()+1);
            System.out.println("right");
        }
        else{

            System.out.println("on ne peut pas aller à droite");
        }
    }

    
 
	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y)
					&& (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}
 
	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}
 
	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;
 
		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}
 
		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};
 
	public static void main(String[] args) {
		int x = args.length >= 1 ? (Integer.parseInt(args[0])) : 8;
		int y = args.length == 2 ? (Integer.parseInt(args[1])) : 8;

        
        Random r = new Random();
        

        x = r.nextInt(20-10) + 10;
        y = r.nextInt(20-10) + 10;
        System.out.println("taille du labyrinthe :"+x+"x"+y);
		MazeGenerator maze = new MazeGenerator(x, y, 5);

        for (int i = 0; i < 20; i++) {
            
            for (int j = 0; j < maze.perso.length; j++) {
                Random random = new Random();
                int n = random.nextInt(4);
                switch (n) {
                    case 0: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_up(maze.perso[j]);
                        break;
                    case 1: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_down(maze.perso[j]);
                        break;
                    case 2: 
                        System.out.print("joueur n°"+j+" : ");
                        
                        maze.go_left(maze.perso[j]);
                        break;
                    case 3: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_right(maze.perso[j]);
                        break;
                }
                
            }
            
            maze.display();
            
        }
	}
 
}