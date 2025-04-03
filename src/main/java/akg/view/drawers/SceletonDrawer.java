package akg.view.drawers;

import akg.model.obj.Face;
import org.apache.commons.math3.linear.RealVector;

import java.awt.image.BufferedImage;
import java.util.List;

import static akg.view.ImageDrawer.drawLine;

public class SceletonDrawer {

    public static void drawSkeleton(BufferedImage image, Face f, List<RealVector> list, int color, double zNear, double zFar) {
        List<Face.FaceVertex> pointList = f.vert;
        for (int j = 0; j < pointList.size(); j++) {
            int x1 = (int) list.get(pointList.get(j).vId).getEntry(0);
            int x2 = (int) list.get(pointList.get((j + 1) % pointList.size()).vId).getEntry(0);
            int y1 = (int) list.get(pointList.get(j).vId).getEntry(1);
            int y2 = (int) list.get(pointList.get((j + 1) % pointList.size()).vId).getEntry(1);
            double z1 = list.get(pointList.get(j).vId).getEntry(2);
            double z2 = list.get(pointList.get((j + 1) % pointList.size()).vId).getEntry(2);
            if ((x1 > 0 || x2 > 0) && (y1 > 0 || y2 > 0) &&
                    (x1 < image.getWidth() || x2 < image.getWidth()) && (y1 < image.getHeight() || y2 < image.getHeight()) &&
                    (z1 < zFar && z2 < zFar) &&
                    (z1 > zNear && z2 > zNear)) {
                drawLine(image, x1, y1, x2, y2, color);
            }
        }
    }
}
