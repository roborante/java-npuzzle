package rbm.npuzzle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

/**
 *
 * @author Ro
 * 09/06/2026
 * 
 */
public class GUI {
    public  static nPuzzle puzzle;
    private static int     size  = 4;
    private static final JFrame       main  = new JFrame      ( );
    private static final JLayeredPane board = new JLayeredPane( );
    private static final JLabel       heur  = new JLabel      ( );
    private static int     tileSize;
    private static boolean wait  = false; 
    private static boolean goal  = false; 

    public static void main(String[ ] args) {
        JFrame.setDefaultLookAndFeelDecorated(false);
        puzzle = new nPuzzle(getSize());
        main.setTitle("simple nPuzzle by robramo"); 
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        main.setLayout   (new BorderLayout(       ));
        main.setResizable(false);

        JMenuBar  menuBar = new JMenuBar (          );
        JMenu     game    = new JMenu    ("Game"    ); 
        JMenuItem shuffle = new JMenuItem("Shuffle" ); 
        JMenu     tiles   = new JMenu    ("Tiles"   ); 
        JMenuItem eigth   = new JMenuItem("Eigth"   ); 
        JMenuItem fifteen = new JMenuItem("Fifteen" ); 
        JMenu     solve   = new JMenu    ("Solution"); 
        JMenuItem find    = new JMenuItem("Find"    ); 
        menuBar.add(game );
        menuBar.add(tiles);
        menuBar.add(solve);
        menuBar.setPreferredSize(new Dimension(0,22));
        JPanel status  = new JPanel( );
        status.setBorder(new BevelBorder(       BevelBorder.LOWERED));
        status.setLayout(new BoxLayout  (status,BoxLayout  .X_AXIS ));
        status.setPreferredSize(new Dimension(0,22));
        heur  .setAlignmentY((float)(0.89));
        status.add(heur);
        GridBagConstraints pos = new GridBagConstraints( );
        pos.anchor = GridBagConstraints.CENTER;
        pos.fill   = GridBagConstraints.BOTH;
        pos.insets     = new Insets(0,0,0,0);
        pos.gridwidth  = 1;
        pos.gridheight = 1;
        pos.weightx    = 1;
        pos.weighty    = 1;
        pos.gridx      = 0;
        pos.gridy      = 0;
        JPanel back = new JPanel(new GridBagLayout( ));
        board.setPreferredSize(new Dimension(396, 396));
        board.setVisible(true);
        back .setVisible(true);
        back .add  (board,pos);
        setBoard( );
	
        shuffle.addActionListener((ActionEvent ev) -> {
            if(!wait) {
                puzzle = new nPuzzle(getSize());
                board.removeAll( );
                setBoard( );
            }
        });

        eigth.addActionListener((ActionEvent ev) -> {
            if(getSize() != 3 && !wait) {
                setSize(3);
                puzzle = new nPuzzle(getSize());
                board.removeAll( );
                setBoard( );
            }
        });

        fifteen.addActionListener((ActionEvent ev) -> {
            if(getSize() != 4 && !wait) {
                setSize(4);
                puzzle = new nPuzzle(getSize());
                board.removeAll( );
                setBoard( );
            }
        });

        find.addActionListener((ActionEvent ev) -> {
            if(!wait && !goal) {
                wait = true;
                new Thread( ) {
                    @Override
                    public void run( ) {
                        Vector<Byte> solution = getSolution( );
                        int counter = 0;
                        int blankX  = 0;
                        int blankY  = 0;
                        for(byte x=0;x<getSize();++x) {
                            for(byte y=0;y<getSize();++y) {
                                if(puzzle.board.get(x).get(y) == 0) {
                                    blankX = y*tileSize;
                                    blankY = x*tileSize;
                                    break;
                                }
                            }
                        }
                        
                        for(Byte next : solution) {
                            heur.setText(" Executing solution: " + (solution.size( )-counter) + " to goal");
                            
                            for(Component binga : board.getComponents( )) {
                                if(Integer.parseInt(binga.getName( )) == next.intValue( )) {
                                    int tileX = binga.getX( );
                                    int tileY = binga.getY( );
                                    
                                    if     (tileX < blankX) {
                                        slide((JButton) binga,"right",false);
                                    } else if(tileY > blankY) {
                                        slide((JButton) binga,"up"   ,false);
                                    } else if(tileX > blankX) {
                                        slide((JButton) binga,"left" ,false);
                                    } else if(tileY < blankY) {
                                        slide((JButton) binga,"down" ,false);
                                    } 
                                    
                                    blankX = tileX;
                                    blankY = tileY;
                                    break;
                                }
                            }
                            try {
                                Thread.sleep(764);
                            } catch (InterruptedException ex) {}
                            
                            ++counter;
                        }
                        isGoal(0);
                    }
                }
                .start( );
            }
        });

        game .add(shuffle);
        tiles.add(eigth  );
        tiles.add(fifteen);
        solve.add(find   );
        main.setJMenuBar(menuBar);
        main.add(back  ,BorderLayout.CENTER);
        main.add(status,BorderLayout.SOUTH );
        main.pack( );
        main.setLocationRelativeTo(null);
        main.setVisible(true);
    }

