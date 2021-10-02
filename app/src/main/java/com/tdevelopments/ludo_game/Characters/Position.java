package com.tdevelopments.ludo_game.Characters;

/**
 * Created by Win on 24-04-2018.
 */

public class Position {

        private int X,Y;

        public Position(int x,int y)
        {
            this.X = x;
            this.Y = y;
        }

        public  int getX()
        {
            return X;
        }

        public int getY()
        {
            return Y;
        }

    @Override
    public String toString() {
        return String.format("X is %d, Y is %d",X,Y);
    }
}
