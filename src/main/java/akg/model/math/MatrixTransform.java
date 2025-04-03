package akg.model.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MatrixTransform {
    public static RealVector calculateZAxis(RealVector eye, RealVector target){
        return eye.subtract(target).unitVector();
    }

    public static RealVector calculateXAxis(RealVector up, RealVector ZAxis){
        return crossProduct(up,ZAxis).unitVector();
    }

    public static RealVector calculateYAxis(RealVector up){
        return up.copy();
    }

    public static RealMatrix calculateTranslationTransform(RealVector translation){
        return new Array2DRowRealMatrix(new double[][]{
                {1, 0, 0, translation.getEntry(0)},
                {0, 1, 0, translation.getEntry(1)},
                {0, 0, 1, translation.getEntry(2)},
                {0, 0, 0, 1}
        });
    }

    public static RealMatrix calculateScaleTransform(RealVector scale){
        return new Array2DRowRealMatrix(new double[][]{
                {scale.getEntry(0), 0, 0, 0},
                {0, scale.getEntry(1), 0, 0},
                {0, 0, scale.getEntry(2), 0},
                {0, 0, 0, 1}
        });
    }

    public static RealMatrix calculateAngleTransform(RealVector angle){
        return new Array2DRowRealMatrix(new double[][]{
                {cos(Math.PI/2*angle.getEntry(2)), -sin(Math.PI/2*angle.getEntry(2)), 0, 0},
                {sin(Math.PI/2*angle.getEntry(2)), cos(Math.PI/2*angle.getEntry(2)), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        }).multiply(new Array2DRowRealMatrix(new double[][]{
                {cos(Math.PI/2*angle.getEntry(1)), 0, sin(Math.PI/2*angle.getEntry(1)), 0},
                {0, 1, 0, 0},
                {-sin(Math.PI/2*angle.getEntry(1)), 0, cos(Math.PI/2*angle.getEntry(1)), 0},
                {0, 0, 0, 1}
        })).multiply(new Array2DRowRealMatrix(new double[][]{
                {1, 0, 0, 0},
                {0, cos(Math.PI/2*angle.getEntry(0)), -sin(Math.PI/2*angle.getEntry(0)), 0},
                {0, sin(Math.PI/2*angle.getEntry(0)), cos(Math.PI/2*angle.getEntry(0)), 0},
                {0, 0, 0, 1}
        }));
    }

    public static RealMatrix calculateViewTransform(RealVector xAxis, RealVector yAxis, RealVector zAxis, RealVector eye){
        return new Array2DRowRealMatrix(new double[][]{
                {xAxis.getEntry(0), xAxis.getEntry(1), xAxis.getEntry(2), -(xAxis.dotProduct(eye))},
                {yAxis.getEntry(0), yAxis.getEntry(1), yAxis.getEntry(2), -(yAxis.dotProduct(eye))},
                {zAxis.getEntry(0), zAxis.getEntry(1), zAxis.getEntry(2), -(zAxis.dotProduct(eye))},
                {0, 0, 0, 1}});
    }

    public static RealMatrix calculateProjectionOrtoTransform(int w, int h, double zNear, double zFar){
        return new Array2DRowRealMatrix(new double[][]{
                {2.0/w, 0, 0, 0},
                {0, 2.0/h, 0, 0},
                {0, 0, 1.0/(zNear-zFar), zNear/(zNear-zFar)},
                {0, 0, 0, 1}});
    }

    public static RealMatrix calculateProjectionPerspecTransform(int w, int h, double zNear, double zFar){
        double aspect = (double) w / h;
        double f = 1.0 / Math.tan(Math.toRadians(90) / 2.0);

        return new Array2DRowRealMatrix(new double[][]{
                {f / aspect, 0, 0, 0},
                {0, f, 0, 0},
                {0, 0, (zFar + zNear) / (zNear - zFar), (2 * zFar * zNear) / (zNear - zFar)},
                {0, 0, -1, 0}});
    }

    public static RealMatrix calculateViewportTransform(int w, int h, double xMin, double yMin){
        return new Array2DRowRealMatrix(new double[][]{
                {w/2.0, 0, 0, xMin+w/2.0},
                {0, -h/2.0, 0, yMin+h/2.0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});
    }

    public static RealVector crossProduct(RealVector u, RealVector v) {
        if (u.getDimension() != 3 || v.getDimension() != 3) {
            throw new IllegalArgumentException("Векторное произведение определено только для 3D-векторов.");
        }

        double x = u.getEntry(1) * v.getEntry(2) - u.getEntry(2) * v.getEntry(1);
        double y = u.getEntry(2) * v.getEntry(0) - u.getEntry(0) * v.getEntry(2);
        double z = u.getEntry(0) * v.getEntry(1) - u.getEntry(1) * v.getEntry(0);

        return new ArrayRealVector(new double[]{x, y, z});
    }

    public static RealVector calculateUpVector(RealVector eye, RealVector target) {
        RealVector zAxis = eye.subtract(target).unitVector();

        RealVector up = new ArrayRealVector(new double[]{0.0, 1.0, 0.0});

        RealVector xAxis = crossProduct(up, zAxis).unitVector();
        RealVector yAxis = crossProduct(zAxis, xAxis);

        return yAxis.unitVector();
    }

    public static RealVector polarToOrthogonal(RealVector polar){
        double r = polar.getEntry(0);
        double f = polar.getEntry(1);
        double t = polar.getEntry(2);
        return new ArrayRealVector(new double[]{ r*Math.sin(t)*Math.sin(f), r*Math.cos(t), r*Math.sin(t)*Math.cos(f)});
    }

    public static void copyRealVector(RealVector to, RealVector from){
        for(int i = 0; i<3; i++)
        {
            to.setEntry(i, from.getEntry(i));
        }
    }
}
