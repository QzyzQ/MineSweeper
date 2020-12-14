import java.util.Random;
import java.util.Arrays;
import java.util.Scanner;

/*
A friend mentioned having the choice to create either a web-app or minesweeper for a coding interview.
This had me thinking about how I would go about creating minesweeper and once I had an idea, I couldn't NOT make it, right?
The game is fully playable and functional, but it is far from perfect.
Some downsides/struggles of this project:
	-Testing when the output was random each time
	-Not pretty, I mean the field is a bunch of equal-signs surrounded by brackets
	-Not very user-friendly as one has to count out the columns and rows to figure out which coordinates to type to search
	-Does not reveal reachable 0-locations when one 0-location is revealed, as an ideal version of the game would.
	-9 conditions to fill each location in with number of adjacent mines, I'm certain this can be simplified immensely.
	-Only allows square playing fields (I'm not sure whether this is actually negative in the case of this game)
	-Doesn't handle anything except for two integer inputs where the second integer is less than the square of the first.
The first things I would implement to correct some of these downsides were I to revisit this project:
	-Error handling for incorrect inputs
	-Figure out more concise conditional logic for the numberFill method
	-Add labels for columns/rows

Despite the aforementioned flaws I enjoyed making this. It took me a bit to come up with some of what turned out to be simple solutions. For example,
Instead of using two separate arrays, one that is revealed to the player and one that is hidden, I was planning on making objects out of every location with attributes
for their hidden values and their shown values only to realize that would have made this implementation more complicated than necessary (at least in this case).

To run this, simply compile it and then run it with two integer arguments where the only technical constraint is that the square of the first integer must be larger than the second integer.
Realistically I would keep the first integer below 20 as otherwise it will probably not be very visible in a terminal window.
In bash, compiling/running this would look like the following:
	javac Sweep.java
	java Sweep 10 20
*/
public class Sweep{
	public static void main(String[] args){

		/*
		Input will look like "java Sweep 10 15"
		Where 10 is the width/length and 15 is the number of mines
		*/
		int dimension = Integer.parseInt(args[0]);
		int numMines = Integer.parseInt(args[1]);
		int lineFieldSize = dimension*dimension;
		Scanner scan = new Scanner(System.in);//Scanner for future user inputs


		/*
		Making a one-dimensional array as it will be easier to randomly
		place the mines within it and then later convert it into two dimensions.
		Going to fill this array with "E" just as a placeholder to be replaced with
		mines and the numbers indicating adjacent mines.
		*/
		String[] emptyField = new String[lineFieldSize];
		for (int i = 0; i<lineFieldSize; i++){
			emptyField[i] = "E";
		}

		/*
		Using the placeMines method from below to randomly select a number
		of "E"s within the one-dimensional array to be replaced with mines 
		equal to the number of mines input
		*/
		String[] mineField = placeMines(emptyField, numMines);
		//System.out.println(mineField);
		//Printing to check for correctness as I could not think of a way to unit test with random outputs

		String[][] twoDMineField = twoDimensionify(mineField, dimension);
		//System.out.println(Arrays.deepToString(twoDMineField).replace("], ", "]\n"));

		String[][] trueMineField = numberFill(twoDMineField, dimension);
		//System.out.println(Arrays.deepToString(trueMineField).replace("], ", "]\n"));

		//Using "=" as arbitrarily selected symbol to represent unsearched locations on minefield
		String filler = "=";
		String[][] shownField = new String[dimension][dimension];
		for (int i = 0; i < dimension; i++){
			for (int j = 0; j < dimension; j++){
				shownField[i][j] = filler;
			}
		}
		System.out.println(Arrays.deepToString(shownField).replace("], ", "]\n"));

		boolean gameGoing = true;
		int column;
		int row;
		int winCounter = 1;
		/*
		The field is now created and this while loop will be how the game is 'played'.
		It requests input and, once received, reveals the corresponding location of the minefield.
		*/
		while (gameGoing){
			System.out.println("Please input the coordinates (column,row) of the location you wish to check for mines next.");
			System.out.println("Values start from top left (0,0) and increase toward bottom right of field.");
			System.out.println("do so by entering your column value followed by hitting enter, then enter your row value followed by enter");
			System.out.println("For example: 0[Enter]0[Enter] reveals the top left corner location.");

			column = scan.nextInt();
			row = scan.nextInt();

			shownField[row][column] = twoDMineField[row][column];
			//Prints the field with the newly revealed location included.
			System.out.println(Arrays.deepToString(shownField).replace("], ", "]\n"));

			//Result of selecting location with mine
			if (twoDMineField[row][column] == "M"){
				gameGoing = false;
				System.out.println("BOOOOOM!!! You stepped on a mine! Game Over.");
				System.out.println(Arrays.deepToString(trueMineField).replace("], ", "]\n"));
			}
			//Result of clearing all non-mine spaces
			if (winCounter == (lineFieldSize-numMines)){
				gameGoing = false;
				System.out.println("Congratulations, you cleared the minefield!");
				System.out.println(Arrays.deepToString(trueMineField).replace("], ", "]\n"));
			}
			winCounter++;
		}
	}

