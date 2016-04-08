package com.leuradu.android.bikeapp.utils;

import android.util.Log;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by radu on 08.04.2016.
 */
public class MapGraph {

    Graph graph;

    public MapGraph(String path){
        try {
            graph = new DefaultGraph("A Test");
            FileReader fr = new FileReader(new File(path));

            FileSourceDGS source = new FileSourceDGS();
            source.addSink(graph);
            source.readAll(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void computeRoute() {

        AStar astar = new AStar(graph);
        //astar.setCosts(new DistanceCosts());
//		langa gara example
//		astar.compute("353358120", "354668580");
//		zona garii example
//		astar.compute("2009760896","543644699");
//		langa gara large
        astar.compute("358388388", "2817351297");

        Path p = astar.getShortestPath();
        for (Element e : p.getNodePath()) {
            Log.d("path:", e.getAttribute("lon").toString());
        }
        Log.d("computeRoute:", astar.getShortestPath().toString());
    }
}