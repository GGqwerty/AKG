package akg.view.drawers;

import akg.model.math.TriangleFace;
import akg.model.obj.Face;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static akg.model.math.Geometry.linePairIntersectionPolygon;
import static akg.view.ImageDrawer.drawYLineThroughZBuffer;

public class ScanLineTriangleDrawer {

    public static void triangleRasterizationLineScan(BufferedImage image, TriangleFace f, List<RealVector> list, RealVector eye, double zNear, double zFar, double[][] zBuffer, int color) {
        RealVector normal = f.getNormal(list);

        RealVector a = list.get(f.vert.get(0).vId);
        RealVector b = list.get(f.vert.get(1).vId);
        RealVector c = list.get(f.vert.get(2).vId);

        if (normal.dotProduct(eye)<0 &&
                (((a.getEntry(0) > 0 && a.getEntry(0) < image.getWidth()) ||
                        (b.getEntry(0) > 0 && b.getEntry(0) < image.getWidth()) ||
                        (c.getEntry(0) > 0 && c.getEntry(0) < image.getWidth())) &&

                        ((a.getEntry(1) > 0 && a.getEntry(1) < image.getHeight()) ||
                                (b.getEntry(1) > 0 && b.getEntry(1) < image.getHeight()) ||
                                (c.getEntry(1) > 0 && c.getEntry(1) < image.getHeight())) &&

                        ((a.getEntry(2) > zNear && a.getEntry(2) < zFar) ||
                                (b.getEntry(2) > zNear && b.getEntry(2) < zFar) ||
                                (c.getEntry(2) > zNear && c.getEntry(2) < zFar)) &&

                        (a.getEntry(2) != Double.MAX_VALUE) &&
                        (b.getEntry(2) != Double.MAX_VALUE) &&
                        (c.getEntry(2) != Double.MAX_VALUE))
        ) {
            polygonRasterization(image, f, list, zNear, zFar, zBuffer, color);
        }
    }

    public static void polygonRasterization(BufferedImage image, Face f, List<RealVector> list, double zNear, double zFar, double[][] zBuffer, int color) {
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        List<Double> z = new ArrayList<>();
        for (int i = 0; i < f.vert.size(); i++) {
            x.add((int) (list.get(f.vert.get(i).vId).getEntry(0)));
            y.add((int) (list.get(f.vert.get(i).vId).getEntry(1)));
            z.add((list.get(f.vert.get(i).vId).getEntry(2)));
        }
        for (int i = Math.max(Collections.min(y), 0); i <= Math.min(Collections.max(y), image.getHeight()); i++) {
            List<Pair<Integer, Double>> intersections = linePairIntersectionPolygon(x, y, z, i);
            for (int j = 0; j < intersections.size(); j += 2) {
                drawYLineThroughZBuffer(image, i, intersections.get(j).getFirst(), intersections.get(j).getSecond(), intersections.get(j + 1).getFirst(), intersections.get(j).getSecond(), color, zNear, zFar, zBuffer);
            }
        }
    }

}
