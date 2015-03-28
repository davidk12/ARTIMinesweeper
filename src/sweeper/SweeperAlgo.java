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
    private LinkedList<Point> excludeList;

    private Random random;

    // temp stuff
    private boolean madeChanges;

    public SweeperAlgo()
    {
        safeFrontier = new LinkedList<Point>();
        checkFrontier = new LinkedList<Point>();
        excludeList = new LinkedList<Point>();
        random = new Random();

        madeChanges = true;
    }

    /**
     * This function is called by the minesweeper(PGMS) class and
     * and calls all the logic for sweeping the board.
     *
     * @param m Stores all the information needed to interact with the map
     */
    @Override
    public void play(Map m)
    {
        gameInfo(m);

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
                System.out.println("Doing random selection \n");

                System.out.println("First node: " + p.x + "," + p.y);
                int totalMapNodes = m.rows() * m.columns();
                checkForBetterStartingPos(totalMapNodes, m.mines_minus_marks(), p,m);

                System.out.println("Random selection Done \n");
            }
            else
                addAllAdjacentToSafeFrontier(p, m);


            while (madeChanges)
            {
                madeChanges = false;
                probeSafeFrontier(m);
                findSafeBombs(m);

            }
        }
        catch (BombException e)
        {
            System.out.println(e.getMessage());
        }
    }
    private void gameInfo(Map m)
    {
        System.out.println("Game info");
        System.out.println("Size: " + m.rows() + " * " +  m.columns());
        System.out.println("Number of bombs: " + m.mines_minus_marks());
        System.out.println();
    }

    /**
     * Probes all nodes that are stored in the safeFrontier and adds the
     * probed node to an appropriate frontier.
     * If a node has value of 0 then all adjacent nodes are added to the safeFrontier
     * If a node has a value greater then 0 the it is added to checkFrontier
     *
     * @param m Stores all the information needed to interact with the map
     * @throws BombException if a bomb is probed.
     */
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

    /**
     * If the first node probed does not contain a value of 0, then
     * we probe a new node until we find one with a value of 0.
     * If a node has value of 0 then all adjacent nodes are added to the safeFrontier.
     * If a node has a value greater then 0 the it is added to checkFrontier.
     *
     * @param m Stores all the information needed to interact with the map.
     * @throws BombException if a bomb is probed.
     */
    private void checkForBetterStartingPos(int uncheckedNodes, int unknownBombsInMap ,Point p, Map m) throws BombException
    {
        int response = m.probe(p.x, p.y);

        if (response == Map.BOOM)
            throw new BombException("Probed a bomb");

        p.value = response;
        probed[p.x][p.y] = true;
        if (response == 0)
        {
            addAllAdjacentToSafeFrontier(p, m);
        }
        else
        {
            unknownBombsInMap -= p.value;
            uncheckedNodes -= 9;

            double chanceOfAdjacentBomb = (double)response / 8.0;
            double chanceRandomProbeBomb = (double)unknownBombsInMap / (double)uncheckedNodes;

            System.out.println("Chance of adjacent bomb: " + (double)response + " / 8 = " + chanceOfAdjacentBomb);
            System.out.println("Chance of random bomb: " + (double)unknownBombsInMap + " / " + (double)uncheckedNodes + " = " + chanceRandomProbeBomb);
            System.out.println();

            if (chanceOfAdjacentBomb >= chanceRandomProbeBomb)
            {
                p.value = response;
                checkFrontier.add(p);

                addNodesToExcludeList(p);
                p = makeRandomProbe(m);
                System.out.println("Random node: " + p.x + "," + p.y);
                checkForBetterStartingPos(uncheckedNodes, unknownBombsInMap, p, m);
            }
            else
            {
                p = probeRandomAdjacent(p.x, p.y, m);
                System.out.println("Adjacent node: " + p.x + "," + p.y);
                checkForBetterStartingPos(uncheckedNodes, unknownBombsInMap, p, m);
            }
        }
    }

    private void addNodesToExcludeList(Point p)
    {
        for (int i = -1; i < 2; i++)
        {
            if (i == 0)
                continue;

            excludeList.add(new Point(p.x + i, p.y + i));
            excludeList.add(new Point(p.x + i, p.y));
            excludeList.add(new Point(p.x, p.y + i));
        }

        excludeList.add(new Point(p.x - 1, p.y + 1));
        excludeList.add(new Point(p.x + 1, p.y - 1));
    }

    private void findSafeBombs(Map m)
    {
        LinkedList<Point> possibleBomb = new LinkedList<Point>();



        for (int y = 0; y < checkFrontier.size(); y++)
        {
            if (findSafeNodesAroundBombs(m, checkFrontier.get(y)))
                continue;

            int numberOfAdjacentBombs = checkFrontier.get(y).value;

            for (int i = -1; i < 2; i++)
            {
                if (i == 0)
                    continue;

                if (!checkOutOfBounds(checkFrontier.get(y).x + i, checkFrontier.get(y).y + i, m)
                        && !hasBeenProbed(checkFrontier.get(y).x + i, checkFrontier.get(y).y + i))
                    possibleBomb.add(new Point(checkFrontier.get(y).x + i, checkFrontier.get(y).y + i));

                if (!checkOutOfBounds(checkFrontier.get(y).x + i, checkFrontier.get(y).y, m)
                        && !hasBeenProbed(checkFrontier.get(y).x + i, checkFrontier.get(y).y))
                    possibleBomb.add(new Point(checkFrontier.get(y).x + i, checkFrontier.get(y).y));

                if (!checkOutOfBounds(checkFrontier.get(y).x, checkFrontier.get(y).y + i, m)
                        && !hasBeenProbed(checkFrontier.get(y).x, checkFrontier.get(y).y + i))
                    possibleBomb.add(new Point(checkFrontier.get(y).x, checkFrontier.get(y).y + i));
            }

            if (!checkOutOfBounds(checkFrontier.get(y).x - 1, checkFrontier.get(y).y + 1, m)
                    && !hasBeenProbed(checkFrontier.get(y).x - 1, checkFrontier.get(y).y + 1))
                possibleBomb.add(new Point(checkFrontier.get(y).x - 1, checkFrontier.get(y).y + 1));

            if (!checkOutOfBounds(checkFrontier.get(y).x + 1, checkFrontier.get(y).y - 1, m)
                    &&!hasBeenProbed(checkFrontier.get(y).x + 1, checkFrontier.get(y).y - 1))
                possibleBomb.add(new Point(checkFrontier.get(y).x + 1, checkFrontier.get(y).y - 1));

            if (numberOfAdjacentBombs == possibleBomb.size())
            {
                System.out.println(checkFrontier.get(y).toString() + " marked bomb/s");
                for (Point markBomb: possibleBomb)
                {
                    probed[markBomb.x][markBomb.y] = true;
                    m.mark(markBomb.x, markBomb.y);
                    bombArr[markBomb.x][markBomb.y] = true;

                    System.out.print(markBomb.toString());

                    removeFromCheckFrontier(markBomb);
                }

                // Temp stuff
                madeChanges = true;
            }

            possibleBomb.clear();
        }
    }

    // TODO: Add adjacent bombs to bombcount
    private boolean findSafeNodesAroundBombs(Map m, Point p)
    {
        int bombCount = 0;

        for (int i = -1; i < 2; i++)
        {
            if (i == 0)
                continue;

            if (!checkOutOfBounds(p.x + i, p.y + i, m) && bombArr[p.x + i][p.y + i])
                bombCount++;
            if (!checkOutOfBounds(p.x + i, p.y, m) && bombArr[p.x + i][p.y])
                bombCount++;
            if (!checkOutOfBounds(p.x, p.y + i, m) && bombArr[p.x][p.y + i])
                bombCount++;
        }
        if (!checkOutOfBounds(p.x - 1, p.y + 1, m) && bombArr[p.x - 1][p.y + 1])
            bombCount++;
        if (!checkOutOfBounds(p.x + 1, p.y - 1, m) && bombArr[p.x + 1][p.y - 1])
            bombCount++;

        if (p.value == bombCount)
        {
            addAllAdjacentToSafeFrontier(p, m);
            removeFromCheckFrontier(p);

            // Temp stuff
            madeChanges = true;

            return true;
        }

        return false;
    }

    /**
     * Handles adding all nodes adjacent to a given point to the safeFrontier.
     *
     * @param p Stores all the information needed for a single point in the map
     * @param m Stores all the information needed to interact with the map
     */
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

    /**
     * Checks if a certain coordinate is out of bounds.
     *
     * @param x coordinate in the map.
     * @param y coordinate in the map.
     * @param m Stores all the information needed to interact with the map.
     * @return true if the coordinate is out of bounds, else false.
     */
    private boolean checkOutOfBounds(int x, int y, Map m)
    {
        if (x < 0 || x >= m.rows())
            return true;
        else if (y < 0 || y >= m.columns())
            return true;

        return false;
    }

    /**
     * Checks if a certain node in the map has been probed or not.
     *
     * @param x coordinate in the map
     * @param y coordinate in the map
     * @return true if the node has been probed, else false.
     */
    private boolean hasBeenProbed(int x, int y)
    {
        return probed[x][y];
    }

    /**
     * Finds a random node in the map that has not been probed.
     *
     * @param m Stores all the information needed to interact with the map.
     * @return a point that has not been probed
     */
    private Point makeRandomProbe(Map m)
    {
        int x;
        int y;
        boolean illegalCoords;

        do
        {
            illegalCoords = false;

            x = random.nextInt(m.rows());
            y = random.nextInt(m.columns());

            for (Point excludePoint: excludeList)
            {
                if (x == excludePoint.x && y == excludePoint.y)
                    illegalCoords = true;
            }

        } while (hasBeenProbed(x, y) || illegalCoords);

        return new Point(x, y);
    }

    /**
     * Finds a random node adjacent to a given point that has not been probed.
     *
     * @param x coordinate in the map.
     * @param y coordinate in the map.
     * @param m Stores all the information needed to interact with the map.
     * @return a random point adjacent to a given point that has not been probed.
     */
    private Point probeRandomAdjacent(int x, int y, Map m)
    {
        do
        {
            // random from - 1 to 1
            x += random.nextInt(3) - 1;
            y += random.nextInt(3) - 1;
        } while (checkOutOfBounds(x, y, m) || hasBeenProbed(x, y));

        return new Point(x, y);
    }

    private boolean removeFromCheckFrontier(Point p)
    {
        for (int i = 0; i < checkFrontier.size(); i++)
        {
            if (checkFrontier.get(i).equals(p))
            {
                checkFrontier.remove(i);
                return true;
            }
        }

        return false;
    }
}


