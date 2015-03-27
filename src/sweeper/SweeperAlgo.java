package sweeper;

import map.Map;
import map.Strategy;

import java.util.LinkedList;
import java.util.Random;

public class SweeperAlgo implements Strategy
{
    private LinkedList<Point> safeFrontier;
    private boolean[][] probed;

    private LinkedList<Point> checkFrontier;

    private boolean[][] bombArr;

    private Random random;


    public SweeperAlgo()
    {
        safeFrontier = new LinkedList<Point>();
        checkFrontier = new LinkedList<Point>();
        random = new Random();
    }

    @Override
    public void play(Map m)
    {
        probed = new boolean[m.rows()][m.columns()];
        bombArr = new boolean[m.rows()][m.columns()];

        // first pick
        Point p = new Point(m.rows() / 2, m.columns() / 2);

        int response = m.probe(p.x, p.y);
        if (Map.BOOM == response)
            return;
        try
        {
            probed[p.x][p.y] = true;
            p.value = response;

            // check if sweeper needs to randomly select a new block
            if (p.value != 0)
            {
                checkFrontier.add(p);
                checkForBetterStartingPos(m);
            }
            else
                addAllAdjacentToSafeFrontier(p, m);

            //while (true)
            //{
                probeSafeFrontier(m);

            //}
        }
        catch (BombException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void probeSafeFrontier(Map m) throws BombException
    {
        while (!safeFrontier.isEmpty())
        {
            Point p = safeFrontier.getFirst();
            safeFrontier.remove(0);
            int response = m.probe(p.x, p.y);
            if (response == Map.BOOM)
                throw new BombException("Probed a bomb in safe frontier. Check your code!");

            p.value = response;
            probed[p.x][p.y] = true;

            if (p.value == 0)
                addAllAdjacentToSafeFrontier(p, m);
            else
                checkFrontier.add(p);
        }
    }

    private void checkForBetterStartingPos(Map m) throws BombException
    {
        Point p = makeRandomProbe(m);

        int response = m.probe(p.x, p.y);

        if (response == Map.BOOM)
            throw new BombException("Probed a bomb");

        probed[p.x][p.y] = true;

        if (m.look(p.x, p.y) != 0)
        {
            p.value = response;
            checkFrontier.add(p);

            checkForBetterStartingPos(m);
        }
        else
            addAllAdjacentToSafeFrontier(p, m);
    }

    private void addAllAdjacentToSafeFrontier(Point p, Map m)
    {
        for (int i = 0; i < 8; i++)
        {
            int x = p.x;
            int y = p.y;

            switch (i)
            {
                // up
                case 0:
                    x = p.x;
                    y = p.y + 1;
                    break;

                // up and right
                case 1:
                    x = p.x + 1;
                    y = p.y + 1;
                    break;

                // right
                case 2:
                    x = p.x + 1;
                    y = p.y;
                    break;

                // down and right
                case 3:
                    x = p.x + 1;
                    y = p.y - 1;
                    break;

                // down
                case 4:
                    x = p.x;
                    y = p.y - 1;
                    break;

                // down and left
                case 5:
                    x = p.x - 1;
                    y = p.y - 1;
                    break;

                // left
                case 6:
                    x = p.x - 1;
                    y = p.y;
                    break;

                // up and left
                case 7:
                    x = p.x - 1;
                    y = p.y + 1;
                    break;
            }

            if (!checkOutOfBounds(x, y, m))
            {
                if (!hasBeenProbed(x, y))
                {
                    safeFrontier.add(new Point(x, y));
                }
            }
        }
    }

    private boolean checkOutOfBounds(int x, int y, Map m)
    {
        if (x < 0 || x >= m.rows())
            return true;
        else if (y < 0 || y >= m.columns())
            return true;

        return false;
    }

    private boolean hasBeenProbed(int x, int y)
    {
        return probed[x][y];
    }

    private Point makeRandomProbe(Map m)
    {
        int x;
        int y;
        do
        {
            x = random.nextInt(m.rows());
            y = random.nextInt(m.columns());
        } while (hasBeenProbed(x, y));

        return new Point(x, y);
    }

    private Point pokeRandomAdjacent(Map m)
    {
        int x;
        int y;
        do
        {
            // random from - 1 to 1
            x = random.nextInt(3) - 1;
            y = random.nextInt(3) - 1;
        } while (hasBeenProbed(x, y) || checkOutOfBounds(x, y, m));

        return new Point(x, y);
    }
}