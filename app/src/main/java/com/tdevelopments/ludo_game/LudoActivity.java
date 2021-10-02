package com.tdevelopments.ludo_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.tdevelopments.ludo_game.Characters.Ludo.Piece.Piece;
import com.tdevelopments.ludo_game.Characters.Ludo.Player.Player;
import com.tdevelopments.ludo_game.Characters.PathPostion;
import com.tdevelopments.ludo_game.Characters.Position;

import java.util.Random;

public class LudoActivity extends AppCompatActivity {
    private LudoGameView ludoGameView;

    public static PathPostion[] romPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //gameView = new GameView(this);
        Display display = getWindowManager().getDefaultDisplay();

        //Getting the screen resolution into point object
        Point size = new Point();
        display.getSize(size);
        PieceChoice choice = new PieceChoice(2);
        PieceChoice choice1 = new PieceChoice(4);
        PieceChoice choice2 = new PieceChoice(3);
        PieceChoice choice3 = new PieceChoice(1);
        PieceChoice[] choices = new PieceChoice[4];

        choices[0] = choice3;
        choices[1] = choice;
        choices[2] = choice2;
        choices[3] = choice1;

        ludoGameView = new LudoGameView(this,size.x,size.y,choices);
        setContentView(ludoGameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ludoGameView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ludoGameView.onPause();
    }

    enum Turn{
        RED,
        GREEN,
        BLUE,
        YELLOW;
    }

    class PieceChoice{
        public int PlayerId;

        public PieceChoice(int id)
        {
            this.PlayerId = id;
        }
    }

    private class LudoGameView extends SurfaceView implements Runnable{

        private Context context;
        private Thread gameThread;
        private SurfaceHolder holder;
        private Canvas canvas;
        private Paint paint;

        private int ScreenWidth, ScreenHeight;
        private boolean isPlaying, gameFinished = false;

        private Position[] red,blue,yellow,green;

        private Position[] redPath, bluePath,yellowPath,greenPath;

        private int CircleSize;

        private Bitmap bmpStar,bmpStarWhite, bmpMap, bmpDice;

        private Bitmap bmpRedPiece,bmpGreenPiece,bmpYellowPiece,bmpBluePiece;

        private Player playerRed, playerBlue,playerGreen,playerYellow;

        private boolean initRed,initBlue,initGreen,initYellow,moveRed,moveGreen,moveYellow,moveBlue;
        private int placeToMove = 0;

        private String redPieceColor = "#710f01",
                bluePieceColor = "#515DDA",
                greenPieceColor = "#094702",
                yellowPieceColor = "#b5b00b";

        Turn t;

        private int suffleDice = 30;
        private int nextDrawTime = 0,totalPlayers = 0;
        private boolean isShuffling = false, isMoving = false, toSuffle = false, toMove = false, isFactSet = false, toHome = false;

        private Rect rectRed,rectBlue,rectYellow,rectGreen;
        private String winText = "";

        public LudoGameView(Context context, int screenX, int screenY, PieceChoice[] choices) {

            super(context);
            this.context = context;
            this.ScreenWidth = screenX;
            this.ScreenHeight = screenY;

            holder = getHolder();
            paint = new Paint();
            bmpStar = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_star_icon);
            bmpStarWhite = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_star_icon_white);
            bmpDice = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            romPath = new PathPostion[52];
            moveRed = true;

            rectRed = new Rect(50,50,200,200);
            rectBlue = new Rect(ScreenWidth - 200, ScreenHeight - 200, ScreenWidth - 50, ScreenHeight - 50);
            rectGreen = new Rect(ScreenWidth - 200, 50, ScreenWidth - 50, 200);
            rectYellow = new Rect(50, ScreenHeight - 200,200,ScreenHeight - 50);

            totalPlayers = choices.length;

            for (int i = 0; i < choices.length; i++)
            {
                PieceChoice p = choices[i];
                Log.d("LudoActivity","choice : " + p.PlayerId);

                if(p.PlayerId == 1)
                    initRed = true;
                else if(p.PlayerId == 2)
                    initGreen = true;
                else if(p.PlayerId == 3)
                    initBlue = true;
                else if(p.PlayerId == 4)
                    initYellow = true;

                if(i == 0) {
                    if(p.PlayerId == 1)
                        t = Turn.RED;
                    else if(p.PlayerId == 2)
                        t = Turn.GREEN;
                    else if(p.PlayerId == 3)
                        t = Turn.BLUE;
                    else if(p.PlayerId == 4)
                        t = Turn.YELLOW;
                }
            }