    private static void setBoard( ) {
        tileSize = 396/getSize();

        for(int x=0;x<getSize();++x) { 
            for(int y=0;y<getSize();++y) {
                JLabel back = new JLabel( );
                back.setBackground(new Color(0x29,0x29,0x29));
                back.setBounds    (x*tileSize,y*tileSize,tileSize,tileSize);

                try { back.setIcon(new ImageIcon(ImageIO.read(ClassLoader
                        .getSystemResourceAsStream("tiles/0.gif"))
                        .getScaledInstance(tileSize,tileSize,Image.SCALE_SMOOTH)));
                } catch(IOException ex) {}

                back.setOpaque(true);
                board.add(back,JLayeredPane.DEFAULT_LAYER);

                if(puzzle.board.get(x).get(y) != 0) {
                    JButton tile = new JButton( );
                    
                    try { 
                        tile.setIcon(new ImageIcon(ImageIO.read(ClassLoader
                                .getSystemResourceAsStream("tiles/"+Integer.toString(puzzle.board.get(x).get(y))+".gif"))
                                .getScaledInstance(tileSize,tileSize,Image.SCALE_SMOOTH)));
                    }
                    catch(IOException ex) {}
                    
                    board.add(tile,JLayeredPane.MODAL_LAYER);
                    tile.setBounds(y*tileSize,x*tileSize,tileSize,tileSize);
                    tile.setBackground(           new Color(0x29,0x29,0x29));
                    tile.setBorder(new LineBorder(new Color(0x29,0x29,0x29)));
                    tile.setName(Integer.toString(puzzle.board.get(x).get(y)));

                    tile.addMouseListener(new java.awt.event.MouseAdapter( ) {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent ev) {
                            if(!wait && !goal) {
                                int j = (tile.getX( )+11)/tileSize;
                                int k = (tile.getY( )+11)/tileSize;
                                if((k >      0 && puzzle.board.get(k-1).get(j) == 0)
                                        || (j >      0 && puzzle.board.get(k).get(j-1) == 0)
                                        || (k < getSize()-1 && puzzle.board.get(k+1).get(j) == 0)
                                        || (j < getSize()-1 && puzzle.board.get(k).get(j+1) == 0)) {
                                    tile.setBorder(new LineBorder(new Color(189,0,0),1));
                                }
                            }
                        }
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent ev) {
                            tile.setBorder(new LineBorder(new Color(0x29,0x29,0x29)));
                        }
                    });

                    tile.addActionListener((ActionEvent ev) -> {
                        if(!wait && !goal) {
                            int n = Integer.parseInt(tile.getName( ));
                            int j = (tile.getX( )+11)/tileSize;
                            int k = (tile.getY( )+11)/tileSize;
                            
                            if(k > 0 && puzzle.board.get(k-1).get(j) == 0) {
                                puzzle.board.get(k  ).set(j,(byte)0);
                                puzzle.board.get(k-1).set(j,(byte)n); slide(tile,"up",true); 
                            }
                            else if(j > 0 && puzzle.board.get(k).get(j-1) == 0) {
                                puzzle.board.get(k).set(j  ,(byte)0);
                                puzzle.board.get(k).set(j-1,(byte)n); slide(tile,"left",true); 
                            }
                            else if(k < getSize()-1 && puzzle.board.get(k+1).get(j) == 0) {
                                puzzle.board.get(k  ).set(j,(byte)0);
                                puzzle.board.get(k+1).set(j,(byte)n); slide(tile,"down",true); 
                            }
                            else if(j < getSize()-1 && puzzle.board.get(k).get(j+1) == 0) {
                                puzzle.board.get(k).set(j  ,(byte)0);
                                puzzle.board.get(k).set(j+1,(byte)n); slide(tile,"right",true); 
                            }
                            else {
                                return;
                            }
                            
                            int heuristic = puzzle.heuristic(puzzle.board);
                            switch(heuristic) {
                                case  0 -> isGoal(236);
                                case  1 -> heur.setText(" Heuristic -> taxicab + linear conflicts : 1 move");
                                default -> heur.setText(" Heuristic -> taxicab + linear conflicts : ~" + heuristic + " moves");
                            }
                        }
                    });
                }
            }
        }
        heur.setText (" Heuristic -> taxicab + linear conflicts : ~" + puzzle.heuristic(puzzle.board) + " moves");
        main.validate( );
        main.repaint ( );
        goal = false;
    }

    private static void slide(JButton tile, String towards, boolean manual) {
        if(manual) {
            wait = true;
        }
        new Thread( ) {
            @Override
            public void run( ){
                int shift = 0;
                int x = tile.getX( ); 
                int y = tile.getY( );

                do {
                    if(null != towards) 
                        switch (towards) {
                            case "up"    -> tile.setBounds(x,y-shift,tileSize,tileSize); 
                            case "left"  -> tile.setBounds(x-shift,y,tileSize,tileSize); 
                            case "down"  -> tile.setBounds(x,y+shift,tileSize,tileSize); 
                            case "right" -> tile.setBounds(x+shift,y,tileSize,tileSize); 
                            default -> { }
                        }
                    board.repaint( );
                    try { 
                        Thread.sleep(1); 
                    }  
                    catch (InterruptedException ex) {}
                    ++shift;
                }
                while(shift != tileSize+1);
            }
        }
        .start( );

        if(manual) {
            wait = false;
        }
    }

    private static Vector<Byte> getSolution( )
    {
        heur.setText(" Searching optimal solution with IDA*...");
        return(puzzle.idaStar( ));
    }

    private static void isGoal(int ms) {
        goal = true;
        heur.setText(" Goal"); 

        new Thread( ) {
            @Override
            public void run( ) {
                try {
                    Thread .sleep(ms);
                    JButton star = new JButton( );
                    star.setIcon(new ImageIcon(ImageIO.read(ClassLoader
                    .getSystemResourceAsStream("tiles/star.gif")) 
                    .getScaledInstance(tileSize,tileSize,Image.SCALE_SMOOTH)));
                    star.setBorder(new LineBorder(new Color(0x29,0x29,0x29)));
                    board.add(star,JLayeredPane.MODAL_LAYER);
                    star.setBackground(new Color(0x29,0x29,0x29));
                    int xy = (getSize()-1)*tileSize;
                    int appear = 0;
                    do {
                        star.setBounds(xy-appear+tileSize/2,xy-appear+tileSize/2,appear*2,appear*2);
                        board.repaint( );
                        Thread.sleep (2);
                        ++appear;
                    }
                    while(tileSize > appear*2);
                } 
                catch (InterruptedException | IOException ex) {}
                wait = false;
            }
        }
        .start( );
    }

    public static int getSize() {
        return size;
    }

    public static void setSize(int aSize) {
        size = aSize;
    }
}