package com.tdevelopments.ludo_game.Characters;

import android.graphics.Path;
import android.util.Log;

/**
 * Created by Win on 24-04-2018.
 */

public class Arrows {

    private int X,Y,SIZE;

    private int offset = 0;

    private boolean reverse = false;

    private int ArrowSize;

    public Arrows(int x, int y, int size)
    {
        this.Y = y - size;
        this.SIZE = size;
        ArrowSize = size / 2;
        Log.d("Ludo Activity","X is " + x + ", size is " + size);

        this.X = x;//(x + size) / 2 - (ArrowSize / 2);
    }

    public void setY()
    {
        if(reverse)
        {
            Y -= 1;
            offset--;
            if(offset == 0)
                reverse = false;
        }
        else
        {
            Y += 1;
            offset++;
            if(offset == 5)
                reverse = true;
        }
    }

    public int getX()
    {
        return X;
    }

    public int getY()
    {
        return Y;
    }

    public Path getPath()
    {
        Path p = new Path();
        int x =  X - ArrowSize / 2;
        int diff = (int)(SIZE * (70f/100f));
        p.moveTo(x,Y);
        p.lineTo(x + ArrowSize, Y);
        p.lineTo(x + ArrowSize,Y+diff);
        p.lineTo(x + ArrowSize + 5,Y + diff);
        p.lineTo(x + (ArrowSize / 2), Y + SIZE);
        p.lineTo(x - 5, Y + diff);
        p.lineTo(x,Y + diff);
        p.lineTo(x,Y);

        return p;
    }
}

