import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import za.ac.wits.snake.DevelopmentAgent;
import java.util.LinkedList;
import java.util.Random;

public class MyAgent extends DevelopmentAgent {
	int[][] map;
	int w, h;
	FileWriter fw;
    BufferedWriter bw;
    String[][] snakeState;
    int[][] snakeHeads;
    int mySnakeNum;
    int[] apple1; 
    int[] apple2;
    int[] directions;	//0-Up, 1-Down, 2-Left, 3-Right
    
    boolean curr_invis = false;
    boolean prev_invis = false;
    String[] invispos;
    int invissnakenum;
    
    public static void main(String args[]) throws IOException {
    	//Files.deleteIfExists(Paths.get("out.txt"));
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }
    
    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
    		w = Integer.parseInt(temp[1]);
    		h = Integer.parseInt(temp[2]);
    		map = new int[w][h];
    		
            while (true) {
            	String state[] = new String[7];
                state[0] = br.readLine();
                state[1] = br.readLine();
                state[2] = br.readLine();
                state[3] = br.readLine();
                state[4] = br.readLine();
                state[5] = br.readLine();
                state[6] = br.readLine();
                //logState(state);
                
                snakeState = new String[4][];
                for (int i = 0; i < 4; i++){
                	snakeState[i] = state[3+i].split(" ");
                }
                
                for (int i = 0; i < w; i++){	//initialize map
        			for (int j = 0; j < h; j++){
        				map[i][j] = 0;
        			}
        		}
                
                if (state[0].contains("Game Over")) {
                    break;
                }
                
                apple1 = new int[2]; 
                apple2 = new int[2];
                String[] app1 = state[0].split(" ");
                String[] app2 = state[1].split(" ");
                apple1[0] = Integer.parseInt(app1[0]);  apple1[1] = Integer.parseInt(app1[1]);
                apple2[0] = Integer.parseInt(app2[0]);	apple2[1] = Integer.parseInt(app2[1]);
                
                if ((apple1[0] >= 0) && (apple1[1] >= 0)) {map[apple1[1]][apple1[0]] = -1;}		//Sets apple positions to -1 on map
                if ((apple2[0] >= 0) && (apple2[1] >= 0)) {map[apple2[1]][apple2[0]] = -1;}
           
                directions = new int[4];          
                mySnakeNum = Integer.parseInt(state[2]);
                snakeHeads = new int[nSnakes][2]; 	//to store positions of the heads of snakes 
                prev_invis = curr_invis;
                curr_invis = false;
                invissnakenum = -1;
                
                for (int i = 0; i < nSnakes; i++) {
                    String[] snakeLine = snakeState[i]; 	// {state, length, kills, {points}}
                    
                    if (!snakeLine[0].equals("dead")){
                    	String[] str;
                    	if (!snakeLine[0].equals("invisible")) {
                    		str = Arrays.copyOfRange(snakeLine, 3, snakeLine.length);
                    	} 
                    	else {
                    		if (i != mySnakeNum) {
                    			curr_invis = true;
                    			invissnakenum = i;
                    			if (!prev_invis){
                    				invispos = snakeLine;
                    			}
                    			str = Arrays.copyOfRange(invispos, 5, snakeLine.length);
                    		}
                    		else{
                    			str = Arrays.copyOfRange(snakeLine, 5, snakeLine.length);
                    		}
                    	}
                    	
                    	if (str.length > 1) {
                    		String[] head = str[0].split(",");
	                    	String[] first = str[1].split(",");
	                    	if (Integer.parseInt(head[1]) < Integer.parseInt(first[1]))			{directions[i] = 0;}
	                    	else if (Integer.parseInt(head[1]) > Integer.parseInt(first[1]))	{directions[i] = 1;}
	                    	else if (Integer.parseInt(head[0]) < Integer.parseInt(first[0]))	{directions[i] = 2;}
	                    	else if (Integer.parseInt(head[0]) > Integer.parseInt(first[0]))	{directions[i] = 3;}
                    	}
                    	
                        for (int j = 0; j < str.length; j++){	//fill in snake bodies	
            				String[] coord = str[j].split(",");
            				if (j == 0) {
            					snakeHeads[i][0] = Integer.parseInt(coord[0]);
            					snakeHeads[i][1] = Integer.parseInt(coord[1]);
            				}
            				
            				map[Integer.parseInt(coord[1])][Integer.parseInt(coord[0])] = 1;
            				
            				if (j < str.length-1) {
            					String[] next = str[j+1].split(",");
            					
            					if (coord[0].equals(next[0])){
            						for (int k = Math.min(Integer.parseInt(coord[1]), Integer.parseInt(next[1])); k < Math.max(Integer.parseInt(coord[1]), Integer.parseInt(next[1])); k++){
            							map[k][Integer.parseInt(coord[0])] = 1;
            						}
            					}
            					
            					if (coord[1].equals(next[1])){
            						for (int k = Math.min(Integer.parseInt(coord[0]), Integer.parseInt(next[0])); k < Math.max(Integer.parseInt(coord[0]), Integer.parseInt(next[0])); k++){
            							map[Integer.parseInt(coord[1])][k] = 1;
            						}
            					}
            				}
            			}
                    }
                }
                
                int move = makeMove();
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean nextMoveIsGoal(int[] head, int[] goal){
    	if (((head[1] + 1 == goal[1]) && (head[0] == goal[0])) ||	//goal is one move down
    		 (head[1] - 1 == goal[1]) && (head[0] == goal[0])  ||	//goal is one move up
    		 (head[0] - 1 == goal[0]) && (head[1] == goal[1])  ||	//goal is one move left
    		 (head[0] + 1 == goal[0]) && (head[1] == goal[1]))  {	//goal is one move right
    		return true;
    	}
    	else return false;
    }
    
    private void logState(String[] state) throws IOException{
        StringBuilder sb = new StringBuilder();
        for (String s : state) {
            sb.append(s).append("\n");
        }
        fw = new FileWriter("out.txt", true);
        bw = new BufferedWriter(fw);
		bw.write(sb.toString());
		bw.close();
		fw.close();
    }
    
    private int makeMove(){
    	int[] position = snakeHeads[mySnakeNum];
    	int m = new Random().nextInt(4);	//if there's no path to the apple, make a random move
    	boolean clash = false;
    	ArrayList<int[]> moves, invmoves;
    	int[] nextpos;
    	boolean a1onboard = ((apple1[0] != -1) && ((apple1[1] != -1)));
    	boolean apple1isnext = false;
    	
    	if (curr_invis){
    		String[] t = snakeState[mySnakeNum][snakeState[mySnakeNum].length-1].split(",");
    		int[] tail = {Integer.parseInt(t[0]), Integer.parseInt(t[1])};
    		map[tail[1]][tail[0]] = 0;
    		moves = bfs(position, tail, map);
    		
    		if (moves.size() > 2) {
        		nextpos = (moves.get(moves.size()-1));
        		if (nextpos[1] - position[1] == 1)	{m = 1;}	//DOWN
    	    	if (nextpos[1] - position[1] == -1)	{m = 0;}	//UP
    	    	if (nextpos[0] - position[0] == 1)	{m = 3;}	//RIGHT
    	    	if (nextpos[0] - position[0] == -1)	{m = 2;}	//LEFT
        	}	else {	//if there's no path, try not to die
        		map[tail[1]][tail[0]] = 1;
        		m = altMove(position);
        	}
    		nextpos = nextPositionFromMove(m, position);
    		for (int i = 0; i < 4; i++){
        		if (i != mySnakeNum){
        			int[] nextmove = nextPositionFromMove(directions[i], snakeHeads[i]);	//gets the next move if snake continues straight
        			if (Arrays.equals(nextpos, nextmove)) {
    					map[nextmove[1]][nextmove[0]] = 1;
    					m  = altMove(position);
    				}
        		}
    		}
    		return m;
    	}
    	
    	if (a1onboard) apple1isnext= nextMoveIsGoal(snakeHeads[mySnakeNum], apple1);
    	if (nextMoveIsGoal(snakeHeads[mySnakeNum], apple2) || apple1isnext){	//if a shorter snake and mine are both one move away from an apple, don't eat it
        	for (int i = 0; i < 4; i++){
        		if (i != mySnakeNum){
        			if ((nextMoveIsGoal(snakeHeads[i], apple2)) && (Integer.parseInt(snakeState[i][1]) <= Integer.parseInt(snakeState[mySnakeNum][1]))){
        				clash = true;
        			}
        		}
        	}
        }
    	
    	if (!clash){
    		moves = bfs(position, apple2, map);
    		if (a1onboard) {	//prioritise invisibility apple if it takes less than 15 more moves than other apple 
    			invmoves = bfs(position, apple1, map);
    			moves = (moves.size() < invmoves.size() - 15) ? moves : invmoves;	
    		}
    		
    		if (moves.size() > 0) {
        		nextpos = (moves.get(moves.size()-1));
        		if (nextpos[1] - position[1] == 1)	{m = 1;}	//DOWN
    	    	if (nextpos[1] - position[1] == -1)	{m = 0;}	//UP
    	    	if (nextpos[0] - position[0] == 1)	{m = 3;}	//RIGHT
    	    	if (nextpos[0] - position[0] == -1)	{m = 2;}	//LEFT
        	}	else {	//if there's no path, try not to die
        		m = altMove(position);
        	}
    		
    		nextpos = nextPositionFromMove(m, position);
    		boolean redo = false;
    		boolean gotocentre = false;
    		int[] centre = centreTings();
    		for (int i = 0; i < 4; i++){
        		if (i != mySnakeNum){
        			int[] nextmove = nextPositionFromMove(directions[i], snakeHeads[i]);	//gets the next move if snake continues straight
        			if (Arrays.equals(nextpos, nextmove)) {
        				if (!onlyMoveClash(nextpos, snakeHeads[i], directions[i])){
        					map[nextmove[1]][nextmove[0]] = 1;
        				}
    					redo = true;
    				}
    				ArrayList<int[]> a = bfs(snakeHeads[i], apple2, map);
    				if ((a.size() + 15 < moves.size()) && (Integer.parseInt((snakeState[mySnakeNum][1])) < 45)) {
    					redo = true;
    					gotocentre = true;
    				}
    				if (onlyMoveClash(nextpos, snakeHeads[i], directions[i])){
    					redo = true;
    				}
        		}
        	}
    		boolean alt = false;
    		if (redo){
    			moves = bfs(position, centre, map);
    			ArrayList<int[]> centremoves = bfs(position, apple2, map);
    			int movesize = moves.size();
    			if ((gotocentre) && (movesize < 4)) {
    				moves = centremoves;
    			}
    			else m = altMove(position);
    			
        		if (moves.size() > 0) {
            		nextpos = (moves.get(moves.size()-1));
            		if (nextpos[1] - position[1] == 1)	{m = 1;}	//DOWN
        	    	if (nextpos[1] - position[1] == -1)	{m = 0;}	//UP
        	    	if (nextpos[0] - position[0] == 1)	{m = 3;}	//RIGHT
        	    	if (nextpos[0] - position[0] == -1)	{m = 2;}	//LEFT
            	}	else {	//if there's no path, try not to die
            		alt = true;
            	}
        		nextpos = nextPositionFromMove(m, position);
        		for (int i = 0; i < 4; i++){
            		if (i != mySnakeNum){
            			int[] nextmove = nextPositionFromMove(directions[i], snakeHeads[i]);	//gets the next move if snake continues straight
            			if (Arrays.equals(nextpos, nextmove)) {
            				if (!onlyMoveClash(nextpos, snakeHeads[i], directions[i])){
            					map[nextmove[1]][nextmove[0]] = 1;
            				}
        					alt = true;
        				}
            			if (gotocentre){
            				int[][] tempmap = map;
            				tempmap[nextpos[0]][nextpos[1]] = 1;
            				ArrayList<int[]> nexttings = bfs(position, apple2, tempmap);
            				if (nexttings.isEmpty()) {
            					alt = true;
            				}
            			}
            		}
            	}
    		}
    		if (alt) m = altMove(position);
    	}
    	else {
    		m = altMove(position);	//if colliding with another snake when eating apple, avoid it
    	}
    	
    	return m;
    }
    
    private int[] centreTings(){
    	int[] cent = new int[2]; 
    	int radius = 0;	int i = 0;
    	while (map[h/2 + radius][w/2 + radius] != 0)	{radius += -2*i;i++;}
    	cent[0] = w/2 + radius; cent[1] = h/2 + radius;
    	/*if 		(map[h/2][w/2] == 0)	 {cent[0] = w/2; cent[1] = h/2;}
    	else if (map[h/2 - 3][w/2 - 3] == 0) {cent[0] = w/2 - 3; cent[1] = h/2 - 3;}
    	else if (map[h/2 + 3][w/2] == 0) {cent[0] = w/2; cent[1] = h/2 + 3;}
    	else if (map[h/2][w/2 + 3] == 0) {cent[0] = w/2 + 3; cent[1] = h/2;}
    	else if (map[h/2 - 3][w/2 + 3] == 0) {cent[0] = w/2 + 3; cent[1] = h/2 - 3;}
    	else if (map[h/2 - 3][w/2] == 0) {cent[0] = w/2; cent[1] = h/2 - 3;}
    	else if (map[h/2 + 3][w/2 - 3] == 0) {cent[0] = w/2 - 3; cent[1] = h/2 + 3;}
    	else if (map[h/2][w/2 - 3] == 0) {cent[0] = w/2 - 3; cent[1] = h/2;}
    	else if (map[h/2 + 3][w/2 + 3] == 0) {cent[0] = w/2 + 3; cent[1] = h/2 + 3;}
    	else {
    		int r1 = new Random().nextInt(4) - 2;	int r2 = new Random().nextInt(4) - 2;
    		cent[0] = w/2 + r1; cent[1] = w/2 + r2; 
    	}|*/
    	return cent;
    }
    
    private int[] nextPositionFromMove(int m, int[] currpos){
    	int x = currpos[0];	int y = currpos[1];
    	switch (m){
    		case 0:	
    			y = y - 1;
    			break;
    		case 1:	
    			y = y + 1;
    			break;
    		case 2:	
    			x = x - 1;
    			break;
    		case 3:	
    			x = x + 1;
    			break;
    	}
    	int[] pos = {x, y};
    	return pos;
    }
    
    private	boolean onlyMoveClash(int[] myMove, int[] other, int direction){
    	int x = other[0]; int y = other[1];
    	if ((x-1 < 0) || (x+1 >= w) || (y-1 < 0) || (y+1 >= h)) return false;
    	boolean onlymove = false;
    	int[] move = other;
    	switch (direction){
    		case 0:{	//Facing Up
    			if 	((map[y+1][x] == 0) && (map[y][x-1] == 1) && (map[y][x+1] == 1))	{
    				move[1] = y+1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x-1] == 0) && (map[y][x+1] == 1)) {
    				move[0] = x-1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x-1] == 1) && (map[y][x+1] == 0)) {
    				move[0] = x+1;
    				onlymove = true;
    			}
    		}
    		case 1:{	//Facing Down
    			if 	((map[y-1][x] == 0) && (map[y][x-1] == 1) && (map[y][x+1] == 1))	{
    				move[1] = y-1;
    				onlymove = true;
    			}
    			else if ((map[y-1][x] == 1) && (map[y][x-1] == 0) && (map[y][x+1] == 1)) {
    				move[0] = x-1;
    				onlymove = true;
    			}
    			else if ((map[y-1][x] == 1) && (map[y][x-1] == 1) && (map[y][x+1] == 0)) {
    				move[0] = x+1;
    				onlymove = true;
    			}
    		}
    		case 2:{	//Facing Left
    			if 	((map[y+1][x] == 0) && (map[y][x-1] == 1) && (map[y-1][x] == 1))	{
    				move[1] = y+1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x-1] == 0) && (map[y-1][x] == 1)) {
    				move[0] = x-1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x-1] == 1) && (map[y-1][x] == 0)) {
    				move[1] = y-1;
    				onlymove = true;
    			}
    		}
    		case 3:{	//Facing Right
    			if 	((map[y+1][x] == 0) && (map[y][x+1] == 1) && (map[y-1][x] == 1))	{
    				move[1] = y+1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x+1] == 0) && (map[y-1][x] == 1)) {
    				move[0] = x+1;
    				onlymove = true;
    			}
    			else if ((map[y+1][x] == 1) && (map[y][x+1] == 1) && (map[y-1][x] == 0)) {
    				move[1] = y-1;
    				onlymove = true;
    			}
    		}
    	}
    	if (onlymove) {
    		map[move[1]][move[0]] = 1;
    		return Arrays.equals(myMove, move);
    	}
    	else return false;
    }
    
    private int altMove(int[] currpos){	//makes an alternate move to not die 
    	int x = currpos[0]; int y = currpos[1];
    	boolean[] available = {false, false, false, false};
    	int m = 4;
    	int maxnumopen = 0;
    	if ((y-1 >= 0 ) && (map[y-1][x] == 0))	{available[0] = true;}
    	if ((y+1 < h) && (map[y+1][x] == 0))	{available[1] = true;}
    	if ((x-1 >= 0 ) && (map[y][x-1] == 0))	{available[2] = true;}
    	if ((x+1 < w) && (map[y][x+1] == 0))	{available[3] = true;}
    	for (int i = 0; i < 4; i++){
    		if (available[i]){
    			int s = 0;
    			switch (i){
    			case 0:{
    				int ny = y-1;	if (ny < 0) break;
    				if ((ny-1 >= 0 ) && (map[ny-1][x] == 0)){s++;}
    		    	if ((x-1 >= 0 ) && (map[ny][x-1] == 0))	{s++;}
    		    	if ((x+1 < w) && (map[ny][x+1] == 0))	{s++;}
    			}
    			case 1:{
    				int ny = y+1; if (ny >= h) break;
    		    	if ((ny+1 < h) && (map[ny+1][x] == 0))	{s++;}
    		    	if ((x-1 >= 0 ) && (map[ny][x-1] == 0))	{s++;}
    		    	if ((x+1 < w) && (map[ny][x+1] == 0))	{s++;}
    			}
    			case 2:{
    				int nx = x-1;	if (nx < 0) break;
    				if ((y-1 >= 0 ) && (map[y-1][nx] == 0))	{s++;}
    		    	if ((y+1 < h) && (map[y+1][nx] == 0))	{s++;}
    		    	if ((nx-1 >= 0 ) && (map[y][nx-1] == 0)){s++;}
    			}
    			case 3:{
    				int nx = x+1;	if (nx >= w) break;
    				if ((y-1 >= 0 ) && (map[y-1][nx] == 0))	{s++;}
    		    	if ((y+1 < h) && (map[y+1][nx] == 0))	{s++;}
    		    	if ((nx+1 < w) && (map[y][nx+1] == 0))	{s++;}
    			}
    			}
    			if (maxnumopen < s) {
    				maxnumopen = s;
    				m = i;
    			}
    		}
    	}
    	return m;
    }
    
    private ArrayList<int[]> bfs(int[] root, int[] dest, int[][] map){	//performs a BFS from root to dest;	returns an ArrayList with moves to make
    	if ((root[0] < 0) || (root[0] > w-1) || (root[1] < 0) || (root[1] > h-1)) {return new ArrayList<int[]>();}
    	LinkedList<int[]> q = new LinkedList<int[]>();
    	int[][][] parent = new int[h][w][2];
    	boolean[][] checked = new boolean[h][w];
    	for (int i = 0; i < w; i++){
			for (int j = 0; j < h; j++){
				checked[i][j] = false;
				parent[i][j][0] = -1;
				parent[i][j][1] = -1;
			}
		}
    	
    	checked[root[1]][root[0]] = true;
    	q.addLast(root);
    	int[] curr = root;
    	
    	while ((!q.isEmpty()) && (!Arrays.equals(curr,dest))){
    		curr = q.remove();
    		
    		int[] down = new int[2];
    		down[0] = curr[0];
    		down[1] = curr[1]+1;
    		
    		int[] up = new int[2];
    		up[0] = curr[0];
    		up[1] = curr[1]-1;	
    		
    		int[] right = new int[2];
    		right[0] = curr[0]+1;
    		right[1] = curr[1];	
    		
    		int[] left = new int[2];
    		left[0] = curr[0]-1;
    		left[1] = curr[1];
    	
    		if ((down[1] < h) && (checked[down[1]][down[0]] == false) && (map[down[1]][down[0]] == 0 || map[down[1]][down[0]] == -1)){
    			checked[down[1]][down[0]] = true;
    			parent[down[1]][down[0]] = curr;
    			q.addLast(down);
    		}
    		
    		if ((up[1] >= 0) && (checked[up[1]][up[0]] == false) && (map[up[1]][up[0]] == 0 || map[up[1]][up[0]] == -1)){
    			checked[up[1]][up[0]] = true;
    			parent[up[1]][up[0]] = curr;
    			q.addLast(up);
    		}
    		
    		if ((right[0] < w) && (checked[right[1]][right[0]] == false) && (map[right[1]][right[0]] == 0 || map[right[1]][right[0]] == -1)){
    			checked[right[1]][right[0]] = true;
    			parent[right[1]][right[0]] = curr;
    			q.addLast(right);
    		}
    		
    		if ((left[0] >= 0) && (checked[left[1]][left[0]] == false) && (map[left[1]][left[0]] == 0 || map[left[1]][left[0]] == -1)){
    			checked[left[1]][left[0]] = true;
    			parent[left[1]][left[0]] = curr;
    			q.addLast(left);
    		}
    	} 
    	
    	ArrayList<int[]> arr = new ArrayList<int[]>();
    	if (!q.isEmpty() && checked[dest[1]][dest[0]]) {
    		while (!Arrays.equals(curr,root)) {
    			arr.add(curr);
    			curr = parent[curr[1]][curr[0]];
    		}
    		//arr.add(root);
    	}
    	return arr;
    }
}