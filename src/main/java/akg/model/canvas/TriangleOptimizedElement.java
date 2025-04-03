package akg.model.canvas;

import akg.model.math.FaceTriangulation;
import akg.model.math.TriangleFace;
import akg.model.obj.ObjData;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TriangleOptimizedElement extends CanvasElement{

    protected ObjData obj;

    public Random random = new Random();

    public double zNear = 0.001;

    public double zFar = 1000;

    {
        eye = new ArrayRealVector(new double[]{0, 0, 1});

        target = new ArrayRealVector(new double[]{0, 0, 0});

        up = new ArrayRealVector(new double[]{0, 1, 0});

        translation = new ArrayRealVector(new double[]{0, 0, 0});

        scale = new ArrayRealVector(new double[]{1, 1, 1});

        angle = new ArrayRealVector(new double[]{0, 0, 0});

        drawMode = DrawMode.FACE_HALF_SPACE;
    }

    protected List<TriangleFace> triangles = new ArrayList<>();

    public TriangleOptimizedElement(ObjData o) {
        obj = o;
        obj.f.forEach(x -> triangles.addAll(FaceTriangulation.triangleFan(x)));
    }


    @Override
    public void drawElement(BufferedImage image) {

    }
}
