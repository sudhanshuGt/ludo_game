package com.tdevelopments.ludo_game.Characters.Ludo.Player;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import com.tdevelopments.ludo_game.Characters.Arrows;
import com.tdevelopments.ludo_game.Characters.Ludo.Piece.Piece;
import com.tdevelopments.ludo_game.Characters.PathPostion;
import com.tdevelopments.ludo_game.Characters.Position;
import com.tdevelopments.ludo_game.LudoActivity;

/**
 * Created by Win on 24-04-2018.
 */

public class Player {

    private int StartIndex,EndIndex;

    private Piece[] pieces;

    private boolean isPass;

    private Arrows[] arrows;

    private boolean isWin = false;

    private String PlayerColor = "";

    private Bitmap bmpPiece;

    private int pieceSize;

    public Player(int si, int ei, Position[] piecePos, int pieceSize, Position[] path, String col) {
        Log.d("LudoActivity","pieceSize : " + pieceSize);
        this.StartIndex = si;
        this.EndIndex = ei;

        this.pieceSize = pieceSize;
        PlayerColor = col;
        PathPostion[] romPath = new PathPostion[57];
        if(path == null)
            Log.d("LudoActivity","path is empty");
        else {
            Log.d("LudoActivity", "path is not empty");
            Log.d("LudoActivity","Path count " + path.length);
        }

        for (int i = 0; i < 51; i++)
        {
            Log.d("LudoActivity", " si : " + si + ", i : "+ i + " Total : " + (si + i));
            if(si + i > 51) {
                Log.d("LudoActivity", " si : " + si + ", i : "+ i + " Total - 51 : " + (si + i - 52));
                romPath[i] = LudoActivity.romPath[si + i - 52];
            }
            else {
                romPath[i] = LudoActivity.romPath[si + i];
            }
        }

        int j = 0;
        for (int i = 51; i < 57; i++) {
            romPath[i] = new PathPostion(path[j].getX(),path[j].getY(),false);
            j++;
        }

        pieces = new Piece[4];

        for (int i = 0; i < piecePos.length; i++)
        {
            pieces[i] = new Piece(piecePos[i].getX(),piecePos[i].getY(), pieceSize, romPath);
        }

        arrows = new Arrows[4];

        isPass = false;

        bmpPiece = Bitmap.createBitmap(pieceSize,pieceSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpPiece);
        Paint p = new Paint();

        p.setColor(Color.WHITE);
        c.drawCircle(0,0,pieceSize - 4,p);
        p.setColor(Color.WHITE);
        Path traingle = new Path();
        traingle.moveTo(4,pieceSize / 2);
        traingle.lineTo(pieceSize / 2,pieceSize - 8);
        traingle.lineTo(pieceSize - 4,pieceSize / 2);
        traingle.lineTo(4,pieceSize / 2);

        c.drawPath(traingle,p);
        p.setColor(Color.parseColor(col));

        c.drawCircle(0,0,pieceSize - 8,p);
    }

    public void setArrows(int[] x, int[] y, int size)
    {
        for (int i = 0; i< 4; i++)
        {
            arrows[i] = new Arrows(x[i],y[i],size);
        }
    }

    public Bitmap getBmpPiece()
    {
        return bmpPiece;
    }

    public int getPieceSize()
    {
        return pieceSize;
    }
    public Arrows getArrows(int index)
    {
        return arrows[index];
    }

    public int getSI()
    {
        return StartIndex;
    }
    public int getEI()
    {
        return EndIndex;
    }

    public boolean checkCanMove(int id,int next) {
        int r = pieces[id].canMove(next);
        if(r == 1)
            return true;
        else if(r == 2)
        {
            if(isPass)
                return  true;
            else
                return false;
        }
        else
            return false;
    }
    public void setIsPass()
    {
        isPass = true;
    }
    public boolean getIsPass()
    {
        return isPass;
    }

    public boolean getWin(){return isWin;}
    public void setWin(boolean value){isWin = value;}

    public Piece[] getRedPiecePositions()
    {
        return pieces;
    }
}
