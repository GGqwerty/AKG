package akg.model.canvas;

import akg.model.math.FaceTriangulation;
import akg.model.math.TriangleFace;
import akg.model.mtl.MtlData;
import akg.model.obj.Face;
import akg.model.obj.Lamp;
import akg.model.obj.ObjData;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static akg.model.light.FongCalculate.computePhongLighting;
import static akg.model.light.FongCalculate.shadePoint;
import static akg.model.math.FaceTriangulation.addNormals;
import static akg.model.math.MatrixTransform.*;
import static akg.model.math.Geometry.*;
import static akg.view.ImageDrawer.*;

public class CanvasObj extends CanvasElement {

    private List<TriangleFace> triangles = new ArrayList<>();

    private List<RealVector> modelVertex = null;

    private List<RealVector> modelTexture = null;

    private List<RealVector> modelNormal = null;

    private MtlData textures = null;

    private final double zNear = 0.001;

    private final double zFar = 1000;

    {
        eye = new ArrayRealVector(new double[]{0, 0, 1});

        target = new ArrayRealVector(new double[]{0, 0, 0});

        up = new ArrayRealVector(new double[]{0, 1, 0});

        translation = new ArrayRealVector(new double[]{0, 0, 0});

        scale = new ArrayRealVector(new double[]{1, 1, 1});

        angle = new ArrayRealVector(new double[]{0, 0, 0});

        drawMode = DrawMode.FONG;

        color=Color.WHITE.getRGB();
    }

    public CanvasObj(ObjData o) {
        o.f.forEach(x -> triangles.addAll(FaceTriangulation.triangleFan(x)));
        modelVertex=List.copyOf(o.v);
        modelNormal=addNormals(triangles, o.v, o.vn);
        modelTexture=List.copyOf(o.vt);
        textures=o.mtl;
    }

    @Override
    public void drawElement(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        List<RealVector> list = new ArrayList<>();
        list.add(new ArrayRealVector(new double[]{}));
        List<RealVector> listWorld = new ArrayList<>();
        listWorld.add(new ArrayRealVector(new double[]{}));
        List<RealVector> listWorldNormal = new ArrayList<>();
        listWorldNormal.add(new ArrayRealVector(new double[]{}));

        List<Double> wCoords = new ArrayList<>();
        wCoords.add(0.0);

        RealVector zAxis = calculateZAxis(eye, target);
        RealVector xAxis = calculateXAxis(up, zAxis);
        RealVector yAxis = calculateYAxis(up);

        RealMatrix trans = calculateTranslationTransform(translation);
        RealMatrix sc = calculateScaleTransform(scale);
        RealMatrix ang = calculateAngleTransform(angle);

        RealMatrix view = calculateViewTransform(xAxis, yAxis, zAxis, eye);
        //////////////////////////////////
        //RealMatrix projection = calculateProjectionOrtoTransform(w, h, 1, 1000);
        RealMatrix projection = calculateProjectionPerspecTransform(w, h, zNear, zFar);
        //////////////////////////////////
        RealMatrix viewport = calculateViewportTransform(w, h, 0, 0);

        RealMatrix transformationMatrix = viewport.multiply(projection).multiply(view).multiply(trans).multiply(sc).multiply(ang);

        RealMatrix transformationWorld = trans.multiply(sc).multiply(ang);

        for (int i = 1; i < modelVertex.size(); i++) {
            RealVector buf = transformationWorld.operate(modelVertex.get(i));
            listWorld.add(buf);
        }

        for (int i = 1; i < modelNormal.size(); i++) {
            RealVector buf = transformationWorld.operate(modelNormal.get(i).append(0.0));
            buf=buf.getSubVector(0, 3);
            listWorldNormal.add(buf);
        }

        double[][] zBuffer = new double[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                zBuffer[i][j] = Double.MAX_VALUE;
            }
        }

        for (int i = 1; i < modelVertex.size(); i++) {
            RealVector buf = transformationMatrix.operate(modelVertex.get(i));
            wCoords.add(buf.getEntry(3));
            ////////////////////
            ////////////////////
            if (buf.getEntry(2) > zNear && buf.getEntry(2) < zFar) {
                buf = buf.mapDivide(buf.getEntry(3));
            } else {
                if (drawMode != DrawMode.SKELETON) {
                    buf = buf.mapDivide(buf.getEntry(3));
                    buf.setEntry(2, Double.MAX_VALUE);
                }
            }
            ////////////////////
            list.add(buf);
        }

