import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class View extends JFrame{
    private MazeGenerator mazeG;
    BufferedImage display;
    
    View(MazeGenerator m){
        this.mazeG=m;
        
        this.setTitle("Maze");
        this.setVisible(true);//fenetre visible pour placer les boutons
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //definir la taille de la fenetre et interdire le changement de taille ensuite
        this.setBounds(0, 0, 800, 600);
        this.setResizable(false);
        this.setLayout(null);
        
        //labyrinthe
        drawMaze();
        drawPerso();
        JPanel mazePanel=new MazePanel();
        mazePanel.setBounds(0, 0, 600, 600);
        this.add(mazePanel);
        
        //chat box
        ChatPanel chatPanel=new ChatPanel(new WrapLayout(WrapLayout.LEFT, 5, 5));
        JScrollPane scrollP=new JScrollPane(chatPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollP.setBounds(600, 0,
                200-scrollP.getVerticalScrollBar().getPreferredSize().width,
                600-View.this.getInsets().top-View.this.getInsets().bottom);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);//vitesse de scroll
        this.add(scrollP);
    }
    
    void drawMaze(){
        display=new BufferedImage(2*mazeG.x+1, 2*mazeG.y+1, BufferedImage.TYPE_INT_RGB);
        
        //mur de droite
        for(int i=0; i<display.getHeight(); i++)
            display.setRGB(display.getWidth()-1, i, Color.BLACK.getRGB());
        //mur d'en bas
        for(int i=0; i<display.getWidth(); i++)
            display.setRGB(i, display.getHeight()-1, Color.BLACK.getRGB());
        
        for (int i=0; i<mazeG.y; i++){
            for (int j=0; j<mazeG.x; j++){
                if((mazeG.maze[j][i] & 1)==0){
                    display.setRGB(2*j, 2*i, Color.BLACK.getRGB());
                    display.setRGB(2*j+1, 2*i, Color.BLACK.getRGB());
                }
                else{
                    display.setRGB(2*j, 2*i, Color.BLACK.getRGB());
                    display.setRGB(2*j+1, 2*i, Color.WHITE.getRGB());
                }
            }
            
            for(int j=0; j<mazeG.x; j++){
                if((mazeG.maze[j][i] & 8)==0){
                    display.setRGB(2*j, 2*i+1, Color.BLACK.getRGB());
                    display.setRGB(2*j+1, 2*i+1, Color.WHITE.getRGB());
                }
                else{
                    display.setRGB(2*j, 2*i+1, Color.WHITE.getRGB());
                    display.setRGB(2*j+1, 2*i+1, Color.WHITE.getRGB());
                }
            }
        }
    }
    
    void resetPerso(){
        for(int i=0; i<display.getWidth(); i++){
            for(int j=0; j<display.getHeight(); j++){
                int toCompare=display.getRGB(i, j);
                if(toCompare!=Color.BLACK.getRGB() && toCompare!=Color.WHITE.getRGB())
                    display.setRGB(i, j, Color.WHITE.getRGB());
            }
        }
    }
    
    void drawPerso(){
        resetPerso();
        for(Pion p : mazeG.getPerso())
            display.setRGB(2*p.getX()+1, 2*p.getY()+1, Color.PINK.getRGB());
    }
    
    
    class MazePanel extends JPanel{
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            double max=(double)(display.getWidth()<display.getHeight()?display.getHeight():display.getWidth());
            int fac=(int)(Math.floor((600-View.this.getInsets().top-View.this.getInsets().bottom)/max));
            g.drawImage(display, 0, 0, display.getWidth()*fac, display.getHeight()*fac, null);
        }
    }
    
    
    class ChatPanel extends JPanel{
        ArrayList<String> moves=new ArrayList<String>();
        
        ChatPanel(WrapLayout w){
            super(w);
            for(int i=0; i<200; i++) moves.add("test"+i+"   ");
        }
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            while(!moves.isEmpty()){
                this.add(new JLabel(moves.remove(0)));
                /*FontMetrics metric=g.getFontMetrics();
                String toWrite=moves.remove(0);
                g.drawString(toWrite, 0, 10);*/
            }
        }
    }
}