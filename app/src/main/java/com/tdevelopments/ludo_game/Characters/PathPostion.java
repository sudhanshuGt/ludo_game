package com.tdevelopments.ludo_game.Characters;

/**
 * Created by Win on 23-04-2018.
 */

public class PathPostion {

    private int X,Y;
    private boolean isStar;

    public PathPostion(int x, int y, boolean isStar)
    {
        this.X = x;
        this.Y = y;
        this.isStar = isStar;
    }

    public int getX()
    {
        return X;
    }

    public int getY() { return Y; }

    public boolean getIsStar()
    {
        return isStar;
    }

    @Override
    public String toString() {
        return String.format("X : %d, Y : %d: ",X,Y);
    }
}