            toSuffle = true;
        }

        @Override
        public void run() {
            while (isPlaying) {
                update();
                Draw();
                control();
            }
        }

        void update(){
            if(bmpMap == null) {
                generateMap();
            }

            if(isShuffling)
                SuffleDice();
            else if(isMoving) {
                MovePiece();
            }
            else if(toHome) {
                MoveToHome();
            }
        }

        void generateMap() {
            bmpMap = Bitmap.createBitmap(ScreenWidth,ScreenHeight, Bitmap.Config.ARGB_8888);
            Canvas map = new Canvas(bmpMap);

            float r = 40.0f;
            int blockWidthHight = (int) (ScreenHeight * (r / 100f));
            int left = (ScreenWidth / 2) - (ScreenHeight / 2);
            Log.d("LudoActivity", "block height " + blockWidthHight);

            paint.setColor(Color.BLACK);
            map.drawRect(left - 2 , 0, blockWidthHight + left - 2, blockWidthHight, paint);
            paint.setColor(Color.RED);
            map.drawRect(left, 2, blockWidthHight - 2 + left, blockWidthHight - 2, paint);

            int redBorderWidth = (int) ((float) blockWidthHight * (20f / 100f));
            Log.d("LudoActivity", "redBorder height " + redBorderWidth);

            paint.setColor(Color.BLACK);
            map.drawRect(left + redBorderWidth,
                    redBorderWidth + 2,
                    blockWidthHight - 2 + left - redBorderWidth,
                    blockWidthHight - 2 - redBorderWidth, paint);

            paint.setColor(Color.WHITE);

            map.drawRect(left + redBorderWidth + 2,
                    redBorderWidth + 4,
                    blockWidthHight - 2 + left - redBorderWidth - 2,
                    blockWidthHight - 4 - redBorderWidth, paint);

            int whiteAreaWidth = (blockWidthHight - redBorderWidth) - (redBorderWidth + 4);
            Log.d("LudoActivity", "white area " + whiteAreaWidth);

            int circleWidth = (int) ((float) whiteAreaWidth * (25f / 100f));
            CircleSize = circleWidth - 4;
            Log.d("LudoActivity", "circle width " + circleWidth);

            //RED Circle
            int leftX = (left + redBorderWidth + 2 + circleWidth - 2);
            int leftY = (redBorderWidth + 4 + circleWidth - 2);

            red = new Position[4];
            Position p = new Position(leftX, leftY);
            red[0] = p;

            paint.setColor(Color.BLACK);
            map.drawCircle((float) leftX, (float) leftY, circleWidth / 2, paint);

            paint.setColor(Color.RED);

            map.drawCircle((float) leftX, (float) leftY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);
            int rightX = left + blockWidthHight - redBorderWidth - 2 - circleWidth;
            int rightY = redBorderWidth + 4 + circleWidth - 2;

            p = new Position(rightX, rightY);
            red[1] = p;
            map.drawCircle((float) rightX,
                    (float) rightY, circleWidth / 2, paint);

            paint.setColor(Color.RED);

            map.drawCircle((float) rightX,
                    (float) rightY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);

            int bottomLeftY = blockWidthHight - redBorderWidth - 2 - circleWidth;

            p = new Position(leftX, bottomLeftY);
            red[2] = p;
            map.drawCircle((float) leftX, (float) bottomLeftY, circleWidth / 2, paint);

            paint.setColor(Color.RED);

            map.drawCircle((float) leftX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            p = new Position(rightX, bottomLeftY);
            red[3] = p;
            paint.setColor(Color.BLACK);
            map.drawCircle((float) rightX, (float) bottomLeftY, circleWidth / 2, paint);
            paint.setColor(Color.RED);
            map.drawCircle((float) rightX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            //Bottom Part(Yellow)

            int topY = ScreenHeight - blockWidthHight;
            paint.setColor(Color.BLACK);
            map.drawRect(left - 2, topY, blockWidthHight + left - 2, ScreenHeight, paint);
            paint.setColor(Color.YELLOW);
            map.drawRect(left, topY, blockWidthHight - 2 + left, ScreenHeight - 2, paint);
            Log.d("LudoActivity", "redBorder height " + redBorderWidth);

            paint.setColor(Color.BLACK);
            map.drawRect(left + redBorderWidth,
                    topY + redBorderWidth + 2,
                    blockWidthHight - 2 + left - redBorderWidth,
                    ScreenHeight - 2 - redBorderWidth, paint);

            paint.setColor(Color.WHITE);

            map.drawRect(left + redBorderWidth + 2,
                    topY + redBorderWidth + 4,
                    blockWidthHight - 2 + left - redBorderWidth - 2,
                    ScreenHeight - 4 - redBorderWidth, paint);

            //YELLOW Circle
            leftX = (left + redBorderWidth + circleWidth);
            leftY = (topY + redBorderWidth + 4 + circleWidth - 2);

            yellow = new Position[4];
            p = new Position(leftX, leftY);
            yellow[0] = p;

            paint.setColor(Color.BLACK);
            map.drawCircle((float) leftX, (float) leftY, circleWidth / 2, paint);

            paint.setColor(Color.YELLOW);

            map.drawCircle((float) leftX, (float) leftY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);
            rightX = left + blockWidthHight - redBorderWidth - 2 - circleWidth;
            rightY = topY + redBorderWidth + 4 + circleWidth - 2;

            p = new Position(rightX, rightY);
            yellow[1] = p;
            map.drawCircle((float) rightX,
                    (float) rightY, circleWidth / 2, paint);

            paint.setColor(Color.YELLOW);

            map.drawCircle((float) rightX,
                    (float) rightY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);

            bottomLeftY = ScreenHeight - redBorderWidth - 2 - circleWidth;

            p = new Position(leftX, bottomLeftY);
            yellow[2] = p;
            map.drawCircle((float) leftX, (float) bottomLeftY, circleWidth / 2, paint);

            paint.setColor(Color.YELLOW);

            map.drawCircle((float) leftX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            p = new Position(rightX, bottomLeftY);
            yellow[3] = p;
            paint.setColor(Color.BLACK);
            map.drawCircle((float) rightX, (float) bottomLeftY, circleWidth / 2, paint);
            paint.setColor(Color.YELLOW);
            map.drawCircle((float) rightX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);


            //Top right (GREEN)

            int topRightX = ScreenHeight - blockWidthHight + left;
            paint.setColor(Color.BLACK);
            map.drawRect(topRightX - 2, 0, ScreenHeight + left, blockWidthHight, paint);
            paint.setColor(Color.GREEN);
            map.drawRect(topRightX + 2, 2, ScreenHeight + left - 2, blockWidthHight - 2, paint);

            paint.setColor(Color.BLACK);
            map.drawRect(topRightX + redBorderWidth,
                    redBorderWidth + 2,
                    left + ScreenHeight - redBorderWidth,
                    blockWidthHight - 2 - redBorderWidth, paint);

            paint.setColor(Color.WHITE);

            map.drawRect(topRightX + redBorderWidth + 2,
                    redBorderWidth + 4,
                    left + ScreenHeight - redBorderWidth - 2,
                    blockWidthHight - 4 - redBorderWidth, paint);


            //GREEN Circle
            leftX = (topRightX + redBorderWidth + 2 + circleWidth - 2);
            leftY = (redBorderWidth + 4 + circleWidth - 2);

            green = new Position[4];
            p = new Position(leftX, leftY);
            green[0] = p;

            paint.setColor(Color.BLACK);
            map.drawCircle((float) leftX, (float) leftY, circleWidth / 2, paint);

            paint.setColor(Color.GREEN);

            map.drawCircle((float) leftX, (float) leftY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);
            rightX = topRightX + blockWidthHight - redBorderWidth - 2 - circleWidth;
            rightY = redBorderWidth + 4 + circleWidth - 2;

            p = new Position(rightX, rightY);
            green[1] = p;
            map.drawCircle((float) rightX,
                    (float) rightY, circleWidth / 2, paint);

            paint.setColor(Color.GREEN);

            map.drawCircle((float) rightX,
                    (float) rightY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);

            bottomLeftY = blockWidthHight - redBorderWidth - 2 - circleWidth;

            p = new Position(leftX, bottomLeftY);
            green[2] = p;
            map.drawCircle((float) leftX, (float) bottomLeftY, circleWidth / 2, paint);

            paint.setColor(Color.GREEN);

            map.drawCircle((float) leftX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            p = new Position(rightX, bottomLeftY);
            green[3] = p;
            paint.setColor(Color.BLACK);
            map.drawCircle((float) rightX, (float) bottomLeftY, circleWidth / 2, paint);
            paint.setColor(Color.GREEN);
            map.drawCircle((float) rightX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);


            //BottomRight (Blue)

            paint.setColor(Color.BLACK);
            map.drawRect(topRightX - 2, topY, ScreenHeight + left, ScreenHeight, paint);
            paint.setColor(Color.BLUE);
            map.drawRect(topRightX + 2, topY - 2, ScreenHeight + left - 2, ScreenHeight - 2, paint);

            paint.setColor(Color.BLACK);
            map.drawRect(topRightX + redBorderWidth,
                    topY + redBorderWidth + 2,
                    left + ScreenHeight - redBorderWidth,
                    ScreenHeight - 2 - redBorderWidth, paint);

            paint.setColor(Color.WHITE);

            map.drawRect(topRightX + redBorderWidth + 2,
                    topY + redBorderWidth + 4,
                    left + ScreenHeight - redBorderWidth - 2,
                    ScreenHeight - 4 - redBorderWidth, paint);

            //BLUE Circle
            leftX = (topRightX + redBorderWidth + circleWidth);
            leftY = (topY + redBorderWidth + 4 + circleWidth - 2);

            blue = new Position[4];
            p = new Position(leftX, leftY);
            blue[0] = p;

            paint.setColor(Color.BLACK);
            map.drawCircle((float) leftX, (float) leftY, circleWidth / 2, paint);

            paint.setColor(Color.BLUE);

            map.drawCircle((float) leftX, (float) leftY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);
            rightX = topRightX + blockWidthHight - redBorderWidth - 2 - circleWidth;
            rightY = topY + redBorderWidth + 4 + circleWidth - 2;

            p = new Position(rightX, rightY);
            blue[1] = p;
            map.drawCircle((float) rightX, (float) rightY, circleWidth / 2, paint);

            paint.setColor(Color.BLUE);

            map.drawCircle((float) rightX, (float) rightY, (circleWidth - 4) / 2, paint);

            paint.setColor(Color.BLACK);

            bottomLeftY = ScreenHeight - redBorderWidth - 2 - circleWidth;

            p = new Position(leftX, bottomLeftY);
            blue[2] = p;
            map.drawCircle((float) leftX, (float) bottomLeftY, circleWidth / 2, paint);

            paint.setColor(Color.BLUE);

            map.drawCircle((float) leftX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            p = new Position(rightX, bottomLeftY);
            blue[3] = p;
            paint.setColor(Color.BLACK);
            map.drawCircle((float) rightX, (float) bottomLeftY, circleWidth / 2, paint);
            paint.setColor(Color.BLUE);
            map.drawCircle((float) rightX, (float) bottomLeftY, (circleWidth - 4) / 2, paint);

            //Draw Path Now

            int pathWidth = (int) ((float) ScreenHeight * (20f / 100f));
            Log.d("LudoActivity", "Path Width : " + pathWidth);
            int siglePathWidth = pathWidth / 3;

            Bitmap scaledBmp = Bitmap.createScaledBitmap(bmpStar, siglePathWidth, siglePathWidth, true);
            Bitmap scaledBmpWhite = Bitmap.createScaledBitmap(bmpStarWhite, siglePathWidth, siglePathWidth, true);


            //path between red and yellow
            redPath = new Position[6];
            for (int i = 0; i < 3; i++) {
                int pathY = blockWidthHight + (siglePathWidth * i);
                int pathX = left;
                for (int j = 0; j < 6; j++) {
                    paint.setColor(Color.BLACK);
                    map.drawRect(pathX - 2, pathY, pathX + siglePathWidth,
                            pathY + siglePathWidth, paint);

                    if ((i == 0 && j == 1)) {
                        paint.setColor(Color.RED);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);
                        map.drawBitmap(scaledBmpWhite, (float) pathX - 2, (float) pathY, null);

                    } else if ((i == 1 && j > 0)) {
                        paint.setColor(Color.RED);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);

                        if (j == 5)
                            redPath[5] = new Position(pathX + siglePathWidth, pathY);
                        p = new Position(pathX, pathY);
                        redPath[j - 1] = p;

                    } else {
                        paint.setColor(Color.WHITE);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);
                    }

                    if (i == 2 && j == 1)
                        map.drawBitmap(scaledBmp, pathX, pathY, null);

                    switch (i) {
                        case 0:
                            if (j == 1) {
                                romPath[1] = new PathPostion(pathX, pathY, true);
                            } else {
                                romPath[j] = new PathPostion(pathX, pathY, false);
                            }
                            break;
                        case 1:
                            if (j == 0)
                                romPath[51] = new PathPostion(pathX, pathY, false);
                            break;
                        case 2:
                            if (j == 1)
                                romPath[50 - j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[50 - j] = new PathPostion(pathX, pathY, false);
                            break;
                    }

                    pathX += siglePathWidth;
                }
            }

            //path between green and red
            greenPath = new Position[6];
            for (int i = 0; i < 3; i++) {
                int pathY = 0; //blockWidthHight + (siglePathWidth * i);
                int pathX = left + blockWidthHight + (siglePathWidth * i);
                for (int j = 0; j < 6; j++) {
                    paint.setColor(Color.BLACK);
                    map.drawRect(pathX - 2, pathY, pathX + siglePathWidth, pathY + siglePathWidth, paint);

                    if ((i == 1 && j > 0)) {
                        paint.setColor(Color.GREEN);
                        map.drawRect(pathX, pathY + 2, pathX + siglePathWidth,
                                pathY + siglePathWidth, paint);
                        if (j == 5)
                            greenPath[5] = new Position(pathX, pathY + siglePathWidth);

                        greenPath[j - 1] = new Position(pathX, pathY);

                    } else if (i == 2 && j == 1) {
                        paint.setColor(Color.GREEN);
                        map.drawRect(pathX, pathY + 2, pathX + siglePathWidth,
                                pathY + siglePathWidth, paint);

                        map.drawBitmap(scaledBmpWhite, (float) pathX - 2, (float) pathY, null);
                    } else {
                        paint.setColor(Color.WHITE);
                        map.drawRect(pathX, pathY + 2, pathX + siglePathWidth,
                                pathY + siglePathWidth, paint);
                    }

                    if (i == 0 && j == 1) {
                        map.drawBitmap(scaledBmp, pathX, pathY, null);
                    }

                    switch (i) {
                        case 0:
                            if (j == 1)
                                romPath[11 - j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[11 - j] = new PathPostion(pathX, pathY, false);
                            break;
                        case 1:
                            if (j == 0)
                                romPath[12] = new PathPostion(pathX, pathY, false);
                            break;
                        case 2:
                            if (j == 1)
                                romPath[13 + j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[13 + j] = new PathPostion(pathX, pathY, false);
                            break;
                    }

                    pathY += siglePathWidth;
                }
            }

            //path between green and blue
            bluePath = new Position[6];
            for (int i = 0; i < 3; i++) {
                int pathY = blockWidthHight + (siglePathWidth * i);
                int pathX = left + ScreenHeight - siglePathWidth;
                for (int j = 0; j < 6; j++) {
                    paint.setColor(Color.BLACK);
                    map.drawRect(pathX, pathY, pathX + siglePathWidth, pathY + siglePathWidth, paint);

                    if ((i == 1 && j > 0)) {
                        paint.setColor(Color.BLUE);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth - 2,
                                pathY + siglePathWidth - 2, paint);
                        p = new Position(pathX, pathY);
                        bluePath[j - 1] = p;

                        if (j == 5)
                            bluePath[5] = new Position(pathX - siglePathWidth, pathY);
                    } else if (i == 2 && j == 1) {
                        paint.setColor(Color.BLUE);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth - 2,
                                pathY + siglePathWidth - 2, paint);
                        map.drawBitmap(scaledBmpWhite, (float) pathX - 2, (float) pathY, null);
                    } else {
                        paint.setColor(Color.WHITE);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth - 2,
                                pathY + siglePathWidth - 2, paint);
                    }

                    if (i == 0 && j == 1)
                        map.drawBitmap(scaledBmp, pathX, pathY, null);

                    switch (i) {
                        case 0:
                            if (j == 1)
                                romPath[24 - j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[24 - j] = new PathPostion(pathX, pathY, false);
                            break;
                        case 1:
                            if (j == 0)
                                romPath[25] = new PathPostion(pathX, pathY, false);
                            break;
                        case 2:
                            if (j == 1)
                                romPath[26 + j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[26 + j] = new PathPostion(pathX, pathY, false);
                            break;
                    }
                    pathX -= siglePathWidth;
                }
            }

            //path between yellow and blue
            yellowPath = new Position[6];
            for (int i = 0; i < 3; i++) {
                int pathY = ScreenHeight - blockWidthHight; //blockWidthHight + (siglePathWidth * i);
                int pathX = left + blockWidthHight + (siglePathWidth * i);
                for (int j = 0; j < 6; j++) {
                    paint.setColor(Color.BLACK);
                    map.drawRect(pathX - 2, pathY, pathX + siglePathWidth, pathY + siglePathWidth, paint);

                    if ((i == 0 && j == 4)) {
                        paint.setColor(Color.YELLOW);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);
                        map.drawBitmap(scaledBmpWhite, (float) pathX - 2, (float) pathY, null);

                    } else if (i == 1 && j < 5) {
                        Log.d("LudoActivity","In Yellow");
                        Log.d("LudoActivity","i is " + i + " & j is " + j);
                        paint.setColor(Color.YELLOW);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);
                        if (j == 0) {
                            yellowPath[5] = new Position(pathX, pathY - siglePathWidth);
                        }

                        yellowPath[4 - j] = new Position(pathX, pathY);
                    } else {
                        paint.setColor(Color.WHITE);
                        map.drawRect(pathX, pathY, pathX + siglePathWidth,
                                pathY + siglePathWidth - 2, paint);
                    }

                    if (i == 2 && j == 4)
                        map.drawBitmap(scaledBmp, pathX, pathY, null);

                    switch (i) {
                        case 0:
                            if (j == 4)
                                romPath[44 - j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[44 - j] = new PathPostion(pathX, pathY, false);
                            break;
                        case 1:
                            if (j == 5)
                                romPath[38] = new PathPostion(pathX, pathY, false);
                            break;
                        case 2:
                            if (j == 4)
                                romPath[32 + j] = new PathPostion(pathX, pathY, true);
                            else
                                romPath[32 + j] = new PathPostion(pathX, pathY, false);
                    }
                    pathY += siglePathWidth;
                }
            }

            for (int i = 0; i < greenPath.length; i++)
            {
                Log.d("LudoActivity","Green path " + i + " : " + greenPath[i].toString());
            }

            for (int i = 0; i < yellowPath.length; i++)
            {
                if(yellowPath[i] == null)
                    Log.d("LudoActivity","Yellow path at " + i + " is null");
                else
                    Log.d("LudoActivity","Yellow path " + i + " : " + yellowPath[i].toString());
            }

            if(initRed) {
                if (playerRed == null) {
                    playerRed = new Player(1, 50, red, siglePathWidth,redPath,redPieceColor);
                    playerRed.setArrows(new int[]{red[0].getX(),red[1].getX(),red[2].getX(),red[3].getX()},
                            new int[]{red[0].getY(),red[1].getY(),red[2].getY(),red[3].getY()},CircleSize);
                }
            }

            if(initYellow)
            {
                if(playerYellow == null) {
                    Log.d("LudoActivity","Yellow Path");
                    playerYellow = new Player(40, 38, yellow, siglePathWidth,yellowPath,yellowPieceColor);
                    playerYellow.setArrows(new int[]{yellow[0].getX(),yellow[1].getX(),yellow[2].getX(),yellow[3].getX()},
                            new int[]{yellow[0].getY(),yellow[1].getY(),yellow[2].getY(),yellow[3].getY()},CircleSize);
                }
            }

            if(initGreen)
            {
                if(playerGreen == null) {
                    playerGreen = new Player(14, 12, green, siglePathWidth,greenPath,greenPieceColor);
                    playerGreen.setArrows(new int[]{green[0].getX(),green[1].getX(),green[2].getX(),green[3].getX()},
                            new int[]{green[0].getY(),green[1].getY(),green[2].getY(),green[3].getY()},CircleSize);
                }
            }

            if(initBlue)
            {
                if(playerBlue == null) {
                    playerBlue = new Player(27, 25, blue, siglePathWidth,bluePath,bluePieceColor);
                    playerBlue.setArrows(new int[]{blue[0].getX(),blue[1].getX(),blue[2].getX(),blue[3].getX()},
                            new int[]{blue[0].getY(),blue[1].getY(),blue[2].getY(),blue[3].getY()},CircleSize);
                }
            }

            paint.setColor(Color.BLACK);
            map.drawRect(left + blockWidthHight, blockWidthHight,
                    left + blockWidthHight + pathWidth, blockWidthHight + pathWidth, paint);
//                        paint.setColor(Color.RED);
//                        map.drawRect(50 + blockWidthHight+2,blockWidthHight+2,
//                        50 + blockWidthHight + pathWidth-2,blockWidthHight + pathWidth-2,paint);

            paint.setColor(Color.RED);
            Path path = new Path();
            path.moveTo(left + blockWidthHight + 2, blockWidthHight + 2);
            path.lineTo(left + blockWidthHight + 2, blockWidthHight + pathWidth - 2);
            path.lineTo(left + blockWidthHight + (pathWidth / 2), blockWidthHight + (pathWidth / 2));
            path.lineTo(left + blockWidthHight + 2, blockWidthHight + 2);
            path.close();
            map.drawPath(path, paint);

            paint.setColor(Color.BLUE);

            path = new Path();
            path.moveTo(left + blockWidthHight + pathWidth - 2, blockWidthHight + 2);
            path.lineTo(left + blockWidthHight + pathWidth - 2, blockWidthHight + pathWidth - 2);
            path.lineTo(left + blockWidthHight + (pathWidth / 2), blockWidthHight + (pathWidth / 2));
            path.lineTo(left + blockWidthHight + pathWidth - 2, blockWidthHight + 2);
            path.close();

            map.drawPath(path, paint);

            paint.setColor(Color.GREEN);
            path = new Path();
            path.moveTo(left + blockWidthHight + 2, blockWidthHight + 2);
            path.lineTo(left + blockWidthHight + pathWidth - 2, blockWidthHight + 2);
            path.lineTo(left + blockWidthHight + (pathWidth / 2), blockWidthHight + (pathWidth / 2));
            path.lineTo(left + blockWidthHight + 2, blockWidthHight + 2);
            path.close();

            map.drawPath(path, paint);

            paint.setColor(Color.YELLOW);
            path = new Path();
            path.moveTo(left + blockWidthHight + 2, blockWidthHight + pathWidth - 2);
            path.lineTo(left + blockWidthHight + pathWidth - 2, blockWidthHight + pathWidth - 2);
            path.lineTo(left + blockWidthHight + (pathWidth / 2), blockWidthHight + (pathWidth / 2));
            path.lineTo(left + blockWidthHight + 2, blockWidthHight + pathWidth - 2);
            path.close();

            map.drawPath(path, paint);
            map.save();

            //Generating pieces with Colors
            bmpRedPiece =  Bitmap.createBitmap(siglePathWidth,siglePathWidth, Bitmap.Config.ARGB_8888);

            Canvas redPiece = new Canvas(bmpRedPiece);
            //redPiece.drawColor(Color.GRAY);
            int pieceWidth = (int)(siglePathWidth * (80f/100f));
            int pieceHeight = siglePathWidth;

            int x = siglePathWidth /2 - (pieceWidth / 2);
            int y = 0;

            paint.setColor(Color.parseColor(redPieceColor));

            redPiece.drawCircle(siglePathWidth /2,pieceWidth/2,pieceWidth / 2,paint);
            path = new Path();
            path.moveTo(x,pieceWidth/2);
            path.lineTo(siglePathWidth /2,pieceHeight);
            path.lineTo(x+pieceWidth,pieceWidth/2);
            path.close();
            redPiece.drawPath(path,paint);
            paint.setColor(Color.WHITE);
            redPiece.drawCircle(siglePathWidth /2,pieceWidth/2,10,paint);
            redPiece.save();

            bmpBluePiece = Bitmap.createBitmap(siglePathWidth,siglePathWidth, Bitmap.Config.ARGB_8888);

            Canvas bluePiece = new Canvas(bmpBluePiece);
            paint.setColor(Color.parseColor(bluePieceColor));
            bluePiece.drawCircle(siglePathWidth /2,pieceWidth/2,pieceWidth / 2,paint);
            bluePiece.drawPath(path,paint);
            paint.setColor(Color.WHITE);
            bluePiece.drawCircle(siglePathWidth /2,pieceWidth/2,10,paint);
            bluePiece.save();

            bmpYellowPiece = Bitmap.createBitmap(siglePathWidth,siglePathWidth, Bitmap.Config.ARGB_8888);

            Canvas yellowPiece = new Canvas(bmpYellowPiece);
            paint.setColor(Color.parseColor(yellowPieceColor));
            yellowPiece.drawCircle(siglePathWidth /2,pieceWidth/2,pieceWidth / 2,paint);
            yellowPiece.drawPath(path,paint);
            paint.setColor(Color.WHITE);
            yellowPiece.drawCircle(siglePathWidth /2,pieceWidth/2,10,paint);
            yellowPiece.save();

            bmpGreenPiece = Bitmap.createBitmap(siglePathWidth,siglePathWidth, Bitmap.Config.ARGB_8888);

            Canvas greenPiece = new Canvas(bmpGreenPiece);
            paint.setColor(Color.parseColor(greenPieceColor));
            greenPiece.drawCircle(siglePathWidth /2,pieceWidth/2,pieceWidth / 2,paint);
            greenPiece.drawPath(path,paint);
            paint.setColor(Color.WHITE);
            greenPiece.drawCircle(siglePathWidth /2,pieceWidth/2,10,paint);
            greenPiece.save();
        }

        void Draw(){
            if(holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bmpMap,0,0,null);

                if (t == Turn.RED)
                    canvas.drawBitmap(bmpDice, 50, 50, null);
                else if (t == Turn.GREEN)
                    canvas.drawBitmap(bmpDice, ScreenWidth - 200, 50, null);
                else if (t == Turn.BLUE)
                    canvas.drawBitmap(bmpDice, ScreenWidth - 200, ScreenHeight - 200, null);
                else
                    canvas.drawBitmap(bmpDice, 50, ScreenHeight - 200, null);

                DrawPlayerPiece();
                //canvas.drawBitmap(bmpRedPiece,ScreenWidth - 60,50,null);
                DrawArrows();

                if(gameFinished)
                {
                    paint.setTextSize(150);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(winText,canvas.getWidth() / 2,canvas.getHeight() / 2,paint);
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

        private void MoveToHome() {

            if(initRed) {
                Piece[] pieces = playerRed.getRedPiecePositions();
                for (int i = 0; i < 4; i++) {
                    Piece p = pieces[i];
                    if (p.getHome()) {
                        if (p.UpdateToHome()) {
                            toHome = false;
                            SetNextTurn();
                        }
                        return;
                    }
                }
            }

            if(initBlue) {
                Piece[] pieces = playerBlue.getRedPiecePositions();
                for (int i = 0; i < 4; i++) {
                    Piece p = pieces[i];
                    if (p.getHome()) {
                        if (p.UpdateToHome()) {
                            toHome = false;
                            SetNextTurn();
                        }
                        return;
                    }
                }
            }

            if(initGreen) {
                Piece[] pieces = playerGreen.getRedPiecePositions();
                for (int i = 0; i < 4; i++) {
                    Piece p = pieces[i];
                    if (p.getHome()) {
                        if (p.UpdateToHome()) {
                            toHome = false;
                            SetNextTurn();
                        }
                        return;
                    }
                }
            }

            if(initYellow) {
                Piece[] pieces = playerYellow.getRedPiecePositions();
                for (int i = 0; i < 4; i++) {
                    Piece p = pieces[i];
                    if (p.getHome()) {
                        if (p.UpdateToHome()) {
                            toHome = false;
                            SetNextTurn();
                        }
                        return;
                    }
                }
            }
        }

        private void MovePiece() {

            if(t == Turn.RED)
            {
                Piece[] pieces = playerRed.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    if(p.getMove())
                    {
                        if(p.UpdatePosition())
                        {
                            boolean clear = true;
                            p.setMove(false);
                            isMoving = false;
                            toMove = false;
                            if(initGreen) {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerGreen.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(initBlue)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerBlue.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(initYellow)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerYellow.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(clear) {
                                if(p.getComplete())
                                {
                                    Log.d("LudoActivity","Piece Complete");
                                    int com = 0;
                                    for (int j = 0; j < 4; j++) {
                                        if(pieces[j].getComplete())
                                            com++;
                                    }

                                    if(com == 4) {
                                        playerRed.setWin(true);
                                        checkGameOver();
                                    }
                                    else {
                                        SetNextTurn();
                                    }

                                }
                                else
                                    SetNextTurn();
                            }
                            else
                            {
                                playerRed.setIsPass();
                            }
                        }
                        break;
                    }
                }
            }
            else if(t == Turn.GREEN)
            {
                Piece[] pieces = playerGreen.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    if(p.getMove())
                    {
                        if(p.UpdatePosition())
                        {
                            boolean clear = true;
                            p.setMove(false);
                            isMoving = false;
                            toMove = false;
                            if(initRed) {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerRed.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(initBlue)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerBlue.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(initYellow)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerYellow.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(clear) {
                                if(p.getComplete())
                                {
                                    Log.d("LudoActivity","Piece Complete");
                                    int com = 0;
                                    for (int j = 0; j < 4; j++) {
                                        if(pieces[j].getComplete())
                                            com++;
                                    }

                                    if(com == 4) {
                                        playerGreen.setWin(true);
                                        checkGameOver();
                                    }
                                    else {
                                        SetNextTurn();
                                    }

                                }
                                else
                                    SetNextTurn();
                            }
                            else
                            {
                                playerGreen.setIsPass();
                            }
                        }
                        break;
                    }
                }
            }
            else if(t == Turn.BLUE)
            {
                Piece[] pieces = playerBlue.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    if(p.getMove())
                    {
                        if(p.UpdatePosition())
                        {
                            boolean clear = true;
                            p.setMove(false);
                            isMoving = false;
                            toMove = false;
                            if(initRed) {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerRed.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(initGreen)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerGreen.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(initYellow)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerYellow.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(clear) {
                                if(p.getComplete())
                                {
                                    Log.d("LudoActivity","Piece Complete");
                                    int com = 0;
                                    for (int j = 0; j < 4; j++) {
                                        if(pieces[j].getComplete())
                                            com++;
                                    }

                                    if(com == 4) {
                                        playerBlue.setWin(true);
                                        checkGameOver();
                                    }
                                    else {
                                        SetNextTurn();
                                    }

                                }
                                else
                                    SetNextTurn();
                            }
                            else
                            {
                                playerBlue.setIsPass();
                            }
                        }
                        break;
                    }
                }
            }
            else if(t == Turn.YELLOW)
            {
                Piece[] pieces = playerYellow.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    if(p.getMove())
                    {
                        if(p.UpdatePosition())
                        {
                            p.setMove(false);
                            boolean clear = true;
                            toMove = false;
                            isMoving = false;

                            if(initRed) {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerRed.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(initBlue)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerBlue.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(initGreen)
                            {
                                for (int j = 0; j < 4; j++)
                                {
                                    Piece p1 = playerGreen.getRedPiecePositions()[j];
                                    if(p.getCollision().intersect(p1.getCollision()))
                                    {
                                        if(!p.getIsStar(p.getTarget()))
                                        {
                                            p1.setHome(true);
                                            toHome = true;
                                            clear = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(clear) {
                                if(p.getComplete())
                                {
                                    Log.d("LudoActivity","Piece Complete");
                                    int com = 0;
                                    for (int j = 0; j < 4; j++) {
                                        if(pieces[j].getComplete())
                                            com++;
                                    }

                                    if(com == 4) {
                                        playerYellow.setWin(true);
                                        checkGameOver();
                                    }
                                    else {
                                        SetNextTurn();
                                    }

                                }
                                else
                                    SetNextTurn();
                            }
                            else
                            {
                                playerYellow.setIsPass();
                            }
                        }
                        break;
                    }
                }
            }
        }

        private void checkGameOver() {
            if(totalPlayers == 2) {
                if(t == Turn.RED) {
                    winText = "Player RED wins!!!";
                }
                else if(t == Turn.BLUE)
                    winText = "Player BLUE wins!!!";
                else if(t == Turn.GREEN)
                    winText = "Player GREEN wins!!!";
                else if(t == Turn.YELLOW)
                    winText = "Player YELLOW wins!!!";

            }
            else
            {
                int f = 0;
                int l = 0;

                if(initRed && playerRed.getWin())
                    f++;
                else
                    l = 1;

                if(initGreen && playerGreen.getWin())
                    f++;
                else
                    l = 2;

                if(initBlue && playerBlue.getWin())
                    f++;
                else
                    l = 3;

                if(initYellow && playerYellow.getWin())
                    f++;
                else
                    l = 4;

                if(f == totalPlayers - 1) {

                    switch (l)
                    {
                        case 1:
                            winText = "Player RED Lost";
                            break;
                        case 2:
                            winText = "Player GREEN Lost";
                            break;
                        case 3:
                            winText = "Player BLUE Lost";
                            break;
                        case 4:
                            winText = "Player YELLOW Lost";
                    }
                }

            }

            gameFinished = true;

        }

        private void SetNextTurn() {

            if (!gameFinished) {
                if(placeToMove == 6)
                {
                    toSuffle = true;
                    placeToMove = 0;
                    return;
                }
                placeToMove = 0;

                if (t == Turn.RED) {
                    if (playerGreen != null && playerGreen.getWin() == false) {
                        t = Turn.GREEN;
                        toSuffle = true;
                    } else if (playerBlue != null && playerBlue.getWin() == false) {
                        t = Turn.BLUE;
                        toSuffle = true;
                    } else if (playerYellow != null && playerYellow.getWin() == false) {
                        t = Turn.YELLOW;
                        toSuffle = true;
                    }
                } else if (t == Turn.GREEN) {
                    if (playerBlue != null && playerGreen.getWin() == false) {
                        t = Turn.BLUE;
                        toSuffle = true;
                    } else if (playerYellow != null && playerYellow.getWin() == false) {
                        t = Turn.YELLOW;
                        toSuffle = true;
                    } else if (playerRed != null && playerRed.getWin() == false) {
                        t = Turn.RED;
                        toSuffle = true;
                    }
                } else if (t == Turn.BLUE) {
                    if (playerYellow != null && playerYellow.getWin() == false) {
                        t = Turn.YELLOW;
                        toSuffle = true;
                    } else if (playerRed != null && playerRed.getWin() == false) {
                        t = Turn.RED;
                        toSuffle = true;
                    } else if (playerGreen != null && playerGreen.getWin() == false) {
                        t = Turn.GREEN;
                        toSuffle = true;
                    }

                } else if (t == Turn.YELLOW) {
                    if (playerRed != null && playerRed.getWin() == false) {
                        t = Turn.RED;
                        toSuffle = true;
                    } else if (playerGreen != null && playerGreen.getWin() == false) {
                        t = Turn.GREEN;
                        toSuffle = true;
                    } else if (playerBlue != null && playerBlue.getWin() == false) {
                        t = Turn.BLUE;
                        toSuffle = true;
                    }
                }
            }
        }

        private void SuffleDice() {
            Random r = new Random();
            int next = r.nextInt(6) + 1;
            if(suffleDice <= 0) {
                suffleDice = 100;
                isShuffling = false;
                toSuffle = false;
                placeToMove = getNextNo(next);
                checkCanMove();
            }
            else {
                getNextNo(next);
                suffleDice--;
            }
        }

        private void checkCanMove() {
            if(t == Turn.RED)
            {
                Piece[] pieces = playerRed.getRedPiecePositions();
                int moveCount = 0;
                int id = 0;
                for (int i= 0; i < pieces.length; i++)
                {
                    if((pieces[i].getIsKilled() == false || placeToMove == 6) &&
                            (pieces[i].getComplete() == false && playerRed.checkCanMove(i,placeToMove)))
                    {
                        toMove = true;
                        moveCount++;
                        id = i;
                    }
                }
                if(toMove == false)
                    nextDrawTime = 30;
                else if(moveCount == 1)
                {
                    isMoving = true;
                    if(pieces[id].getComplete() == false) {
                        if (playerRed.checkCanMove(id, placeToMove)) {
                            isMoving = true;
                            pieces[id].setTarget(placeToMove);
                            pieces[id].setMove(true);
                        }
                        else {
                            SetNextTurn();
                        }
                    }
                }
            }
            else if(t == Turn.BLUE)
            {
                Piece[] pieces = playerBlue.getRedPiecePositions();
                int moveCount = 0;
                int id = 0;
                for (int i= 0; i < pieces.length; i++)
                {
                    if((pieces[i].getIsKilled() == false || placeToMove == 6) &&
                            (pieces[i].getComplete() == false && playerBlue.checkCanMove(i,placeToMove)))
                    {
                        toMove = true;
                        moveCount++;
                        id = i;
                    }
                }
                if(toMove == false)
                    nextDrawTime = 30;
                else if(moveCount == 1)
                {
                    isMoving = true;
                    if(pieces[id].getComplete() == false) {
                        if (playerBlue.checkCanMove(id, placeToMove)) {
                            isMoving = true;
                            pieces[id].setTarget(placeToMove);
                            pieces[id].setMove(true);
                        }
                        else {
                            SetNextTurn();
                        }
                    }
                }
            }
            else if(t == Turn.GREEN)
            {
                Piece[] pieces = playerGreen.getRedPiecePositions();
                int moveCount = 0;
                int id = 0;
                for (int i= 0; i < pieces.length; i++)
                {
                    if((pieces[i].getIsKilled() == false || placeToMove == 6) &&
                            (pieces[i].getComplete() == false && playerGreen.checkCanMove(i,placeToMove)))
                    {
                        toMove = true;
                        moveCount++;
                        id = i;
                    }
                }
                if(toMove == false)
                    nextDrawTime = 30;
                else if(moveCount == 1)
                {
                    isMoving = true;
                    if(pieces[id].getComplete() == false) {
                        if (playerGreen.checkCanMove(id, placeToMove)) {
                            isMoving = true;
                            pieces[id].setTarget(placeToMove);
                            pieces[id].setMove(true);
                        }
                        else {
                            SetNextTurn();
                        }
                    }
                }
            }
            else if(t == Turn.YELLOW)
            {
                Piece[] pieces = playerYellow.getRedPiecePositions();
                int moveCount = 0;
                int id = 0;
                for (int i= 0; i < pieces.length; i++)
                {
                    if((pieces[i].getIsKilled() == false || placeToMove == 6) &&
                            (pieces[i].getComplete() == false && playerYellow.checkCanMove(i,placeToMove)))
                    {
                        toMove = true;
                        moveCount++;
                        id = i;
                    }
                }
                if(toMove == false)
                    nextDrawTime = 30;
                else if(moveCount == 1)
                {
                    isMoving = true;
                    if(pieces[id].getComplete() == false) {
                        if (playerYellow.checkCanMove(id, placeToMove)) {
                            isMoving = true;
                            pieces[id].setTarget(placeToMove);
                            pieces[id].setMove(true);
                        }
                        else
                        {
                            SetNextTurn();
                        }
                    }
                }
            }
        }

        private int getNextNo(int next) {

            bmpDice = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmpDice);
            if(t == Turn.RED)
                paint.setColor(Color.RED);
            else if(t == Turn.BLUE)
                paint.setColor(Color.BLUE);
            else if(t == Turn.GREEN)
                paint.setColor(Color.GREEN);
            else if (t == Turn.YELLOW)
                paint.setColor(Color.YELLOW);

            c.drawRoundRect(new RectF(0,0,100,100),6,6,paint);
            paint.setColor(Color.WHITE);

            switch (next)
            {
                case 1:
                    c.drawCircle(50,50,10,paint);
                    break;
                case 2:
                    c.drawCircle(20,20,10,paint);
                    c.drawCircle(80,80,10,paint);
                    break;
                case 3:
                    c.drawCircle(20,20,10,paint);
                    c.drawCircle(50,50,10,paint);
                    c.drawCircle(80,80,10,paint);
                    break;
                case 4:
                    c.drawCircle(30,30,10,paint);
                    c.drawCircle(70,70,10,paint);
                    c.drawCircle(30,70,10,paint);
                    c.drawCircle(70,30,10,paint);
                    break;
                case 5:
                    c.drawCircle(20,20,10,paint);
                    c.drawCircle(80,80,10,paint);
                    c.drawCircle(50,50,10,paint);
                    c.drawCircle(20,80,10,paint);
                    c.drawCircle(80,20,10,paint);
                    break;
                case 6:
                    c.drawCircle(20,20,10,paint);
                    c.drawCircle(20,50,10,paint);
                    c.drawCircle(20,80,10,paint);
                    c.drawCircle(80,20,10,paint);
                    c.drawCircle(80,80,10,paint);
                    c.drawCircle(80,50,10,paint);
                    break;
            }

            return next;
        }

        private void DrawPlayerPiece() {

            if(playerRed != null)
            {
                Piece[] pieces = playerRed.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    //canvas.drawBitmap(playerRed.getBmpPiece(),p.getX() - (playerRed.getPieceSize() / 2), p.getY() - (playerRed.getPieceSize()),null);
                    paint.setColor(Color.parseColor(redPieceColor));
                    canvas.drawBitmap(bmpRedPiece,p.getX()-bmpRedPiece.getWidth()/2,p.getY()-bmpRedPiece.getHeight(),null);
                    //canvas.drawCircle(p.getX(),p.getY(),(CircleSize - 8) / 2,paint);
                    //canvas.drawRect(p.getCollision(),paint);
                }

                if(t == Turn.RED)
                {
                    if(placeToMove == 0) {
                        getNextNo(6);
                    }
                    else {
                        getNextNo(placeToMove);
                    }
                }
            }

            if(playerYellow != null)
            {
                Piece[] pieces = playerYellow.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    paint.setColor(Color.parseColor(yellowPieceColor));
                    canvas.drawBitmap(bmpYellowPiece,p.getX()-bmpYellowPiece.getWidth()/2,p.getY()-bmpYellowPiece.getHeight(),null);
                    //canvas.drawRect(p.getCollision(),paint);
                }

                if(t == Turn.YELLOW)
                {
                    if(placeToMove == 0) {
                        getNextNo(6);
                    }
                    else {
                        getNextNo(placeToMove);
                    }
                }
            }

            if(playerBlue != null)
            {
                Piece[] pieces = playerBlue.getRedPiecePositions();
                for (int i = 0; i < pieces.length; i++)
                {
                    Piece p = pieces[i];
                    paint.setColor(Color.parseColor(bluePieceColor));
                    canvas.drawBitmap(bmpBluePiece,p.getX()-bmpBluePiece.getWidth()/2,p.getY()-bmpBluePiece.getHeight(),null);
                }

                if(t == Turn.BLUE)
                {
                    if(placeToMove == 0) {
                        getNextNo(6);
                    }
                    else {
                        getNextNo(placeToMove);
                    }
                }
            }

            if(playerGreen != null)
            {
                Piece[] pieces = playerGreen.getRedPiecePositions();
                for (int i = 0; i < green.length; i++)
                {
                    Piece p = pieces[i];
                    paint.setColor(Color.parseColor(greenPieceColor));
                    canvas.drawBitmap(bmpGreenPiece,p.getX()-bmpGreenPiece.getWidth()/2,p.getY()-bmpGreenPiece.getHeight(),null);
                }

                if(t == Turn.GREEN)
                {
                    if(placeToMove == 0) {
                        getNextNo(6);
                    }
                    else {
                        getNextNo(placeToMove);
                    }
                }
            }

            if(toSuffle == false && toMove == false && toHome == false && nextDrawTime > 0)
                nextDrawTime--;

            if(toSuffle == false && toMove == false && toHome == false && nextDrawTime <= 0) {
                SetNextTurn();
            }
        }

        private void DrawArrows() {

            if(t == Turn.RED)
            {
                if(placeToMove == 6) {
                    Piece[] pieces = playerRed.getRedPiecePositions();
                    for (int i = 0; i < pieces.length; i++) {
                        if (pieces[i].getIsKilled()) {
                            playerRed.getArrows(i).setY();

                            paint.setColor(Color.parseColor("#be5c46"));
//                    paint.setShader(new LinearGradient(0f,0f,
//                            0,
//                            getHeight(),
//                            Color.RED,Color.WHITE, Shader.TileMode.MIRROR));
                            canvas.drawPath( playerRed.getArrows(i).getPath(), paint);
                        }
                    }
                }
            }

            if(t == Turn.GREEN)
            {
                if(placeToMove == 6) {
                    Piece[] pieces = playerGreen.getRedPiecePositions();
                    for (int i = 0; i < pieces.length; i++) {
                        if (pieces[i].getIsKilled()) {

                            playerGreen.getArrows(i).setY();

                            paint.setColor(Color.parseColor("#2f7d24"));
//                    paint.setShader(new LinearGradient(0f,0f,
//                            0,
//                            getHeight(),
//                            Color.GREEN,Color.WHITE, Shader.TileMode.MIRROR));
                            canvas.drawPath(playerGreen.getArrows(i).getPath(), paint);
                        }
                    }
                }
            }

            if(t == Turn.BLUE)
            {
                if(placeToMove == 6) {
                    Piece[] pieces = playerBlue.getRedPiecePositions();
                    for (int i = 0; i < pieces.length; i++) {
                        if (pieces[i].getIsKilled()) {
                            playerBlue.getArrows(i).setY();
                            paint.setColor(Color.parseColor("#051850"));
//                    paint.setShader(new LinearGradient(0f,0f,
//                            0,
//                            getHeight(),
//                            Color.BLUE,Color.WHITE, Shader.TileMode.MIRROR));
                            canvas.drawPath( playerBlue.getArrows(i).getPath(), paint);
                        }
                    }
                }
            }

            if(t == Turn.YELLOW)
            {
                if(placeToMove == 6) {
                    Piece[] pieces = playerYellow.getRedPiecePositions();
                    for (int i = 0; i < pieces.length; i++) {
                        if (pieces[i].getIsKilled()) {
                            playerYellow.getArrows(i).setY();

                            paint.setColor(Color.parseColor("#9b942c"));
//                    paint.setShader(new LinearGradient(0f,0f,
//                            0,
//                            getHeight(),
//                            Color.YELLOW,Color.WHITE, Shader.TileMode.MIRROR));
                            canvas.drawPath(playerYellow.getArrows(i).getPath(), paint);
                        }
                    }
                }
            }
        }

        public void onResume() {
            isPlaying = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void onPause() {
            isPlaying = false;
            try {
                gameThread.join();
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        void control(){
            try {
                gameThread.sleep(17);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if(gameFinished == false) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        if (toSuffle) {
                            if (!toHome) {
                                if (t == Turn.RED) {
                                    if (rectRed.contains((int) event.getX(), (int) event.getY())) {
                                        isShuffling = true;
                                    }
                                } else if (t == Turn.YELLOW) {
                                    if (rectYellow.contains((int) event.getX(), (int) event.getY())) {
                                        isShuffling = true;
                                    }
                                } else if (t == Turn.GREEN) {
                                    if (rectGreen.contains((int) event.getX(), (int) event.getY())) {
                                        Log.d("LudoActivity", "Touch X : " + event.getX() + " Touch Y : " + event.getY());
                                        isShuffling = true;
                                    }
                                } else if (t == Turn.BLUE) {
                                    if (rectBlue.contains((int) event.getX(), (int) event.getY())) {
                                        isShuffling = true;
                                    }
                                }
                            }
                        } else if (toMove) {
                            if (t == Turn.RED) {
                                if (!isMoving) {
                                    Piece[] pieces = playerRed.getRedPiecePositions();

                                    for (int i = 0; i < pieces.length; i++) {
                                        {
                                            if (pieces[i].getCollision().contains((int) event.getX(), (int) event.getY())) {
                                                Log.d("LudoActivity", "On Piece");
                                                if (pieces[i].getIsKilled() == false || placeToMove == 6) {
                                                    Log.d("LudoActivity", "Click on Yellow");
                                                    if (pieces[i].getComplete() == false) {
                                                        if (playerRed.checkCanMove(i, placeToMove)) {
                                                            isMoving = true;
                                                            pieces[i].setTarget(placeToMove);
                                                            pieces[i].setMove(true);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (t == Turn.YELLOW) {
                                if (!isMoving) {
                                    Piece[] pieces = playerYellow.getRedPiecePositions();

                                    for (int i = 0; i < pieces.length; i++) {
                                        {
                                            if (pieces[i].getCollision().contains((int) event.getX(), (int) event.getY())) {
                                                Log.d("LudoActivity", "On Piece");
                                                if (pieces[i].getIsKilled() == false || placeToMove == 6) {
                                                    Log.d("LudoActivity", "Click on Yellow");
                                                    if (pieces[i].getComplete() == false) {
                                                        if (playerYellow.checkCanMove(i, placeToMove)) {
                                                            isMoving = true;
                                                            pieces[i].setTarget(placeToMove);
                                                            pieces[i].setMove(true);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (t == Turn.GREEN) {
                                if (!isMoving) {
                                    Piece[] pieces = playerGreen.getRedPiecePositions();

                                    for (int i = 0; i < pieces.length; i++) {
                                        {
                                            if (pieces[i].getCollision().contains((int) event.getX(), (int) event.getY())) {
                                                Log.d("LudoActivity", "On Piece");
                                                if (pieces[i].getIsKilled() == false || placeToMove == 6) {
                                                    Log.d("LudoActivity", "Click on Green");
                                                    if (pieces[i].getComplete() == false) {
                                                        if (playerGreen.checkCanMove(i, placeToMove)) {
                                                            isMoving = true;
                                                            pieces[i].setTarget(placeToMove);
                                                            pieces[i].setMove(true);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (t == Turn.BLUE) {
                                if (!isMoving) {
                                    Piece[] pieces = playerBlue.getRedPiecePositions();

                                    for (int i = 0; i < pieces.length; i++) {
                                        {
                                            if (pieces[i].getCollision().contains((int) event.getX(), (int) event.getY())) {
                                                Log.d("LudoActivity", "On Piece");
                                                if (pieces[i].getIsKilled() == false || placeToMove == 6) {
                                                    Log.d("LudoActivity", "Click on blue");
                                                    if (pieces[i].getComplete() == false) {
                                                        if (playerBlue.checkCanMove(i, placeToMove)) {
                                                            isMoving = true;
                                                            pieces[i].setTarget(placeToMove);
                                                            pieces[i].setMove(true);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else
                            return false;
                        break;
                }
            }
            else
            {
                startActivity(new Intent(context,MainActivity.class));
                return true;
            }
            return  true;
        }
    }
}
