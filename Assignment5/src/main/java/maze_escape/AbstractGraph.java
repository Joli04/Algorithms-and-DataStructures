package maze_escape;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractGraph<V> {

    /**
     * Graph representation:
     * this class implements graph search algorithms on a graph with abstract vertex type V
     * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
     * this abstraction can be used for both directed and undirected graphs
     **/

    public AbstractGraph() {
    }

    /**
     * retrieves all neighbours of the given fromVertex
     * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
     *
     * @param fromVertex
     * @return
     */
    public abstract Set<V> getNeighbours(V fromVertex);

    /**
     * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
     * if the graph is directed, only outgoing edges shall be traversed
     * firstVertex shall be included in the result as well
     * if the graph is connected, all vertices shall be found
     *
     * @param firstVertex the start vertex for the retrieval
     * @return
     */
    public Set<V> getAllVertices(V firstVertex) {
        Set<V> allVertices = new HashSet<>();
        //Call the recursive method
        getAllVertices(firstVertex, allVertices);
        return allVertices;
    }
    private void getAllVertices(V firstVertex, Set<V> allVertices) {
        //Add the first vertex to the set
        allVertices.add(firstVertex);
        //Check all neighbours of firstVertex and for each neighbour check if it is already in allVertices
        // and if not, add it
        for (V neighbour : getNeighbours(firstVertex)) {
            if (!allVertices.contains(neighbour)) {
                getAllVertices(neighbour, allVertices);
            }
        }
    }

    /**
     * Formats the adjacency list of the subgraph starting at the given firstVertex
     * according to the format:
     * vertex1: [neighbour11,neighbour12,…]
     * vertex2: [neighbour21,neighbour22,…]
     * …
     * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
     * if the graph is directed, only outgoing edges shall be traversed
     * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
     *
     * @param firstVertex
     * @return
     */
    public String formatAdjacencyList(V firstVertex) {
        //Create a StringBuilder
        StringBuilder stringBuilder = new StringBuilder("Graph adjacency list:\n");
        //Call the recursive method
        formatAdjacencyList(firstVertex, stringBuilder);
        // return the result
        return stringBuilder.toString();
    }

    private void formatAdjacencyList(V vertex, StringBuilder stringBuilder) {
        //Add the vertex to the string, with a colon and neighbours in brackets
        stringBuilder.append(vertex).append(": ").append(getNeighbours(vertex).toString()
                .replaceAll("\s+", "")).append("\n");

        //Check all neighbours of vertex and for each neighbour check if it is already in allVertices
        for(V neighbour : getNeighbours(vertex)){
            if (!stringBuilder.toString().contains(neighbour.toString()+ ":")) {
                formatAdjacencyList(neighbour, stringBuilder);
            }
        }
    }


    /**
     * represents a directed path of connected vertices in the graph
     */
    public class GPath {
        private Deque<V> vertices = new LinkedList<>();
        private double totalWeight = 0.0;
        private Set<V> visited = new HashSet<>();

        /**
         * representation invariants:
         * 1. vertices contains a sequence of vertices that are neighbours in the graph,
         * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
         * 2. a path with one vertex equal start and target vertex
         * 3. a path without vertices is empty, does not have a start nor a target
         * totalWeight is a helper attribute to capture total path length from a function on two neighbouring vertices
         * visited is a helper set to be able to track visited vertices in searches, only for analysis purposes
         **/
        private static final int DISPLAY_CUT = 10;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%.2f Length=%d visited=%d (",
                            this.totalWeight, this.vertices.size(), this.visited.size()));
            String separator = "";
            int count = 0;
            final int tailCut = this.vertices.size() - 1 - DISPLAY_CUT;
            for (V v : this.vertices) {
                // limit the length of the text representation for long paths.
                if (count < DISPLAY_CUT || count > tailCut) {
                    sb.append(separator + v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator + "...");
                }
                count++;
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * recalculates the total weight of the path from a given weightMapper that calculates the weight of
         * the path segment between two neighbouring vertices.
         *
         * @param weightMapper
         */
        public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
            this.totalWeight = 0.0;
            V previous = null;
            for (V v : this.vertices) {
                // the first vertex of the iterator has no predecessor and hence no weight contribution
                if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
                previous = v;
            }
        }

        public Queue<V> getVertices() {
            return this.vertices;
        }

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath depthFirstSearch(V startVertex, V targetVertex) {

        if (startVertex == null || targetVertex == null) return null;
        //Create a new path
        GPath path = new GPath();
        return depthFirstSearch(startVertex, targetVertex, path);
    }

    private GPath depthFirstSearch(V startVertex, V targetVertex, GPath path) {
        //Check if our path already contains the startVertex
        if (path.getVisited().contains(startVertex)){
            return null;
        }
        //Add the startVertex to the path
        path.getVisited().add(startVertex);

        //Check if the startVertex is the targetVertex and add the vertices to the path and return the path
        if (startVertex.equals(targetVertex)) {
            path.getVertices().add(startVertex);
            return path;
        }

        for (V neighbour: getNeighbours(startVertex)) {
            GPath currentPath = depthFirstSearch(neighbour, targetVertex, path);
            if (currentPath != null) {
                currentPath.vertices.addFirst(startVertex);
                return currentPath;
            }
        }
        //Return null if we found no path from the startVertex to the targetVertex
        return null;
    }

    /**
     * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath breadthFirstSearch(V startVertex, V targetVertex) {
        if (startVertex == null || targetVertex == null) return null;

        GPath path = new GPath();
        Map<V, V> visitedFrom = new HashMap<>();
        Queue<V> queue = new LinkedList<>();
        path.visited.add(startVertex);

        //Check if the startVertex is the targetVertex and add the vertices to the path and return the path
        if (startVertex.equals(targetVertex)) {
            path.vertices.add(targetVertex);
            return path;
        }

        queue.offer(startVertex);
        visitedFrom.put(startVertex, null);

        V current = queue.poll();
        while (current != null) {
            for (V neighbour : this.getNeighbours(current)) {
                if (neighbour.equals(targetVertex)) {
                    while (current != null) {
                        path.vertices.addFirst(current);
                        current = visitedFrom.get(current);
                    }
                    path.getVertices().add(neighbour);
                    return path;
                } else if (!path.getVisited().contains(neighbour)) {
                    visitedFrom.put(neighbour, current);
                    queue.offer(neighbour);
                    path.getVisited().add(neighbour);
                }
            }
            current = queue.poll();
        }

        return null;
    }

    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean marked = false;  // indicates DSP processing has been marked complete for this vertex
        protected double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path towards this node's vertex

        private MSTNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(MSTNode otherMSTNode) {
            return Double.compare(weightSumTo, otherMSTNode.weightSumTo);
        }
    }

    /**
     * Calculates the edge-weighted shortest path from the startVertex to targetVertex in the subgraph
     * according to Dijkstra's algorithm of a minimum spanning tree
     *
     * @param startVertex
     * @param targetVertex
     * @param weightMapper provides a function(v1,v2) by which the weight of an edge from v1 to v2
     * can be retrieved or calculated
     * @return the shortest path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */

    public GPath dijkstraShortestPath(V startVertex, V targetVertex,
                                      BiFunction<V, V, Double> weightMapper) {

        if (startVertex == null || targetVertex == null) return null;

        // initialise the result path of the search
        GPath path = new GPath();
        path.visited.add(startVertex);

        // easy target
        if (startVertex.equals(targetVertex)) {
            path.vertices.add(startVertex);
            return path;
        }

        // a minimum spanning tree which tracks for every visited vertex:
        //   a) its (parent) predecessor in the currently shortest path towards this visited vertex
        //   b) the total weight of the currently shortest path towards this visited vertex
        //   c) a mark, indicating whether the current path towards this visited vertex is the final shortest.
        // (you may choose a different approach of tracking the MST of the algorithm, if you wish)
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>();

        // initialise the minimum spanning tree with the startVertex
        MSTNode nearestMSTNode = new MSTNode(startVertex);
        nearestMSTNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, nearestMSTNode);

        while (nearestMSTNode != null) {
            nearestMSTNode.marked = true;
            for (V neighbourVertex : getNeighbours(nearestMSTNode.vertex)) {
                MSTNode neighborNode = new MSTNode(neighbourVertex);

                double distance = weightMapper.apply(nearestMSTNode.vertex, neighbourVertex);
                neighborNode.weightSumTo = nearestMSTNode.weightSumTo + distance;

                neighborNode.parentVertex = nearestMSTNode.vertex;

                if (!minimumSpanningTree.containsKey(neighbourVertex) ||
                        minimumSpanningTree.get(neighbourVertex).weightSumTo >= neighborNode.weightSumTo) {
                    minimumSpanningTree.put(neighbourVertex, neighborNode);
                }

                path.getVisited().add(neighbourVertex);
            }

            if (nearestMSTNode.vertex.equals(targetVertex)) {
                path.totalWeight = nearestMSTNode.weightSumTo;

                while (nearestMSTNode.vertex != null) {
                    path.vertices.addFirst(nearestMSTNode.vertex);
                    nearestMSTNode.vertex = minimumSpanningTree.get(nearestMSTNode.vertex).parentVertex;
                }
                return path;
            }

            nearestMSTNode = minimumSpanningTree.values()
                    .stream().filter(v -> !v.marked).min(MSTNode::compareTo).orElse(null);
        }
        return null;
    }

}
