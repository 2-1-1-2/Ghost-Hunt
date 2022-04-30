import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class View extends JFrame{
    private Game game;
    private BufferedImage display;
    private JPanel mazePanel=new MazePanel();
    
    View(Game game){
        this.game=game;
        
        this.setTitle("GhostHunt");
        this.setVisible(true);//fenetre visible pour placer les boutons
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);//fermeture du programme avec la fenetre
        
        //definir la taille de la fenetre et interdire le changement de taille ensuite
        this.setBounds(0, 0, 800, 600);
        this.setResizable(false);
        this.setLayout(null);
        
        //labyrinthe
        drawMaze(game.getMazeColor(), game.getHeight(), game.getWidth());
        mazePanel.setBounds(0, 0, 600, 600);
        this.add(mazePanel);
        
        
        //TODO: chatbox
        //chatbox
        /*ChatPanel chatPanel=new ChatPanel(new WrapLayout(WrapLayout.LEFT, 5, 5));
        JScrollPane scrollP=new JScrollPane(chatPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollP.setBounds(600, 0,
                200-scrollP.getVerticalScrollBar().getPreferredSize().width,
                600-scrollP.getHorizontalScrollBar().getPreferredSize().height-View.this.getInsets().top);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);//vitesse de scroll
        this.add(scrollP);*/
    }
    
    void drawMaze(int[][] mazeColor, int height, int width){
        display=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int i=0; i<height; i++)
            for(int j=0; j<width; j++)
                display.setRGB(j, i, mazeColor[i][j]);
    }
    
    //TODO: introduire un peu d'animation pour que le deplacement ne soit pas trop brusque
    void refresh(int row, int col, int color){
        display.setRGB(col, row, color);
        this.mazePanel.repaint();
    }
    
    
    class MazePanel extends JPanel{
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            double max=(double)(display.getWidth()<display.getHeight()?display.getHeight():display.getWidth());
            int a=(int)(Math.floor((600-View.this.getInsets().top-View.this.getInsets().bottom)/max));
            g.drawImage(display, 0, 0, display.getWidth()*a, display.getHeight()*a, null);
        }
    }
    
    
    class ChatPanel extends JPanel{
        ArrayList<String> moves=new ArrayList<String>();
        
        ChatPanel(WrapLayout w){
            super(w);
            for(int i=0; i<150; i++) moves.add("joueur n°1 : on ne peut pas aller à gauche");
        }
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            while(!moves.isEmpty()){
                JPanel tmp=new JPanel(new FlowLayout());
                JLabel toAdd=new JLabel(moves.remove(0));
                toAdd.setPreferredSize(new Dimension(200, 50));
                tmp.add(toAdd);
                this.add(tmp, 0);
            }
        }
    }
}