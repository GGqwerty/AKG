package akg.model.cubemap;

import akg.model.canvas.CanvasElement;
import akg.model.math.TriangleFace;
import akg.model.obj.Face;
import akg.model.obj.Lamp;
import org.apache.commons.math3.linear.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static akg.model.math.Geometry.edgeFunction;
import static akg.model.math.MatrixTransform.*;

public class CubeMap {
    public BufferedImage posx;
    public BufferedImage posy;
    public BufferedImage posz;
    public BufferedImage negx;
    public BufferedImage negy;
    public BufferedImage negz;

    public RealVector eye;

    public RealVector eyePolar;

    public RealVector target;

    public RealVector up;


    private final double zNear = 0.001;

    private final double zFar = 1000;

    {
        eye = new ArrayRealVector(new double[]{0, 0, 1});

        target = new ArrayRealVector(new double[]{0, 0, 0});

        up = new ArrayRealVector(new double[]{0, 1, 0});
    }

    public void drawMap(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        RealMatrix invertProjection = invertMatrix(calculateProjectionPerspecTransform(w, h, zNear, zFar));

        RealMatrix cameraRotation = createCameraRotation(eyePolar.getEntry(1), eyePolar.getEntry(2)-Math.PI/2);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float ndcX = (2.0f * x) / w - 1.0f;
                float ndcY = 1.0f - (2.0f * y) / h;

                RealVector rayDir = unprojectDirection(ndcX, ndcY, invertProjection, cameraRotation);

                Color color = cubeMapSample(rayDir);

                image.setRGB(x, y, color.getRGB());
            }
        }
    }

    public static RealMatrix invertMatrix(RealMatrix matrix) {
        DecompositionSolver solver = new LUDecomposition(matrix).getSolver();

        if (!solver.isNonSingular()) {
            return matrix;
        }

        return solver.getInverse();
    }

    RealVector unprojectDirection(double ndcX,
                                  double ndcY,
                                  RealMatrix inverseProjection,
                                  RealMatrix rotationMatrix
    ) {
        RealVector clip = new ArrayRealVector(new double[]{ndcX, ndcY, -1.0, 1.0});

        RealVector eye = inverseProjection.operate(clip);
        double[] e = eye.toArray();
        RealVector dir = new ArrayRealVector(new double[]{e[0], e[1], -1.0});

        dir = dir.mapDivide(Math.sqrt(dir.dotProduct(dir)));

        return rotationMatrix.operate(dir).unitVector();
    }

    public static RealMatrix createCameraRotation(double yaw, double pitch) {

        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);


        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        double[][] rotation = new double[][] {
                {
                        cosYaw, 0, -sinYaw
                },
                {
                        sinYaw * sinPitch, cosPitch, cosYaw * sinPitch
                },
                {
                        sinYaw * cosPitch, -sinPitch, cosPitch * cosYaw
                }
        };

        return MatrixUtils.createRealMatrix(rotation).transpose();
    }

    public Color cubeMapSample(RealVector dir) {
        double x = dir.getEntry(0);
        double y = dir.getEntry(1);
        double z = dir.getEntry(2);

        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        BufferedImage face;
        double uc = 0, vc = 0;
        double maxAxis;

        if (absX >= absY && absX >= absZ) {
            maxAxis = absX;
            if (x > 0) {
                face = posx;
                uc = -z;
                vc = y;
            } else {
                face = negx;
                uc = z;
                vc = y;
            }
        } else if (absY >= absX && absY >= absZ) {
            maxAxis = absY;
            if (y > 0) {
                face = posy;
                uc = x;
                vc = -z;
            } else {
                face = negy;
                uc = x;
                vc = z;
            }
        } else {
            maxAxis = absZ;
            if (z > 0) {
                face = posz;
                uc = x;
                vc = y;
            } else {
                face = negz;
                uc = -x;
                vc = y;
            }
        }

        double u = 0.5 * (uc / maxAxis + 1.0);
        double v = 0.5 * (vc / maxAxis + 1.0);

        int texX = Math.min((int)(u * face.getWidth()), face.getWidth() - 1);
        int texY = Math.min((int)((1.0 - v) * face.getHeight()), face.getHeight() - 1);

        return new Color(face.getRGB(texX, texY));
    }

}
