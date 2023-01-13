package maze_escape;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.BiFunction;

public class MazeTest {

    private static final long SEED = 20221203L;
    private static int WIDTH = 100;
    private static int HEIGHT = WIDTH;
    private static final int REMOVE = 250;

    private Maze maze;
    private Set<Integer> vertices;

    @BeforeEach
    void setup() {
        Maze.reSeedRandomizer(SEED);
        maze = new Maze(WIDTH, HEIGHT);
        vertices = maze.getAllVertices(maze.getStartNode());
        maze.generateRandomizedPrim();
        maze.configureInnerEntry();
        maze.removeRandomWalls(REMOVE);
        System.out.printf("\nCreated %dx%d Randomized-Prim-Maze(%d) with %d walls removed\n", WIDTH, HEIGHT, SEED, REMOVE);

    }

    @Test
    @DisplayName("Output Dijkstra")
    public void testDijkstra() {
        Assertions.assertEquals("""
                        Results from 'Dijkstra Shortest Path' in 100x100 maze from vertex '3333' to '2000':
                        Dijkstra Shortest Path: Weight=113.00 Length=40 visited=2184 (3333, 3332, 3231, 3131, 3031, 3032, 2934, 2734, 2736, 2634, ..., 2111, 2210, 2209, 2308, 2307, 2306, 2205, 2102, 2101, 2000)
                        Dijkstra Shortest Path return: Weight=113.00 Length=40 visited=633 (2000, 2101, 2102, 2205, 2306, 2307, 2308, 2209, 2210, 2111, ..., 2634, 2736, 2734, 2934, 3032, 3031, 3131, 3231, 3332, 3333)""",
                doPathSearches(maze, "Dijkstra Shortest Path",
                        (v1,v2)-> maze.dijkstraShortestPath(v1,v2,maze::manhattanTime), vertices));
    }

    @Test
    @DisplayName("Output DepthFirstSearch")
    public void testDepthFirstSearch() {
        Assertions.assertEquals("""
                        Results from 'Depth First Search' in 100x100 maze from vertex '3333' to '2000':
                        Depth First Search: Weight=140.00 Length=51 visited=5357 (3333, 3236, 3136, 2936, 2836, 2837, 2838, 2738, 2737, 2736, ..., 2305, 2405, 2403, 2402, 2500, 2300, 2302, 2102, 2101, 2000)
                        Depth First Search return: Weight=727.00 Length=264 visited=4954 (2000, 2101, 2102, 2205, 2306, 2307, 2308, 2209, 2210, 2111, ..., 4036, 3936, 3837, 3935, 3934, 3634, 3536, 3436, 3434, 3333)""",
                doPathSearches(maze, "Depth First Search", maze::depthFirstSearch, vertices));
    }

    @Test
    @DisplayName("Output BreadthFirstSearch")
    public void testBreadthFirstSearch() {
        Assertions.assertEquals("""
                        Results from 'Breadth First Search' in 100x100 maze from vertex '3333' to '2000':
                        Breadth First Search: Weight=119.00 Length=38 visited=1733 (3333, 3236, 3136, 3032, 2934, 2734, 2736, 2634, 2632, 2630, ..., 2411, 2509, 2508, 2308, 2307, 2306, 2205, 2102, 2101, 2000)
                        Breadth First Search return: Weight=115.00 Length=38 visited=450 (2000, 2101, 2102, 2205, 2306, 2307, 2308, 2209, 2210, 2111, ..., 2630, 2632, 2634, 2736, 2734, 2934, 3032, 3136, 3236, 3333)""",
                doPathSearches(maze, "Breadth First Search", maze::breadthFirstSearch, vertices));
    }


    private static String doPathSearches(Maze maze, String title, BiFunction<Integer, Integer, Maze.GPath> searcher, Set<Integer> vertices) {

        StringBuilder builder = new StringBuilder();

        builder.append(String.format("Results from '%s' in %dx%d maze from vertex '%d' to '%d':\n",
                title, maze.getWidth(), maze.getHeight(), maze.getStartNode(), maze.getExitNode()));

        Maze.GPath path = searcher.apply(maze.getStartNode(), maze.getExitNode());
        if (path.getTotalWeight() == 0.0) path.reCalculateTotalWeight(maze::manhattanTime);

        builder.append(title)
                .append(": ")
                .append(path)
                .append("\n");

        path = searcher.apply(maze.getExitNode(), maze.getStartNode());
        if (path.getTotalWeight() == 0.0) path.reCalculateTotalWeight(maze::manhattanTime);

        builder.append(title)
                .append(" return: ")
                .append(path);

        return builder.toString();
    }
}