	/*
	Method for randomly placing mines, "M", among a one dimensional array filled 
	with values of "E" which i'm using as a spaceholder
	*/
	public static String[] placeMines(String[] lineField, int mines){

		int minesPlaced = 0;
		Random rand = new Random();
		int upperbound = lineField.length;
		int randomIndex;

		while(minesPlaced < mines){
			randomIndex = rand.nextInt(upperbound);
			if (lineField[randomIndex] == "E"){
				lineField[randomIndex] = "M";
				minesPlaced++;
			}
		}
		return lineField;
	}

	//Simple function that turns an x*x length array into a 2D array of x arrays, each of x length
	public static String[][] twoDimensionify(String[] lineField, int rowLength){
		String[][] twoDField = new String[rowLength][rowLength];
		int index = 0;
		for (int i = 0; i < rowLength; i++){
			for (int j = 0; j < rowLength; j++){
				twoDField[i][j] = lineField[index];
				index++;
			}
		}
		return twoDField;
	}

	//Method used to replace non-mine locations with numbers that correspond to the number of adjacent mines.
	public static String[][] numberFill(String[][] mineField, int dimension){
		int adjacentMines;
		String newValue;

		/*
		The below loops will do the replacing of "E" values with a number.
		This loop also includes conditional logic covering the nine cases where the amount and direction of adjacent locations differed. 
		For example: each corner space only has 3 adjacent locations where a central space has 8.
		These conditions were made to avoid any index-out-of-bounds errors.
		*/
		for (int i = 0; i < dimension; i++){
			for (int j = 0; j < dimension; j++){
				adjacentMines = 0;
				//ANY NON-EDGE SPOT
				if (i-1>=0 && j-1>=0 && i+1 < dimension && j+1 < dimension){
					for (int k = i-1; k <= i+1; k++){
						for (int l = j-1; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//TOP-LEFT SPOT
				if (i-1<0 && j-1<0){
					for (int k = i; k <= i+1; k++){
						for (int l = j; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//BOTTOM-RIGHT SPOT
				if (i+1==dimension && j+1==dimension){
					for (int k = i-1; k <= i; k++){
						for (int l = j-1; l <= j; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//TOP-RIGHT SPOT
				if (i-1<0 && j+1==dimension){
					for (int k = i; k <= i+1; k++){
						for (int l = j-1; l <= j; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//BOTTOM-LEFT SPOT
				if (i+1==dimension && j-1<0){
					for (int k = i-1; k <= i; k++){
						for (int l = j; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//TOP-EDGE SPOTS
				if (i-1<0 && j-1>=0 && j+1<dimension){
					for (int k = i; k <= i+1; k++){
						for (int l = j-1; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//LEFT-EDGE SPOTS
				if (i-1>=0 && j-1<0 && i+1<dimension){
					for (int k = i-1; k <= i+1; k++){
						for (int l = j; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//BOTTOM-EDGE SPOTS
				if (i+1==dimension && j-1>=0 && j+1<dimension){
					for (int k = i-1; k <= i; k++){
						for (int l = j-1; l <= j+1; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				//RIGHT-EDGE SPOTS
				if (i-1>=0 && j+1==dimension && i+1<dimension){
					for (int k = i-1; k <= i+1; k++){
						for (int l = j-1; l <= j; l++){
							if (mineField[k][l]=="M"){
								adjacentMines++;
							}
						}
					}
				}
				if (mineField[i][j]!="M"){
					newValue = String.valueOf(adjacentMines);
					mineField[i][j] = newValue;
				}
			}
		}
		return mineField;
	}		
}

