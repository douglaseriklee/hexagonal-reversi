//  HexagonalReversi (c) 2017 Doug Lee

package com.example.temporary.hexagonalreversi;

import android.util.Log;

public class Position
{
    /*
    // Start position for side = 4
    static String start4 = "";
    static
    {
        start4 +=     "****";
        start4 += "****    ";
        start4 += "***     ";
        start4 += "**  BB  ";
        start4 += "*  RRR  ";
        start4 += "*  BB  *";
        start4 += "*     **";
        start4 += "*    ***";
        start4 += "*****";
    }

    // Start position for side = 5
    static String start5 = "";
    static
    {
        start5 +=      "*****";
        start5 += "*****     ";
        start5 += "****      ";
        start5 += "***       ";
        start5 += "**   BB   ";
        start5 += "*   RRR   ";
        start5 += "*   BB   *";
        start5 += "*       **";
        start5 += "*      ***";
        start5 += "*     ****";
        start5 += "******";
    }

    // Start position for side = 6
    static String start6 = "";
    static {
        start6 +=       "******";
        start6 += "******      ";
        start6 += "*****       ";
        start6 += "****        ";
        start6 += "***         ";
        start6 += "**    BB    ";
        start6 += "*    RRR    ";
        start6 += "*    BB    *";
        start6 += "*         **";
        start6 += "*        ***";
        start6 += "*       ****";
        start6 += "*      *****";
        start6 += "*******";
    }
    */
    // Start position for side = 4
    static String start4 = "";
    static
    {
        start4 +=     "****";
        start4 += "****    ";
        start4 += "*** R B ";
        start4 += "**  BR  ";
        start4 += "* BR*BR ";
        start4 += "*  BR  *";
        start4 += "* R B **";
        start4 += "*    ***";
        start4 += "*****";
    }

    // Start position for side = 5
    static String start5 = "";
    static
    {
        start5 +=      "*****";
        start5 += "*****     ";
        start5 += "****      ";
        start5 += "***  R B  ";
        start5 += "**   BR   ";
        start5 += "*  BR*BR  ";
        start5 += "*   BR   *";
        start5 += "*  R B  **";
        start5 += "*      ***";
        start5 += "*     ****";
        start5 += "******";
    }

    // Start position for side = 6
    static String start6 = "";
    static {
        start6 +=       "******";
        start6 += "******      ";
        start6 += "*****       ";
        start6 += "****        ";
        start6 += "***   R B   ";
        start6 += "**    BR    ";
        start6 += "*   BR*BR   ";
        start6 += "*    BR    *";
        start6 += "*   R B   **";
        start6 += "*        ***";
        start6 += "*       ****";
        start6 += "*      *****";
        start6 += "*******";
    }

    //  Position at the start of a game
    static String start = start6;

    //  Values for spaces on the board, ~BLUE == RED && ~RED == BLUE
    static final byte EDGE  =  2;
    static final byte BLANK =  1;
    static final byte BLUE  =  0;
    static final byte RED   = -1;

    static int side    = 6;
    static int columns = 2*side;
    static int rows    = columns-1;
    static int size    = columns*columns+1;

    static void SetSide(int s)
    {
        Log.d("Hex", "SetSide "+s);

        side    = s;
        columns = 2*side;
        rows    = columns-1;
        size    = columns*columns+1;

        switch(side)
        {
            case 4:
                start = start4;
                break;
            case 5:
                start = start5;
                break;
            case 6:
                start = start6;
                break;
        }
    }

    //  Position of the board
    byte board[] = new byte[size];

    //  Position constructor
    Position() {}

    //  Position constructor
    Position(Position p)
    {
        //  Copy given position to board
        System.arraycopy(p.board, 0, board, 0, board.length);
    }

    //  Perform assignment
    void CopyBoard(Position p)
    {
        //  Copy given position to board
        System.arraycopy(p.board, 0, board, 0, board.length);
    }

    //  Update Position for a given move by a given color.
    //  Return number of opponent's pieces reversed or
    //  zero in which case the board will remain the same.
    int UpdatePosition(int i, byte color)
    {
        int count     =  0;
        int offsets[] = {1, columns, rows, -1, -columns, -rows};

        //  Only allow moves on empty spaces
        if( board[i] != BLANK)
            return 0;

        SendCircle(i, color);

        //  Scan for reversals in all six directions
        for(int direction = 0; direction < 6; ++direction)
        {
            int scan = i;

            //  Scan through one row of opponent's pieces
            while(board[scan += offsets[direction]] == ~color)
                ;

            //  If row is terminated by one of our own pieces scan backwards
            //  updating the board, reversing pieces and incrementing count
            if( board[scan] == color)
                while(board[scan -= offsets[direction]] != color)
                {
                    SendCircle(scan, color);
                    ++count;
                }
        }
   
        //  If move is invalid reset the board
        if( count == 0)
            board[i] = BLANK;

        return count;
    }

    void SendCircle(int i, byte color)
    {
        board[i] = color;
    }

