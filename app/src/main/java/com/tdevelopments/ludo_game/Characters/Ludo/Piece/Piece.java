package com.tdevelopments.ludo_game.Characters.Ludo.Piece;

import android.graphics.Rect;
import android.util.Log;
import com.tdevelopments.ludo_game.Characters.PathPostion;

/**
 * Created by Win on 23-04-2018.
 */

public class Piece {

    private int X,Y, homeX,homeY, Start,End;
    private boolean isComplete, isKilled, isMove = false, isHome = false, isRound = false, isIn = false;

    private PathPostion[] path;
    Rect collision;
    private int pieceSize, speed = 3;

    private int currentIndex,target,factorX,factorY, targetX,targetY, currentX,currentY;

    public Piece(int x, int y, int pieceSize, PathPostion[] pathPostions) {
        this.X = x;
        this.Y = y;
        this.homeY = y;
        this.homeX = x;

        isComplete = false;
        isKilled = true;
        target = -6;
        this.pieceSize = pieceSize;
        this.path = pathPostions;
        collision = new Rect(( x- (pieceSize / 2) + 1), (y - (pieceSize / 2) + 1),
                (x- (pieceSize / 2) + pieceSize - 1),(y - (pieceSize / 2) + pieceSize) - 1);
    }

    public int getX() {
        return X;
    }

    public int getY()
    {
        return Y;
    }

    public boolean UpdatePosition() {

        int fx = 0;
        int fy = 0;

        int x = X,y= Y;

        int diffX,diffY;

        if(x > currentX)
            diffX = x - currentX;
        else
            diffX = currentX - x;

        if(y> currentY)
            diffY = y - currentY;
        else
            diffY = currentY - y;

        if(diffX > diffY)
            if(diffY != 0) {
                fx = diffX / diffY;
            }
            else {
                if(diffX != 0)
                    fy = diffY / diffX;
            }

        if(x > currentX)
            factorX = -speed - fx;
        else if(x < currentX)
            factorX = speed + fx;
        else
            factorX = 0;

        if(currentY < y)
            factorY = -speed - fy;
        else if(currentY > y)
            factorY = speed + fy;
        else
            factorY = 0;

         x = x + factorX;
         y = y + factorY;

        if(factorX < 0) {
            if(x < currentX)
                x = currentX;
        }
        else {
            if(x > currentX)
                x = currentX;
        }

        if(factorY < 0 ) {
            if(y < currentY)
                y = currentY;
        }
        else{
            if(y > currentY)
                y = currentY;
        }

        setXY(x,y);

        if(x == targetX && y == targetY) {
            if(currentIndex == path.length - 1)
                isComplete = true;

            return true;
        }
        else {
            if(x == currentX && y==currentY) {
                currentIndex++;
                PathPostion t = path[currentIndex];
                currentX = t.getX() + (pieceSize / 2);
                currentY = t.getY() + (pieceSize / 2);
            }
            return false;
        }
    }

    public void setXY(int x, int y) {
        this.X = x;
        this.Y = y;
        collision = new Rect(( x- (pieceSize / 2) + 1),(y - (pieceSize / 2) + 1),
                (x- (pieceSize / 2) + pieceSize - 1),(y - (pieceSize / 2) + pieceSize) - 1);
    }

    public Rect getCollision()
    {
        return collision;
    }

    public boolean getIsKilled()
    {
        return isKilled;
    }

    public void setIsKilled(boolean value)
    {
        isKilled = value;
    }

    public boolean getMove()
    {
        return isMove;
    }

    public void setMove(boolean value)
    {
        isMove = value;
    }

    public void setTarget(int next) {
        target += next;

        if(isKilled) {
            currentIndex = target;
            isKilled = false;
        }
        else
            currentIndex += 1;

        Log.d("LudoActivity","Target Index : " + target);
        Log.d("LudoActivity","Current Index : " + currentIndex);

        PathPostion t = path[currentIndex];
        currentX = t.getX() + (pieceSize / 2);
        currentY = t.getY() + (pieceSize / 2);

        targetX = path[target].getX() + (pieceSize / 2);
        targetY = path[target].getY() + (pieceSize / 2);
    }

    public int canMove(int next) {
        if(target + next <= 51)
            return 1;
        else if(target + next > 51 && target + next <= 56)
            return 2;
        else
            return  0;

    }

    public int getTarget()
    {
        return target;
    }

    public void setHome(boolean value)
    {
        isHome = value;
    }

    public boolean getHome()
    {
        return isHome;
    }

    public boolean UpdateToHome() {
        int fx = 0;
        int fy = 0;

        int x = X,y = Y;

        int diffX,diffY;

        if(x > homeX)
            diffX = x - homeX;
        else
            diffX = homeX - x;

        if(y> homeY)
            diffY = y - homeY;
        else
            diffY = homeY - y;

        if(diffX > diffY)
            if(diffY != 0) {
                fx = diffX / diffY;
            }
            else {
                if(diffX != 0)
                    fy = diffY / diffX;
            }

        if(x > homeX)
            factorX = -speed - fx;
        else if(x < homeX)
            factorX = speed + fx;
        else
            factorX = 0;

        if(homeY < y)
            factorY = -speed - fy;
        else if(homeY > y)
            factorY = speed + fy;
        else
            factorY = 0;

        x = x + factorX;
        y = y + factorY;

        if(factorX < 0) {
            if(x < homeX)
                x = homeX;
        }
        else {
            if(x > homeX)
                x = homeX;
        }

        if(factorY < 0 ) {
            if(y < homeY)
                y = homeY;
        }
        else{
            if(y > homeY)
                y = homeY;
        }

        setXY(x,y);

        if(x == homeX && y == homeY) {
            Log.d("LudoActivity","HomeX : " + homeX + " x : " + x + " HomeY : " + homeY + " y : " + y);
            target = -6;
            isKilled = true;
            isHome = false;
            return true;
        }
        else
            return false;
    }

    public boolean getComplete()
    {
        return isComplete;
    }

    public boolean getIsStar(int index)
    {
        return path[index].getIsStar();
    }

    @Override
    public String toString() {
        return String.format("X is %d, Y is %d",X,Y);
    }
}