        RealVector buffer = transformationWorld.operate(target.append(1));
        switch (drawMode) {
            case SKELETON:
                triangles.parallelStream().forEach(x -> drawSkeleton(image, x, list, zBuffer));
                break;
            case FACE_HALF_SPACE:
                triangles.parallelStream().forEach(triangle -> {
                    triangleRasterizationHalfSpace(image, image.getWidth(), image.getHeight(),
                            list.get(triangle.vert.get(0).vId),
                            list.get(triangle.vert.get(1).vId),
                            list.get(triangle.vert.get(2).vId),
                            color, lamps, listWorld, triangle
                            , zBuffer, buffer);
                });
                break;
            case FACE_SCAN_LINE:
                triangles.parallelStream().forEach(triangle -> {
                    triangleRasterizationLineScan(image, triangle, list, zNear, zFar, zBuffer, color, lamps, listWorld);
                });
                break;
            case FONG:
                triangles.parallelStream().forEach(triangle -> {
                    triangleRasterizationHalfSpaceFong(image, image.getWidth(), image.getHeight(),
                            list.get(triangle.vert.get(0).vId),
                            list.get(triangle.vert.get(1).vId),
                            list.get(triangle.vert.get(2).vId),
                            color
                            , zBuffer, List.of(listWorldNormal.get(triangle.vert.get(0).vnId), listWorldNormal.get(triangle.vert.get(1).vnId), listWorldNormal.get(triangle.vert.get(2).vnId)), buffer);
                });
            case TEXTURE:
                if(textures==null || modelTexture == null)
                {
                    triangles.parallelStream().forEach(triangle -> {
                        triangleRasterizationHalfSpaceFong(image, image.getWidth(), image.getHeight(),
                                list.get(triangle.vert.get(0).vId),
                                list.get(triangle.vert.get(1).vId),
                                list.get(triangle.vert.get(2).vId),
                                color
                                , zBuffer, List.of(listWorldNormal.get(triangle.vert.get(0).vnId), listWorldNormal.get(triangle.vert.get(1).vnId), listWorldNormal.get(triangle.vert.get(2).vnId)), buffer);
                    });
                }
                else {
                    triangles.parallelStream().forEach(triangle -> {
                        triangleRasterizationHalfSpaceTexture(image, image.getWidth(), image.getHeight(),
                                list.get(triangle.vert.get(0).vId),
                                list.get(triangle.vert.get(1).vId),
                                list.get(triangle.vert.get(2).vId),
                                modelTexture.get(triangle.vert.get(0).vtId),
                                modelTexture.get(triangle.vert.get(1).vtId),
                                modelTexture.get(triangle.vert.get(2).vtId),
                                wCoords.get(triangle.vert.get(0).vId),
                                wCoords.get(triangle.vert.get(1).vId),
                                wCoords.get(triangle.vert.get(2).vId),
                                transformationWorld,
                                color
                                , zBuffer, List.of(listWorldNormal.get(triangle.vert.get(0).vnId), listWorldNormal.get(triangle.vert.get(1).vnId), listWorldNormal.get(triangle.vert.get(2).vnId)), buffer);
                    });
                }
                break;
        }
    }

    public void drawSkeleton(BufferedImage image, Face f, List<RealVector> list, double[][] zBuffer) {
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

    private static double edgeFunction(RealVector a, RealVector b, RealVector c) {
        return (c.getEntry(0) - a.getEntry(0)) * (b.getEntry(1) - a.getEntry(1)) -
                (c.getEntry(1) - a.getEntry(1)) * (b.getEntry(0) - a.getEntry(0));
    }

    private static double colorToNormal(int c){
        return (c/255.0)*2-1;
    }

    public void triangleRasterizationHalfSpaceTexture(BufferedImage image, int width, int height,
                                                      RealVector a, RealVector b, RealVector c,
                                                   RealVector at, RealVector bt, RealVector ct,
                                                   double aw, double bw, double cw,
                                                   RealMatrix transformationWorld,
                                                   int color, double[][] zBuffer, List<RealVector> m, RealVector target) {
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
                        //RealVector texturePoint = at.mapMultiply(w0).add(bt.mapMultiply(w1)).add(ct.mapMultiply(w2));
                        RealVector texturePoint = (at.mapMultiply(w0).mapDivide(aw).
                                add(bt.mapMultiply(w1).mapDivide(bw)).add(ct.mapMultiply(w2).mapDivide(cw)))
                                .mapDivide(w0/aw+w1/bw+w2/cw);
                        Color objectColor = new Color(color);
                        if(textures.materials.get(0).map_Kd!=null) {
                            int xT = (int) (texturePoint.getEntry(0) * textures.materials.get(0).map_Kd.getWidth());
                            int yT = (int) ((1 - texturePoint.getEntry(1)) * textures.materials.get(0).map_Kd.getHeight());
                            objectColor = new Color(textures.materials.get(0).map_Kd.getRGB(xT, yT));
                        }
                        if(textures.materials.get(0).map_Bump!=null) {
                            int xN = (int) (texturePoint.getEntry(0) * textures.materials.get(0).map_Bump.getWidth());
                            int yN = (int) ((1-texturePoint.getEntry(1)) * textures.materials.get(0).map_Bump.getHeight());
                            Color normalColor = new Color(textures.materials.get(0).map_Bump.getRGB(xN, yN));

                            RealVector eVector = textures.materials.get(0).Ke.mapMultiply(255);
                            Color eColor = new Color((int) eVector.getEntry(0), (int) eVector.getEntry(1), (int) eVector.getEntry(2));
                            RealVector dVector = textures.materials.get(0).Kd.mapMultiply(255);
                            Color dColor = new Color((int) dVector.getEntry(0), (int) dVector.getEntry(1), (int) dVector.getEntry(2));
                            RealVector sVector = textures.materials.get(0).Ks.mapMultiply(255);
                            Color sColor = new Color((int) sVector.getEntry(0), (int) sVector.getEntry(1), (int) sVector.getEntry(2));
                            RealVector normal = new ArrayRealVector(new double[]
                                    {
                                            colorToNormal(normalColor.getRed()),
                                            colorToNormal(normalColor.getGreen()),
                                            colorToNormal(normalColor.getBlue())
                                    });
                            normal = transformationWorld.operate(normal.append(0.0)).getSubVector(0, 3);
                            double ns = textures.materials.get(0).Ns;
                            double specularCoef = 1;
                            if(textures.materials.get(0).map_Ks!=null)
                            {
                                specularCoef = textures.materials.get(0).map_Ks.getRGB(
                                        (int) (texturePoint.getEntry(0) * textures.materials.get(0).map_Ks.getWidth()),
                                        (int) ((1-texturePoint.getEntry(1)) * textures.materials.get(0).map_Ks.getHeight())
                                );
                            }
                            Color color1 = Color.BLACK;
                            for (Lamp lamp : lamps) {
                                Color buf = computePhongLighting(normal,
                                        lamp.light.subtract(target.getSubVector(0, 3)), eye.subtract(target.getSubVector(0, 3)),
                                        objectColor, eColor,
                                        dColor, sColor,
                                        0.1, 1,
                                        specularCoef, ns);
                                color1 = new Color(Math.min(255, buf.getRed() + color1.getRed()),
                                        Math.min(255, buf.getGreen() + color1.getGreen()),
                                        Math.min(255, buf.getBlue() + color1.getBlue()));
                            }
                            image.setRGB(x, y, color1.getRGB());
                        }
                        else{
                            image.setRGB(x, y, objectColor.getRGB());
                        }
                    }
                }
            }
        }
    }

    public void triangleRasterizationHalfSpaceFong(BufferedImage image, int width, int height, RealVector a, RealVector b, RealVector c,
                                               int color, double[][] zBuffer, List<RealVector> m, RealVector target) {
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
                        Color color1 = Color.BLACK;
                        for(Lamp lamp: lamps){
                            Color buf = computePhongLighting(m.get(0).mapMultiply(w0)
                                    .add(m.get(1).mapMultiply(w1))
                                    .add(m.get(2).mapMultiply(w2))
                                    , lamp.light.subtract(target.getSubVector(0, 3)), eye.subtract(target.getSubVector(0, 3)), new Color(color), new Color(color), lamp.color, lamp.color, ambientCoef, diffuseCoef, specularCoef, shininess);
                            color1=new Color(Math.min(255, buf.getRed()+color1.getRed()),
                                    Math.min(255, buf.getGreen()+color1.getGreen()),
                                    Math.min(255, buf.getBlue()+color1.getBlue()));
                        }
                        image.setRGB(x, y, color1.getRGB());
                    }
                }
            }
        }
    }

    public void triangleRasterizationHalfSpace(BufferedImage image, int width, int height, RealVector a, RealVector b, RealVector c,
                                  int color, List<Lamp> lamps, List<RealVector> listWorld, TriangleFace f, double[][] zBuffer, RealVector target) {

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

        Color col = new Color(color);
        Color buf = Color.BLACK;

        for(Lamp lamp: lamps){
            Color buf2 = lamp.coef.calculate(0.0, lamp.color, col, lamp.light.subtract(target.getSubVector(0,3)).getNorm()==0||f.getNormal(listWorld).getNorm()==0?0:lamp.light.subtract(target.getSubVector(0,3)).cosine(f.getNormal(listWorld)));
            buf = new Color(Math.min(255, buf2.getRed()+buf.getRed()), Math.min(255, buf2.getGreen()+buf.getGreen()), Math.min(255, buf2.getBlue()+buf.getBlue()));
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
                RealVector pixel = new ArrayRealVector(new double[]{x + 0.5, y + 0.5, 0});
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
                        image.setRGB(x, y, buf.getRGB());
                    }
                }
            }
        }
    }

    int jjj = 0;

    public void triangleRasterizationLineScan(BufferedImage image, TriangleFace f, List<RealVector> list, double zNear, double zFar, double[][] zBuffer, int color, List<Lamp> lamps, List<RealVector> listWorld) {
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
            jjj++;
            Color col = new Color(color);
            Color buf = Color.BLACK;
            for(Lamp lamp: lamps){
                Color buf2 = lamp.coef.calculate(0.0, lamp.color, col, lamp.light.getNorm()==0||f.getNormal(listWorld).getNorm()==0?0:lamp.light.cosine(f.getNormal(listWorld)));
                buf = new Color(Math.min(255, buf2.getRed()+buf.getRed()), Math.min(255, buf2.getGreen()+buf.getGreen()), Math.min(255, buf2.getBlue()+buf.getBlue()));
            }
            polygonRasterization(image, f, list, zNear, zFar, zBuffer, buf.getRGB());
        }
    }

    public void polygonRasterization(BufferedImage image, Face f, List<RealVector> list, double zNear, double zFar, double[][] zBuffer, int color) {
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
