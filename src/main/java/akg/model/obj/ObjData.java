package akg.model.obj;

import akg.model.mtl.MtlData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ObjData {

    public MtlData mtl = null;

    public List<RealVector> v = new ArrayList<>();
    protected int vCounter=1;

    public List<RealVector> vt = new ArrayList<>();
    protected int vtCounter=1;

    public List<RealVector> vn = new ArrayList<>();
    protected int vnCounter=1;

    public List<Face> f = new ArrayList<>();
    protected int fCounter=1;

    {
        v.add(new ArrayRealVector());
        vt.add(new ArrayRealVector());
        vn.add(new ArrayRealVector());
    }

    public void addV(double x, double y, double z){
        v.add(new ArrayRealVector(new double[]{x, y, z, 1}));
        vCounter++;
    }

    public void addV(double x, double y, double z, double w){
        v.add(new ArrayRealVector(new double[]{x, y, z, w}));
        vCounter++;
    }

    public void addVt(double u){
        vt.add(new ArrayRealVector(new double[]{u}));
        vtCounter++;
    }

    public void addVt(double u, double v){
        vt.add(new ArrayRealVector(new double[]{u, v}));
        vtCounter++;
    }

    public void addVt(double u, double v, double w){
        vt.add(new ArrayRealVector(new double[]{u, v, w}));
        vtCounter++;
    }

    public void addVn(double i, double j, double k){
        vn.add(new ArrayRealVector(new double[]{i, j, k}));
        vnCounter++;
    }

    public void addF(List<Face.FaceVertex> vert){
        f.add(new Face(fCounter, vert));
        fCounter++;
    }

    
    
}
