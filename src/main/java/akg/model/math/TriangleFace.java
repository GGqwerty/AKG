package akg.model.math;

import akg.model.obj.Face;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

import static akg.model.math.MatrixTransform.*;

public class TriangleFace extends Face {

    public TriangleFace(List<FaceVertex> list){
        id = 0;
        if(list.size()!=3)
            throw new IllegalArgumentException();
        vert = List.copyOf(list);
    }

    public RealVector getNormal(List<RealVector> vertex){
        int buf1 = vert.get(0).vId;
        int buf2 = vert.get(1).vId;
        RealVector first = new ArrayRealVector(new double[]{
                vertex.get(buf2).getEntry(0)-vertex.get(buf1).getEntry(0),
                vertex.get(buf2).getEntry(1)-vertex.get(buf1).getEntry(1),
                vertex.get(buf2).getEntry(2)-vertex.get(buf1).getEntry(2)
        });
        buf2 = vert.get(2).vId;
        RealVector second = new ArrayRealVector(new double[]{
                vertex.get(buf2).getEntry(0)-vertex.get(buf1).getEntry(0),
                vertex.get(buf2).getEntry(1)-vertex.get(buf1).getEntry(1),
                vertex.get(buf2).getEntry(2)-vertex.get(buf1).getEntry(2)
        });
        return crossProduct(first, second);
    }

    public static RealVector computeBarycentric(RealVector p, RealVector a, RealVector b, RealVector c) {
        RealVector ab = b.subtract(a);
        RealVector ac = c.subtract(a);
        RealVector ap = p.subtract(a);

        double d00 = ab.dotProduct(ab);
        double d01 = ab.dotProduct(ac);
        double d11 = ac.dotProduct(ac);
        double d20 = ap.dotProduct(ab);
        double d21 = ap.dotProduct(ac);
        double denom = d00 * d11 - d01 * d01;

        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        return new ArrayRealVector(new double[]{u, v, w});
    }
}
