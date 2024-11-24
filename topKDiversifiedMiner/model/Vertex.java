package model;

import java.util.ArrayList;

public class Vertex {
    public int label;
    public ArrayList<Edge> edge;

    public Vertex(int i) {
        edge = new ArrayList<>();
    }

    void push(int from, int to, int eLabel) {
        Edge e = new Edge();
        e.from = from;
        e.to = to;
        e.eLabel = eLabel;
        edge.add(e);
    }
}
