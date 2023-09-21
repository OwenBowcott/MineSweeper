import com.badlogic.gdx.ApplicationAdapter; 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; 
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle; 
import com.badlogic.gdx.math.Circle; 
import com.badlogic.gdx.Input.Keys; 
import com.badlogic.gdx.math.Vector2; 
import com.badlogic.gdx.math.MathUtils; 
import com.badlogic.gdx.math.Intersector; 
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Texture; 
import com.badlogic.gdx.InputProcessor; 
import com.badlogic.gdx.*; 
import com.badlogic.gdx.utils.Array;  

public class MineSweeper extends ApplicationAdapter 
{
    private OrthographicCamera camera; //the camera to our world
    private Viewport viewport; //maintains the ratios of your world

    private SpriteBatch batch; //used to draw images
    private BitmapFont font; //used to draw text
    private GlyphLayout layout; //used to help format the text

    private int[][] visibleGrid; //what the player sees
    private int[][] grid; //holds the position of the mines, and how many mines touch each cell
    private Array<Texture> images;//holds al lour images
    private float flagTimer; //times how long it has been since we flagged a cell
    private float gameTimer; //times how long we have been playing the game
    private boolean isGameOver; //checks if the game is over
    private boolean wonGame; //checks if the game has been won
    private int flagOnMines;
    private int totalFlags;
    private int numOfUnexplored;

    public static final int WORLD_WIDTH = 400; 
    public static final int WORLD_HEIGHT = 400; 

    //constant that make our code more readable
    public static final int UNEXPLORED = 9; 
    public static final int MINE = 10; 
    public static final int FLAG = 11; 
    public static final int NUM_MINES = 40;
    public static final int NUM_ROWS = 20; 
    public static final int NUM_COLUMNS = 20; 
    public static final int CELL_WIDTH = WORLD_WIDTH / NUM_COLUMNS;//20 columns  
    public static final int CELL_HEIGHT = WORLD_HEIGHT / NUM_ROWS;//20 rows

    @Override//this is called once when you first run your program
    public void create(){       
        camera = new OrthographicCamera(); 
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera); 

        batch = new SpriteBatch(); 
        font = new BitmapFont(Gdx.files.internal("impact32.fnt")); 
        font.getData().markupEnabled = true; 
        layout = new GlyphLayout(); 

        visibleGrid = new int[20][20];
        grid = new int[20][20]; 

        setVisibleGrid(); 
        //place the mines
        placeMines(); 
        
        int flagOnMines = 0;
        int totalFlags = 0;
        int numOfUnexplored = 0;

        flagTimer = 0;
        isGameOver = false; 
        wonGame = false; 
        count(); //count how many mines are next to each cell in 'grid'

