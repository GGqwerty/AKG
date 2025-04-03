package akg.model.math;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

    public static List<Integer> lineIntersectionPolygon(List<Integer> polygonX, List<Integer> polygonY, int y){
        List<Integer> intersections = new ArrayList<>();

        for (int i = 0; i < polygonX.size(); i++) {
            int j = (i + 1) % polygonX.size();
            if ((polygonY.get(i) <= y && polygonY.get(j) > y) || (polygonY.get(j) <= y && polygonY.get(i) > y)) {
                int xCross = polygonX.get(i) + (y - polygonY.get(i)) *
                        (polygonX.get(j) - polygonX.get(i)) / (polygonY.get(j) - polygonY.get(i));
                intersections.add(xCross);
            }
        }
        return intersections;
    }

    public static List<Pair<Integer, Double>> linePairIntersectionPolygon(List<Integer> polygonX, List<Integer> polygonY, List<Double> polygonZ, int y) {
        List<Pair<Integer, Double>> intersections = new ArrayList<>();
        for (int i = 0; i < polygonX.size(); i++) {
            int j = (i + 1) % polygonX.size();
            if ((polygonY.get(i) <= y && polygonY.get(j) > y) || (polygonY.get(j) <= y && polygonY.get(i) > y)) {
                int xCross = polygonX.get(i) + (y - polygonY.get(i)) *
                        (polygonX.get(j) - polygonX.get(i)) / (polygonY.get(j) - polygonY.get(i));
                Double zCross = polygonZ.get(i) + (y - polygonY.get(i)) *
                        (polygonZ.get(j) - polygonZ.get(i)) / (polygonY.get(j) - polygonY.get(i));
                intersections.add(new Pair<>(xCross, zCross));
            }
        }
        return intersections;
    }

    public static double edgeFunction(RealVector a, RealVector b, RealVector c) {
        return (c.getEntry(0) - a.getEntry(0)) * (b.getEntry(1) - a.getEntry(1)) -
                (c.getEntry(1) - a.getEntry(1)) * (b.getEntry(0) - a.getEntry(0));
    }

}