    //  Return first valid move for a given color or zero if none
    int FirstMove(byte color)
    {
        Position test = new Position(this);

        for(int i = columns; i < size-columns; i++)
            if( test.UpdatePosition(i, color) > 0)
                return i;

        return 0;
    }

    //  Maximum depth of search
    static int level = 6;

    static final int minimum = -1000*1000*1000;
    static final int maximum = -minimum;

    //  Search to a given depth for the best move for a given color.
    //  Return the best move found if at root level otherwise return an
    //  evaluation of the position from the perspective of the given color.
    //  The alpha parameter represents the best value this side knows
    //  it can achieve while the beta parameter represents the best value the
    //  opposing side knows it can achieve.  The goal is to find the highest
    //  alpha value less than beta.  Because higher values which are better
    //  for one side are worse for the other the parameters to and results of 
    //  the recursion are negated.
    int MinimaxSearch(byte color, int depth, int alpha, int beta)
    {
        if( depth == 0)
            return EvaluatePosition(color);

        Position test = new Position(this);
        int      best = minimum;
        int      move = 0;

        //  If not at leaf level of search tree update test position with each valid
        //  move and make recursive search call to evaluate the resulting position
        for(int i = columns; i < size-columns; i++)
            if( test.UpdatePosition(i, color) > 0)
            {
                int result;

                if( test.FirstMove((byte) ~color) > 0)
                    result = -test.MinimaxSearch((byte) ~color, depth-1, -beta, -alpha);
                else if(test.FirstMove(color) > 0)
                    result =  test.MinimaxSearch(color, depth-1, alpha, beta);
                else
                    result =  test.EvaluatePosition(color, depth);

                if( best < result)
                {
                    best = result;
                    move = i;
                }

                //  If the move is an improvement over alpha record it
                if( alpha < result)
                    alpha = result;

                //  If the move is an improvement over beta terminate the search
                //  because the opponent should clearly not pursue this course
                if( alpha >= beta)
                    break;

                test.CopyBoard(this);
            }

        //  Return either the best move found or the best value found
        //  depending on whether the search is at root level
        return depth == level ? move : best;
    }

    //  Values of the different types of spaces on the board
    static final int W0 =    0; // not used
    static final int W1 =   64; // on the edge
    static final int W2 =  -16; // next to on the edge
    static final int W3 =    4; // next to next to on the edge
    static final int W4 =    1; // next to...
    static final int W5 =    1; // next to...
    static final int W6 =    1; // next to...