        images = new Array<Texture>(); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_0.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_1.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_2.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_3.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_4.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_5.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_6.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_7.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_8.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_unexplored.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_mine.png"))); 
        images.add(new Texture(Gdx.files.internal("images/Minesweeper_LAZARUS_21x21_flag.png")));
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_0.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_1.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_2.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_3.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_4.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_5.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_6.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_7.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_8.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_X.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_M.png"))); 
        // images.add(new Texture(Gdx.files.internal("MINESWEEPER_F.png"))); 
    }

    @Override//this is called 60 times a second
    public void render(){
        //these two lines wipe and reset the screen so when something action had happened
        //the screen won't have overlapping images
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        flagTimer += Gdx.graphics.getDeltaTime();//adds to our timer
        if(!isGameOver && !wonGame)//if the game is not over and we have not won the game, add to our timer
            gameTimer += Gdx.graphics.getDeltaTime(); 

        getInput();//helper method to check if the user has done anything 
        wonGame = wonGame(); //helper method to check if the game has been won

        //when we draw with SpriteBatch everything has to be between and begin and an end call
        batch.setProjectionMatrix(viewport.getCamera().combined); 
        batch.begin(); 
        drawBoard();//helper method where we do all the drawing
        batch.end();    
    }

    public void setVisibleGrid()
    {
        //TODO set every cell of the 'visibleGrid' 2D array to UNEXPLORED
        for(int r = 0; r < visibleGrid.length; r++)
        {
            for(int c = 0; c < visibleGrid[r].length; c++)
            {
                visibleGrid[r][c] = UNEXPLORED;
            } 
        }
    }

    public void placeMines()
    {
        grid = new int[20][20]; 
        for(int i = 0; i < NUM_MINES; i++)
        {
            int row; 
            int col; 
            do
            {
                row = (int)(Math.random() * grid.length); 
                col = (int)(Math.random() * grid[0].length); 
            }while(grid[row][col] == MINE);//keep generating a random row and col
            //until we find one that does not have a mine already
            grid[row][col] = MINE; //mine
        }  
    }

    public void count()
    {
        for(int r = 0; r < grid.length; r++)
        {
            for(int c = 0; c < grid[r].length; c++)
            {
                int ct = 0; 
                //TODO: count how many mines are next to each cell in the 2D array 'grid'
                //this is similar to the life lab but also make sure you do not
                //go out of bounds
                //Here are a few examples, you need to finish the rest
                //check if the top left neighbor is a MINE, also check 
                //r-1 and c-1 are valid indices to ensure we do not go out of bounds in the 2D array
                if(r-1 >= 0 && c-1 >= 0 && grid[r-1][c-1] == MINE) ct++;
                if(r+1 < grid.length && c-1 >= 0 && grid[r+1][c-1] == MINE) ct++;
                if(r+1 < grid.length && c+1 < grid[0].length && grid[r+1][c+1] == MINE) ct++;
                if(r+1 < grid.length && grid[r+1][c] == MINE) ct++;
                if(c+1 < grid[0].length && grid[r][c+1] == MINE) ct++;
                if(r-1 >= 0 && c+1 < grid[0].length && grid[r-1][c+1] == MINE) ct++;
                if(r-1 >= 0 && grid[r-1][c] == MINE) ct++;
                if(c-1 >= 0 && grid[r][c-1] == MINE) ct++;
                //check if the top right neighbor is a MINE, also check 
                //r-1 and c+1 are valid indices to ensure we do not go out of bounds in the 2D array
                //you need to finish checking the remaining 6 neighbors

                //if the cell is not a MINE set it to the number of mines that touch it
                if(grid[r][c] != MINE)grid[r][c] = ct; 
            }
        }

    }

    private void getInput()
    {
        //check if the games is over and reset everything
        if(isGameOver || wonGame)
        {
            if(Gdx.input.justTouched())//if we click, everything will reset
            {
                setVisibleGrid(); 
                placeMines(); 
                count();
                isGameOver = false; 
                wonGame = false; 
                gameTimer = 0; 
            }
        }
        else
        {

            if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())//checks for a click
            {
                int x = Gdx.input.getX(); //gets the x of the screen
                int y = Gdx.input.getY(); //gets y of the screen
                Vector2 pos = viewport.unproject(new Vector2(x,y));//maps the coordinate clicked
                //to the correct position of our world

                int row = (visibleGrid.length - 1) - (int)pos.y / CELL_HEIGHT;
                //y coordinate maps to row 0 for values from [380,400)
                //y coordinate maps to row 1 for values from [360,380)
                //y coordinate maps to row 2 for values from [340,360)
                //etc.

                int col = (int)pos.x / CELL_WIDTH; 
                // x coordinate maps to column 0 for values from [0,20)
                // x coordinate maps to column 1 for values from [20,40)
                // x coordinate maps to column 2 for values from [40,60)
                //etc.

                //if we clicked a mine
                if(grid[row][col] == MINE)
                {
                    gameOver();//call helper method gameOver
                }
                else if(visibleGrid[row][col] == UNEXPLORED)//if you clicked an empty cell
                {
                    updateBoard(row, col); //if we clicked an UNEXPLORED cell, update the board
                }

            } 
            //if we right click allow a flag to be placed
            else if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.justTouched())
            {
                int x = Gdx.input.getX(); //gets the x of the screen
                int y = Gdx.input.getY(); //gets y of the screen
                Vector2 pos = viewport.unproject(new Vector2(x,y));//maps the coordinate clicked
                //to the correct position of our world

                //same logic as above
                int row = (visibleGrid.length - 1) - (int)pos.y / CELL_HEIGHT;
                int col = (int)pos.x / CELL_WIDTH;

                //if we right clicked and UNEXPLORED cell and a flagTimer
                //has elapsed .5 secods (the timer is just for fun)
                if(visibleGrid[row][col] == UNEXPLORED && flagTimer > 0.5)
                {
                    visibleGrid[row][col] = FLAG;//if you clicked an empty cell
                    flagTimer = 0; 
                }
                //toggle off a flag 
                else if(visibleGrid[row][col] == FLAG && flagTimer > 0.5)
                {
                    visibleGrid[row][col] = UNEXPLORED;
                    flagTimer = 0; 
                }
            }
        }
    }

    public boolean wonGame()
    {
        //TODO check if the game is won
        //create counters for the num of flags on mines, total flags, and the num of unexplored cells
        //if the grid at a cell is a MINE AND the visibleGrid at that same cell is a FLAG we can increase the flag
        //on mine counter, if the visibleGrid is a flag we can increase teh total flag counter,
        //if the visibleGrid at a cell is UNEXPLORED then we can increase the unexplored counter.
        //We did not win if the totalFlags > NUM_MINES. We won the game if the num of flags on mines equals the NUM_MINES
        //OR if the num of flags on mines + the num of unexplored equals the NUM_MINES so return true
        //otherwise return false

        for(int r = 0; r < grid.length; r++)
        {
            for(int c = 0; c < grid[r].length; c++)
            {
                if(grid[r][c] == MINE && visibleGrid[r][c] == FLAG)
                {
                    flagOnMines++;
                }
                if(visibleGrid[r][c] == FLAG)
                {
                    totalFlags++;
                }
                if(visibleGrid[r][c] == UNEXPLORED)
                {
                    numOfUnexplored++;
                }
            }
        }
        
        if(totalFlags > NUM_MINES)
        {
            return false;
        }
        
        if(flagOnMines == NUM_MINES || flagOnMines + numOfUnexplored == NUM_MINES)
        {
            return true;
        }
        
        return false;
    }

    private void gameOver()
    {
        for(int r = 0; r < grid.length; r++)
        {
            for(int c = 0; c < grid[r].length; c++)  
            {
                if(grid[r][c] == MINE)//display all the mines
                    visibleGrid[r][c] = grid[r][c]; 
            }
        }
        isGameOver = true; 
    }

    private void updateBoard(int row, int col)
    {
        //base case: we are done "exploding" a region if we go out of bounds
        //or if we reach a cell that has already been explored
        if(row < 0 || row >= grid.length
        || col < 0 || col >= grid[0].length
        || visibleGrid[row][col] != UNEXPLORED) //out of bounds or already visible
        {
            return; //in this base case do nothing
        }
        //base case if we reach a cell that does not have 0 mines touching it
        else if(grid[row][col] != 0)//base case stop the expanding of cells when it reaches a cell with a mine next to it
        {
            visibleGrid[row][col] = grid[row][col];//set the visible grid to the numeric value in grid set by the method count()      
        }
        //RECURSIVELY "explode" a region if there are no mines touching the cell
        else 
        {
            visibleGrid[row][col] = 0; 
            updateBoard(row - 1, col); //up
            updateBoard(row + 1, col); //down
            updateBoard(row - 1, col - 1);//top left
            updateBoard(row - 1, col + 1);//top right
            updateBoard(row, col -1 );//left
            updateBoard(row, col + 1);//right
            updateBoard(row + 1, col + 1);//bottom right
            updateBoard(row + 1, col - 1);//bottom left
        }

    }

    private void drawBoard()
    {

        for(int r = 0; r < visibleGrid.length; r++)
        {
            for(int c = 0; c < visibleGrid[0].length; c++)
            {
                //Note visibleGrid[r][c] corresponds to the index of the appropriate
                //Texture object in images
                batch.draw(images.get(visibleGrid[r][c]), //Texture object to draw
                    c * CELL_WIDTH, //x coordinate of the bottom left
                    (grid.length - 1) * CELL_HEIGHT - CELL_HEIGHT * r,//y coordinate of the bottom right
                    CELL_WIDTH, CELL_HEIGHT); //width and height of image to draw            
            }

        }
        //Make sure to draw the text after drawing the board or it will not be visible
        //it would be "underneath" the grid. Think of it like painting the order we
        //paint matters

        //set the appropriate message
        if(isGameOver)
        {
            layout.setText(font, "[BLACK]You Lost\nClick to play again!"); 
            font.draw(batch, //our SpriteBatch
                layout, //GlyphLayout
                (WORLD_WIDTH - layout.width) / 2,//x coordinate of the top left of the text
                (WORLD_HEIGHT -  2 * layout.height) / 2);//y coordinate of the top right of the text
        }
        if(wonGame)
        {
            layout.setText(font, "[BLACK]You won!\nYour time was: " + gameTimer + "\nClick to play again!"); 
            font.draw(batch, layout, 
                (WORLD_WIDTH - layout.width) / 2, 
                (WORLD_HEIGHT -  layout.height) / 2);
        }
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true); 
    }

    @Override
    public void dispose(){
        batch.dispose();
        font.dispose(); 

    }
}
