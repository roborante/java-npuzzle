package rbm.npuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.IntStream;

/**
 *
 * @author Ro
 * 21/03/2026
 * 
 */
class nPuzzle {
    private static boolean solvable(int perm[ ], boolean custom) {
        byte length = (byte)perm.length;
        if(custom) {
            int chk[ ] = Arrays.copyOf(perm,length); Arrays.sort(chk);
            switch(length) {
                case  9 -> {
                    if(!Arrays.equals(chk,IntStream.rangeClosed(0, 8).toArray( ))) {
                        return false;
                    }
                }
                case 16 -> {
                    if(!Arrays.equals(chk,IntStream.rangeClosed(0,15).toArray( ))) {
                        return false;
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        byte inversions = 0;
        for(byte x=0;x<length-1;++x) {
            if(perm[x] > 1) {
                for(byte y=(byte)(x+1);y<length;++y) {
                    if(perm[x] > perm[y] && perm[y] > 0) {
                        ++inversions;
                    }
                }
            }
        }
        return length % 2 != 0 ? inversions % 2 == 0 : blankEven(perm)
                ? inversions % 2 != 0 : inversions % 2 == 0;
    }
    private static boolean blankEven(int perm[ ]) {
        byte z = 0;
        for(byte x=0;x<4;++x) {
            for(byte y=0;y<4;++y) {
                if(perm[z] == 0) {
                    return x % 2 == 0;
                } 
                ++z;
            }
        }
        return false;
    }
    private static Vector<Byte> buildPath(Vector<Node> path) {
        Vector<Byte> moves = new Vector<>( );
        
        for(int step=1; step < path.size( ); ++step) {
            moves.add(path.get(step).move);
        }
        
        return moves;
    }
    private byte size;
    public  ArrayList<ArrayList<Byte>> board = new ArrayList<>( );
    private ArrayList<ArrayList<Byte>> goal  = new ArrayList<>( );


    nPuzzle(int squareOf) { 
        if(squareOf < 3 
        || squareOf > 4) {
            return;
        }
        this.size = (byte)squareOf;
        List<Integer> temp = new ArrayList<>( );
        for(int x=0;x<squareOf*squareOf;++x) {
            temp.add(x);
        }
        do {
            Collections.shuffle(temp,new Random(System.nanoTime( )));
        } while(!solvable(temp.stream( ).mapToInt(y->y).toArray( ),false));
        setBoard(temp.stream( ).mapToInt(z->z).toArray( ));
    }

    private void setBoard(int tiles[ ]) {
        byte z = 0;
        byte target = 1;
        int  reset  = this.size*this.size;

        for(byte k=0;k<reset;++k) {
            this.goal.add(new ArrayList<>( ));
        }

        for(byte x=0;x<this.size;++x) {
            this.board.add(new ArrayList<>( ));

            for(byte y=0;y<this.size;++y) {
                this.board.get(x).add((byte)(tiles[z]));
                ++z;
                this.goal.get(target).add(x);
                this.goal.get(target).add(y);
                target++;
                if(target == reset) {
                    target = 0;
                }
            }
        }
    }

    private byte[ ] blank_pos( ) {
        byte[ ] xy = {-1,-1};

        for(byte x=0;x<this.size;++x) {
            for(byte y=0;y<this.size;++y) {
                if(this.board.get(x).get(y) == 0) {
                    xy[0] = x;
                    xy[1] = y;
                    break;
                }
            }
        }
        return xy;
    }
    
    public Vector<Byte> idaStar( ) {
        Node toSolve  = new Node(this.board);
        byte[ ] blank = blank_pos( );
        toSolve.move  = -1;
        toSolve.x = blank[0];
        toSolve.y = blank[1];

        Vector<Byte> result = new Vector<>( );
        Vector<Node> path   = new Vector<>( ); 
        path.add(toSolve);

        byte bound = heuristic(this.board);
        do {
            byte[ ]  deeper ={127};
            result = findPath(path,(byte)1,bound,deeper);
            bound  = deeper[0];
        }
        while(result.isEmpty( ));
        
        return result;
    }
    
    private Vector<Byte> findPath(Vector<Node> path, byte depth, byte bound, byte[ ] deeper) {
        for(Node next : expand(path.lastElement( ))) {
            byte heur = heuristic(next.state);

            switch(heur) {
                case  0 -> {
                    path.add(next); return buildPath(path); // goal
                }
                default -> heur += depth;
            }
            if(heur <= bound) {
                path.add(next);
                Vector<Byte> result = findPath(path,(byte)(depth+1),bound,deeper);
                if(!result.isEmpty()) {
                    return result; // goal
                }
                path.remove(path.size( )-1);
            }
            else if(heur < deeper[0]) {
                deeper[0] = heur;
            }
        }
        
        return new Vector< >( );
    }

    private Vector<Node> expand(Node parent) {
        Vector<Node> children = new Vector<>( );

        if(parent.x > 0 && parent.state.get(parent.x-1).get(parent.y) != parent.move) { // slide down
            Node child = new Node(parent.state);
            child.x = (byte)(parent.x-1);
            child.y = parent.y;
            child.move = child.state.get(child.x).get(parent.y);
            child.state.get(parent.x).set(parent.y,child.move);
            child.state.get(child.x ).set(parent.y,(byte)0);
            children.add(child);
        }
        if(parent.y > 0 && parent.state.get(parent.x).get(parent.y-1) != parent.move) { // slide right
            Node child = new Node(parent.state);
            child.y = (byte)(parent.y-1);
            child.x = parent.x;
            child.move = child.state.get(parent.x).get(child.y);
            child.state.get(parent.x).set(parent.y,child.move);
            child.state.get(parent.x).set(child.y,(byte)0);
            children.add(child);
        }
        if(parent.x < this.size-1 && parent.state.get(parent.x+1).get(parent.y) != parent.move) { // slide up
            Node child = new Node(parent.state);
            child.x = (byte)(parent.x+1);
            child.y = parent.y;
            child.move = child.state.get(child.x).get(parent.y);
            child.state.get(parent.x).set(parent.y,child.move);
            child.state.get(child.x ).set(parent.y,(byte)0);
            children.add(child);
        }
        if(parent.y < this.size-1 && parent.state.get(parent.x).get(parent.y+1) != parent.move) { // slide left
            Node child = new Node(parent.state);
            child.y = (byte)(parent.y+1);
            child.x = parent.x;
            child.move = child.state.get(parent.x).get(child.y);
            child.state.get(parent.x).set(parent.y,child.move);
            child.state.get(parent.x).set(child.y ,(byte)0);
            children.add(child);
        }
        return children;
    }
    
    public byte heuristic(ArrayList<ArrayList<Byte>> state) {
        byte score  = 0;
        int  target = 1;
        int  reset  = this.size*this.size;
        byte[ ][ ] inter_row = new byte[this.size][this.size];
        byte[ ][ ] inter_col = new byte[this.size][this.size];

        for(byte x=0;x<this.size;++x) {
            for(byte y=0;y<this.size;++y) {
                byte tile = state.get(x).get(y);

                if(tile != 0) {
                    if(tile == target) { 
                        inter_row[x][y] = tile;
                        inter_col[y][x] = tile; 
                    }
                    else {   
                        score += Math.abs(x - this.goal.get(tile).get(0))  // manhattan
                              +  Math.abs(y - this.goal.get(tile).get(1)); // distance
                        
                        if((byte)((tile-1) / this.size) == x) {
                            inter_row[x][y] = tile;
                        }
                        if((byte)((tile-1) % this.size) == y) {
                            inter_col[y][x] = tile;
                        }
                    }
                }
                else { 
                    inter_row[x][y] = 0;
                    inter_col[y][x] = 0; 
                }
                target++;
                if(target == reset) {
                    target = 0;
                }
            }
        }

        if (1 < score) {
            for(byte x=0;x<this.size;++x) {
                for(byte y=0;y<this.size-1;++y) {
                    for(byte z=(byte)(y+1);z<this.size;++z)  // linear conflict
                    {
                        if(inter_row[x][y] > inter_row[x][z] && inter_row[x][z] > 0) { 
                            score += 2; 
                        }
                        if(inter_col[x][y] > inter_col[x][z] && inter_col[x][z] > 0) {
                            score += 2;
                        }
                    } 
                }
            }
        }

        return score;
    }
    
    class Node {
        ArrayList<ArrayList<Byte>> state;
        byte move;
        byte x;
        byte y;
        Node(ArrayList<ArrayList<Byte>> source) {
            this.state = new ArrayList<>(source.size( ));
            for(ArrayList<Byte> row : source) {
                this.state.add(new ArrayList<>(row));
            }
        }
    }
}