    //  Locations of the different types of spaces on the board
    static final int weight[][][] =      {{{W0,W0,W0,W0,
                                W0,W0,W0,W0,W1,W1,W1,W1,
                                W0,W0,W0,W2,W2,W2,W2,W2,
                                W0,W0,W3,W3,W3,W3,W3,W3,
                                W0,W4,W4,W4,W4,W4,W4,W4,
                                W0,W3,W3,W3,W3,W3,W3,W0,
                                W0,W2,W2,W2,W2,W2,W0,W0,
                                W0,W1,W1,W1,W1,W0,W0,W0,
                                W0,W0,W0,W0,W0},
                                           {W0,W0,W0,W0,
                                W0,W0,W0,W0,W4,W3,W2,W1,
                                W0,W0,W0,W3,W4,W3,W2,W1,
                                W0,W0,W2,W3,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W3,W2,W0,
                                W0,W1,W2,W3,W4,W3,W0,W0,
                                W0,W1,W2,W3,W4,W0,W0,W0,
                                W0,W0,W0,W0,W0},
                                           {W0,W0,W0,W0,
                                W0,W0,W0,W0,W1,W2,W3,W4,
                                W0,W0,W0,W1,W2,W3,W4,W3,
                                W0,W0,W1,W2,W3,W4,W3,W2,
                                W0,W1,W2,W3,W4,W3,W2,W1,
                                W0,W2,W3,W4,W3,W2,W1,W0,
                                W0,W3,W4,W3,W2,W1,W0,W0,
                                W0,W4,W3,W2,W1,W0,W0,W0,
                                W0,W0,W0,W0,W0}},
                                              {{W0,W0,W0,W0,W0,
                                 W0,W0,W0,W0,W0,W1,W1,W1,W1,W1,
                                 W0,W0,W0,W0,W2,W2,W2,W2,W2,W2,
                                 W0,W0,W0,W3,W3,W3,W3,W3,W3,W3,
                                 W0,W0,W4,W4,W4,W4,W4,W4,W4,W4,
                                 W0,W5,W5,W5,W5,W5,W5,W5,W5,W5,
                                 W0,W4,W4,W4,W4,W4,W4,W4,W4,W0,
                                 W0,W3,W3,W3,W3,W3,W3,W3,W0,W0,
                                 W0,W2,W2,W2,W2,W2,W2,W0,W0,W0,
                                 W0,W1,W1,W1,W1,W1,W0,W0,W0,W0,
                                 W0,W0,W0,W0,W0,W0},
                                              {W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W5,W4,W3,W2,W1,
                                W0,W0,W0,W0,W4,W5,W4,W3,W2,W1,
                                W0,W0,W0,W3,W4,W5,W4,W3,W2,W1,
                                W0,W0,W2,W3,W4,W5,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W5,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W5,W4,W3,W2,W0,
                                W0,W1,W2,W3,W4,W5,W4,W3,W0,W0,
                                W0,W1,W2,W3,W4,W5,W4,W0,W0,W0,
                                W0,W1,W2,W3,W4,W5,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0},
                                              {W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W1,W2,W3,W4,W5,
                                W0,W0,W0,W0,W1,W2,W3,W4,W5,W4,
                                W0,W0,W0,W1,W2,W3,W4,W5,W4,W3,
                                W0,W0,W1,W2,W3,W4,W5,W4,W3,W2,
                                W0,W1,W2,W3,W4,W5,W4,W3,W2,W1,
                                W0,W2,W3,W4,W5,W4,W3,W2,W1,W0,
                                W0,W3,W4,W5,W4,W3,W2,W1,W0,W0,
                                W0,W4,W5,W4,W3,W2,W1,W0,W0,W0,
                                W0,W5,W4,W3,W2,W1,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0}},
                                                {{W0,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W1,W1,W1,W1,W1,W1,
                                W0,W0,W0,W0,W0,W2,W2,W2,W2,W2,W2,W2,
                                W0,W0,W0,W0,W3,W3,W3,W3,W3,W3,W3,W3,
                                W0,W0,W0,W4,W4,W4,W4,W4,W4,W4,W4,W4,
                                W0,W0,W5,W5,W5,W5,W5,W5,W5,W5,W5,W5,
                                W0,W6,W6,W6,W6,W6,W6,W6,W6,W6,W6,W6,
                                W0,W5,W5,W5,W5,W5,W5,W5,W5,W5,W5,W0,
                                W0,W4,W4,W4,W4,W4,W4,W4,W4,W4,W0,W0,
                                W0,W3,W3,W3,W3,W3,W3,W3,W3,W0,W0,W0,
                                W0,W2,W2,W2,W2,W2,W2,W2,W0,W0,W0,W0,
                                W0,W1,W1,W1,W1,W1,W1,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W0},
                                                 {W0,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W6,W5,W4,W3,W2,W1,
                                W0,W0,W0,W0,W0,W5,W6,W5,W4,W3,W2,W1,
                                W0,W0,W0,W0,W4,W5,W6,W5,W4,W3,W2,W1,
                                W0,W0,W0,W3,W4,W5,W6,W5,W4,W3,W2,W1,
                                W0,W0,W2,W3,W4,W5,W6,W5,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,W2,W1,
                                W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,W2,W0,
                                W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,W0,W0,
                                W0,W1,W2,W3,W4,W5,W6,W5,W4,W0,W0,W0,
                                W0,W1,W2,W3,W4,W5,W6,W5,W0,W0,W0,W0,
                                W0,W1,W2,W3,W4,W5,W6,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W0},
                                                 {W0,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W1,W2,W3,W4,W5,W6,
                                W0,W0,W0,W0,W0,W1,W2,W3,W4,W5,W6,W5,
                                W0,W0,W0,W0,W1,W2,W3,W4,W5,W6,W5,W4,
                                W0,W0,W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,
                                W0,W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,W2,
                                W0,W1,W2,W3,W4,W5,W6,W5,W4,W3,W2,W1,
                                W0,W2,W3,W4,W5,W6,W5,W4,W3,W2,W1,W0,
                                W0,W3,W4,W5,W6,W5,W4,W3,W2,W1,W0,W0,
                                W0,W4,W5,W6,W5,W4,W3,W2,W1,W0,W0,W0,
                                W0,W5,W6,W5,W4,W3,W2,W1,W0,W0,W0,W0,
                                W0,W6,W5,W4,W3,W2,W1,W0,W0,W0,W0,W0,
                                W0,W0,W0,W0,W0,W0,W0}}};

    //  Evaluate position from the perspective of a given color.
    //  Return a higher value the better the position looks.
    int EvaluatePosition(byte color)
    {
        int i     = side-4;
        int value = 0;

        for(int j = 0; j < 3; j++)
            for(int k = columns; k < size-columns; k++)
                if( board[k] == color)
                    value += weight[i][j][k];
                else if( board[k] == ~color)
                    value -= weight[i][j][k];

        return value;
    }

    //  Evaluate position from the perspective of a given color.
    //  Return a higher value the better the position looks.
    int EvaluatePosition(byte color, int depth)
    {
        return 1000*1000*depth*((int) Math.signum(CountColor(color) - CountColor((byte) ~color)));
    }

    //  Return number of spaces of the given color
    int CountColor(byte color)
    {
        int count = 0;

        for(int i = columns; i < size-columns; i++)
            if( board[i] == color)
                count++;

       return count;
    }
}
