package sweeper;

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

    public boolean equals(Point p)
    {
        if (this.x == p.x && this.y == p.y)
            return true;

        return false;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
