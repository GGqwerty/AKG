package akg.model.math;

import akg.model.obj.Face;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

public class FaceTriangulation {
    public static List<TriangleFace> triangleFan(Face face){
        List<TriangleFace> triangles = new ArrayList<>();
        Face.FaceVertex zero = face.vert.get(0);
        int faceSize = face.vert.size();
        List<Face.FaceVertex> verts = face.vert;
        for(int i = 1; i<faceSize-1; i++)
        {
            triangles.add(new TriangleFace(List.of(new Face.FaceVertex[]{verts.get(i), verts.get(i + 1), zero})));
        }
        return triangles;
    }

    public static List<RealVector> addNormals(List<TriangleFace> f, List<RealVector> vertex,  List<RealVector> normals){
        List<RealVector> list = new ArrayList<>(List.copyOf(normals));
        for(TriangleFace tr: f)
        {
            for(int i=0; i<3;i++)
            {
                Face.FaceVertex vert = tr.vert.get(i);
                if(vert.vnId==0)
                {
                    vert.vnId=list.size();
                    list.add(tr.getNormal(vertex));
                }
            }
        }
        return list;
    }
}
