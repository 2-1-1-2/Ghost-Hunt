import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class View extends JFrame{
    private MazePanel mazePanel;
    private ChatPanel chatPanel=new ChatPanel();
    
    View(Game game, int height, int width){
        this.setTitle("GhostHunt");
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        //labyrinthe
        mazePanel=new MazePanel(game.getMazeColor(), height, width);
        this.add(mazePanel);
        
        //chat box
        JScrollPane scrollP=new JScrollPane(chatPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollP.getVerticalScrollBar().setUnitIncrement(12);
        scrollP.setBorder(BorderFactory.createLineBorder(new Color(122, 195, 225), 3));
        this.add(scrollP);
        
        //calcul la taille de la fenetre en fonction de celle du labyrinthe
        calculBounds(height, width, 300, 600, scrollP);
        this.setLayout(null);
    }
    
    void calculBounds(int height, int width, int scrollW, int maxS, JScrollPane scrollP){
        double max=(double)(width<height?height:width);
        int coeff=(int)(Math.floor((maxS-View.this.getInsets().top-View.this.getInsets().bottom)/max));
        int mazeW=width*coeff, mazeH=height*coeff;
        
        mazePanel.setBounds(0, 0, mazeW, mazeH);
        scrollP.setBounds(mazeW, 0, scrollW, mazeH);
        this.setBounds(0, 0, mazeW+scrollW+getInsets().left+getInsets().right,
                             mazeH+getInsets().top+getInsets().bottom);
    }
    
    
    /* FONCTIONS DE DESSIN DU LABYRINTHE */
    class MazePanel extends JPanel{
        private BufferedImage display;
        
        MazePanel(int[][] mazeColor, int height, int width){
            super();
            
            //initilisation du dessin du labyrinthe
            display=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int i=0; i<height; i++)
                for(int j=0; j<width; j++)
                    display.setRGB(j, i, mazeColor[i][j]);
        }

        //TODO: introduire un peu d'animation pour que le deplacement ne soit pas trop brusque
        void refreshMaze(int row, int col, int color){
            display.setRGB(col, row, color);
            this.repaint();
        }
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(display, 0, 0, getWidth(), getHeight(), null);
        }
    }
    
    void refreshMaze(int row, int col, int color){
        mazePanel.refreshMaze(row, col, color);
    }
    
    
    /* FONCTIONS DE DESSIN DU CHAT BOX */
    class ChatPanel extends JPanel{
        String entete="<html>";
        JLabel chatHistory=new JLabel(entete+"</html>");
        boolean bottom=false;
        
        ChatPanel(){
            super(new FlowLayout(0, 5, 3));
            this.chatHistory.setFont(new Font("Times new roman", Font.BOLD, 14));
            this.add(chatHistory);
        }
        
        void addText(String text, String color){
            chatHistory.setText(entete+"<p style=\"color:"+color+";\">"+text+"</p>"+chatHistory.getText().substring(entete.length()));
            this.repaint();
        }
    }
    
    synchronized void addText(String text, String color){
        this.chatPanel.addText(text, color);
    }
}