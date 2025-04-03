package akg.model.mtl;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MtlData {

    public List<Material> materials = new ArrayList<>();

    public static class Material{

        public String materialName;

        public RealVector Ka = new ArrayRealVector(new double[]{1,1,1});
        public RealVector Kd = new ArrayRealVector(new double[]{1,1,1});

        public RealVector Ks = new ArrayRealVector(new double[]{1,1,1});
        public double Ns = 32;

        public RealVector Ke = new ArrayRealVector(new double[]{0,0,0});
        public double Ni = 1;

        public double Tr = 0;

        public int illum = 0;

        public BufferedImage map_Kd;
        public BufferedImage map_Ke;
        public BufferedImage map_Ks;
        public BufferedImage map_Bump;
    }
}
