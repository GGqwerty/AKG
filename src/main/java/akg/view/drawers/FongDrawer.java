package akg.view.drawers;

import akg.model.obj.Lamp;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static akg.model.light.FongCalculate.shadePoint;
import static akg.model.math.Geometry.edgeFunction;

public class FongDrawer {

    public void triangleRasterizationHalfSpaceFong(BufferedImage image, int width, int height, RealVector a, RealVector b, RealVector c,  RealVector eye,
                                                   int color, double zNear, double zFar, double[][] zBuffer, List<RealVector> m, List<Lamp> lamps,
                                                   double ambientCoef, double diffuseCoef, double specularCoef, double shininess) {
        if (!(
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
        )) {
            return;
        }
        var xMin = (int) Math.round(Math.min(a.getEntry(0), Math.min(b.getEntry(0), c.getEntry(0))));
        var yMin = (int) Math.round(Math.min(a.getEntry(1), Math.min(b.getEntry(1), c.getEntry(1))));
        var xMax = (int) Math.round(Math.max(a.getEntry(0), Math.max(b.getEntry(0), c.getEntry(0))));
        var yMax = (int) Math.round(Math.max(a.getEntry(1), Math.max(b.getEntry(1), c.getEntry(1))));


        xMax = Math.min(width - 1, xMax);
        yMax = Math.min(height - 1, yMax);
        xMin = Math.max(0, xMin);
        yMin = Math.max(0, yMin);
        double area = edgeFunction(a, b, c);
        for (var y = yMin; y <= yMax; y++) {
            for (var x = xMin; x <= xMax; x++) {
                RealVector pixel = new ArrayRealVector(new double[]{x, y, 0});
                double w0 = edgeFunction(b, c, pixel);
                double w1 = edgeFunction(c, a, pixel);
                double w2 = edgeFunction(a, b, pixel);

                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {

                    w0 /= area;
                    w1 /= area;
                    w2 /= area;
                    double z = a.getEntry(2) * w0 + b.getEntry(2) * w1 + c.getEntry(2) * w2;
                    if (z < zBuffer[x][y]) {
                        zBuffer[x][y] = z;
                        Color color1 = shadePoint(pixel, a, b, c, m.get(0), m.get(1), m.get(2), lamps.get(0).light, eye, new Color(color), new Color(color), lamps.get(0).color, lamps.get(0).color, ambientCoef, diffuseCoef, specularCoef, shininess);
                        image.setRGB(x, y, color1.getRGB());
                    }
                }
            }
        }
    }
}
