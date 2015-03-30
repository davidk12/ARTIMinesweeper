package sweeper;

import java.util.LinkedList;

public class Point
{
    public int x;
    public int y;
    public int value;


    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Point(int x, int y, int value)
    {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public boolean equals(Point p)
    {
        if (this.x == p.x && this.y == p.y)
            return true;

        return false;
    }

    public boolean equals(int xCoord, int yCoord)
    {
        if (this.x == xCoord && this.y == yCoord)
            return true;

        return false;
    }

    public static Point findPointInLinkedList(LinkedList<Point> list, int xCoord, int yCoord)
    {
        for (Point point: list)
        {
            if (point.equals(xCoord, yCoord))
                return point;
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
