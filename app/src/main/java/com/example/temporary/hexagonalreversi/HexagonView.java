//  HexagonalReversi (c) 2017 Doug Lee

package com.example.temporary.hexagonalreversi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HexagonView extends View
{
    Button           pass;
    TextView         red;
    TextView         blue;
    TextView         status;
    GraphicsPosition position = new GraphicsPosition();

    public HexagonView(Context context)
    {
        super(context);
        requestFocus();
    }

    public HexagonView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        requestFocus();
    }

    public HexagonView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        requestFocus();
    }
   
    protected void onDraw(Canvas canvas)
    {
        Log.d("Hex", "onDraw");
        super.onDraw(canvas);
        position.onDraw(canvas);
    }

    //  Process mouse click event
    public boolean onTouchEvent(@NonNull MotionEvent event)
    {
        return position.onTouchEvent(event);
    }

    void Restart()
    {
        Log.d("Hex", "Restart");
        position = new GraphicsPosition();
        invalidate();
        status.setText(R.string.status_reds_turn);
        pass.setEnabled(true);
        position.KeepScore();
    }

    class PassListener implements OnClickListener
    {
        public void onClick(View v)
        {
            pass.setEnabled(false);
            position.BluesTurn();
        }
    }

    //  Extend Handler so Callback can always refer to latest instance of GraphicsPosition.
    //  That way the current instance can handle a message sent by a previous instance.
    //  This is needed when the device orientation is switched between landscape and portrait.
    //  The old view will process onSaveInstanceState and then be destroyed.
    //  The new view will be created and then process onRestoreInstanceState.
    //  It's important that the new view handles the messages originally sent to the old one.
    static class HexagonHandler extends Handler
    {
        Callback callback = null;

        public void handleMessage(Message msg)
        {
            callback.handleMessage(msg);
        }
    }

    static HexagonHandler handler = new HexagonHandler();

    class GraphicsPosition extends Position implements Callback
    {
        Paint   edges    = new Paint();
        Paint   middle   = new Paint();
        int     delay    = 0;
        int     cxOffset = 0;
        int     cyOffset = 0;
        int     cxOrigin = 0;
        int     cyOrigin = 0;

        //  GraphicsPosition constructor
        GraphicsPosition()
        {
            Log.d("Hex", "GraphicsPosition");

            handler.callback = this;

            //  Translate and copy starting position to board
            for(int i = 0; i < size; i++)
                switch(start.charAt(i))
                {
                    case '*':
                        board[i] = EDGE;
                        break;
                    case ' ':
                        board[i] = BLANK;
                        break;
                    case 'B':
                        board[i] = BLUE;
                        break;
                    case 'R':
                        board[i] = RED;
                        break;
                }
        }

        private final String bundleBoard  = "Board";
        private final String bundleStatus = "Status";
        private final String bundlePass   = "Pass";

        public void PutState(Bundle bundle)
        {
            Log.d("Hex", "PutState");
            bundle.putByteArray(bundleBoard, board);
            bundle.putCharSequence(bundleStatus, status.getText());
            bundle.putBoolean(bundlePass, pass.isEnabled());
        }

        public void GetState(Bundle bundle)
        {
            Log.d("Hex", "GetState");
        //  System.arraycopy(bundle.getByteArray(bundleBoard), 0, board, 0, board.length);
            board = bundle.getByteArray(bundleBoard);
            SetSide((int) Math.sqrt(board.length-1)/2);
            status.setText(bundle.getCharSequence(bundleStatus));
            pass.setEnabled(bundle.getBoolean(bundlePass));
            KeepScore();
        }

        protected void onDraw(Canvas canvas)
        {
            //  Get the bounding dimensions
            int cyPadding = 32;
            int cxDrawing = getWidth();
            int cyDrawing = getHeight()-cyPadding;

            //  Compute the sizes of the sides and altitudes of the equilateral
            //  triangles which form the screen hexagons.  Square root of three
            //  divided by two is the ratio of the altitude to the side.
            //  If size of hexagons is limited by width of client area compute size
            //  of altitude based on the width and compute size of side relative to
            //  altitude otherwise compute size of side based on the height and
            //  compute size of altitude relative to side.
            if( cxDrawing*Math.sqrt(3)/2 < cyDrawing)
            {
                cxOffset = Math.max(2, even(cxDrawing/(rows+rows)));
                cyOffset = (int) (cxOffset/Math.sqrt(3)*2);
                cyOffset = Math.max(2, even(cyOffset));
            }
            else
            {
                cyOffset = Math.max(2, even(cyDrawing/(rows+side)));
                cxOffset = (int) (cyOffset*Math.sqrt(3)/2);
                cxOffset = Math.max(2, cxOffset);
            }

            //  Compute coordinates for center of hexagon at board[side]
            cxOrigin = cxDrawing/2-cxOffset*(3*side-1);
            cyOrigin = cyDrawing/2-cyOffset*(  side-1)*3/2+cyPadding;

            edges.setStyle(Style.STROKE);
            edges.setStrokeWidth(1);

            //  Draw a hexagon for each visible space on the board.  If the
            //  space is occupied draw a circle filled with the proper color.
            for(int i = columns; i < size-columns; i++)
                if( board[i] != EDGE)
                {
                    Path vertices = SetVertices(i);
                    middle.setColor(Color.LTGRAY);
                    canvas.drawPath(vertices, middle);
                    canvas.drawPath(vertices, edges);
                    if( board[i] != BLANK)
                        DrawCircle(i, canvas);
                }
        }

        //  Process mouse click event
        public boolean onTouchEvent(@NonNull MotionEvent event)
        {
            int action = event.getAction();

            if( action == MotionEvent.ACTION_DOWN)
                return true;

            if( action == MotionEvent.ACTION_UP)
            {
                if(!status.getText().equals(getContext().getString(R.string.status_reds_turn))
                && !status.getText().equals(getContext().getString(R.string.status_blue_pass))
                && !status.getText().equals(getContext().getString(R.string.status_red_invalid)))
                    return true;

                int x = (int) event.getX();
                int y = (int) event.getY();

                //  Make a copy of the board for testing
                Position test = new Position(this);

                //  Locate the space under the mouse
                int i = HitTest(x, y);

                //  If the mouse coordinates are within a hexagon which represents a valid
                //  move for RED make the move otherwise set error message.
                if( test.UpdatePosition(i, RED) > 0)
                {
                    pass.setEnabled(false);
                    status.setText(R.string.status_red_moving);
                    UpdatePosition(i, RED);
                }
                else
                    status.setText(R.string.status_red_invalid);

                return true;
            }

            return false;
        }

        //  Return coordinates for the vertices of a given space
        Path SetVertices(int i)
        {
            // Compute center point of hexagon corresponding to space.
            Point p = SetCenter(i);

            // Fill array of points representing the corresponding hexagon by
            // computing offsets from center based on side and altitude sizes.
            int xpoints[] = {p.x,
                             p.x+cxOffset,
                             p.x+cxOffset,
                             p.x,
                             p.x-cxOffset,
                             p.x-cxOffset};
            int ypoints[] = {p.y-cyOffset,
                             p.y-cyOffset/2,
                             p.y+cyOffset/2,
                             p.y+cyOffset,
                             p.y+cyOffset/2,
                             p.y-cyOffset/2};

            Path path = new Path();

            path.moveTo(xpoints[0], ypoints[0]);

            for(int n = 1; n < xpoints.length; n++)
                path.lineTo(xpoints[n], ypoints[n]);

            path.lineTo(xpoints[0], ypoints[0]);

            return path;
        }

        void DrawCircle(int i, Canvas canvas)
        {
            Point p = SetCenter(i);
            int   c;

            //  Set fill color
            switch(board[i])
            {
                case RED:
                    c = Color.RED;
                    break;
                case BLUE:
                    c = Color.BLUE;
                    break;
                default:
                    return;
            }
            
            middle.setColor(c);

            //  Draw a circle which fills 3/4 of the hexagon corresponding to the space.
            canvas.drawCircle(p.x, p.y, 3 * cxOffset / 4, middle);
            canvas.drawCircle(p.x, p.y, 3 * cxOffset / 4, edges);
        }

        //  Return coordinates for the center of a given space
        Point SetCenter(int i)
        {
            //  Compute column and row numbers corresponding to the given space.
            int column = (i-side)%columns;
            int row    = (i-side)/columns;

            //  Compute coordinates for the center of hexagon[row, column].
            //  Each successive row begins 1/2 hexagon width further to the right
            //  and is located 3/4 hexagon height lower within the client area.
            //  This creates an overall hexagon shape because those in the
            //  upper left and lower right corners are never actually drawn.
            return new Point(cxOrigin+row*cxOffset+column*cxOffset*2,
                             cyOrigin+row*cyOffset*3/2);
        }

        //  Convert logical coordinates to a space on the board
        int HitTest(int x, int y)
        {
            Region clip   = new Region(new Rect(0, 0, getRight(), getBottom()));
            Region region = new Region();

            //  For each visible hexagon on the screen create a polygonal region
            //  and test whether the point represented by x and y falls within it.
            //  If and when one is found return the index to the corresponding
            //  space on the board otherwise return zero.
            for(int i = columns; i < size-columns; i++)
            {
                region.setPath(SetVertices(i), clip);
                if( board[i] != EDGE && region.contains(x, y))
                    return i;
            }

            return 0;
        }

        //  Value of nearest even number not greater than...
        int even(int a)
        {
            return a % 2 > 0 ? a-1 : a;
        }

        void RedsTurn()
        {
            if( FirstMove(RED) > 0)
                status.setText(R.string.status_reds_turn);
            else if(FirstMove(BLUE) > 0)
            {
                status.setText(R.string.status_red_pass);
                pass.setEnabled(true);
            }
            else
                EndGame();
        }

        void BluesTurn()
        {
            if( FirstMove(BLUE) > 0)
            {
                status.setText(R.string.status_blues_turn);
                handler.sendEmptyMessageDelayed(1, 1000);
            }
            else if(FirstMove(RED) > 0)
                status.setText(R.string.status_blue_pass);
            else
                EndGame();
        }

        //  Compute score and set message
        void EndGame()
        {
            int score = CountColor(RED)-CountColor(BLUE);

            //  If RED won set congratulatory message including the spread else
            //  if BLUE won set a consolation message including the spread else
            //  set a tie game message.
            if( score > 0)
                status.setText(getContext().getString(R.string.status_red_won) +" "+ score);
            else if( score < 0)
                status.setText(getContext().getString(R.string.status_blue_won)+" "+-score);
            else
                status.setText(R.string.status_tie_game);
        }

        //  Update the score
        void KeepScore()
        {
            red .setText("" + CountColor(RED));
            blue.setText("" + CountColor(BLUE));
        }

        void SendCircle(int i, byte color)
        {
            if( board[i] == BLANK)
            {
                SetCircle(i, color);
                delay = 0;
            }
            else
                handler.sendMessageDelayed(handler.obtainMessage(0, i, color), ++delay*1000);
        }

        void SetCircle(int i, byte color)
        {
            //  Needs Settings -> Sound & notification -> Other sounds -> Touch sounds
            playSoundEffect(SoundEffectConstants.CLICK);
            board[i] = color;
            invalidate();
            KeepScore();
        }

        public boolean handleMessage(Message msg)
        {
            Log.d("Hex", "got message " + msg.arg1 + " " + msg.arg2 + " " + handler.hasMessages(0));

            if( msg.what == 1)
            {
                int i = MinimaxSearch(BLUE, level, minimum, maximum);
                status.setText(R.string.status_blue_moving);
                UpdatePosition(i, BLUE);
                return true;
            }

            SetCircle(msg.arg1, (byte) msg.arg2);

            if(!handler.hasMessages(0))
                if( msg.arg2 == RED)
                    BluesTurn();
                else
                    RedsTurn();

            return true;
        }
    }
}
