package akg.model.light;

import org.apache.commons.math3.linear.RealVector;

import java.awt.*;

import static akg.model.math.TriangleFace.computeBarycentric;

public class FongCalculate {
    public static Color shadePoint(
            RealVector p, RealVector v0, RealVector v1, RealVector v2,
            RealVector n0, RealVector n1, RealVector n2,
            RealVector lightDir, RealVector viewDir, Color objectColor,
            Color ambientColor, Color diffuseColor, Color specularColor,
            double ambientCoef, double diffuseCoef, double specularCoef, double shininess) {

        RealVector bary = computeBarycentric(p.getSubVector(0, 2), v0.getSubVector(0, 2), v1.getSubVector(0, 2), v2.getSubVector(0, 2));
        double alpha = bary.getEntry(0), beta = bary.getEntry(1), gamma = bary.getEntry(2);

        RealVector normal = n0.mapMultiply(alpha)
                .add(n1.mapMultiply(beta))
                .add(n2.mapMultiply(gamma))
                .unitVector();

        return computePhongLighting(normal, lightDir, viewDir, objectColor, ambientColor, diffuseColor,  specularColor, ambientCoef,  diffuseCoef, specularCoef, shininess);
    }

    public static Color computePhongLighting(
            RealVector normal, RealVector lightDir, RealVector viewDir,
            Color objectColor, Color ambientColor, Color diffuseColor, Color specularColor,
            double ambientCoeff, double diffuseCoeff, double specularCoeff, double shininess) {

        normal = normal.unitVector();
        lightDir = lightDir.unitVector();
        viewDir = viewDir.unitVector();

        double ambientR = ambientCoeff * ambientColor.getRed()/255.0;
        double ambientG = ambientCoeff * ambientColor.getGreen()/255.0;
        double ambientB = ambientCoeff * ambientColor.getBlue()/255.0;

        double diffuseFactor = Math.max(0, normal.dotProduct(lightDir));
        double diffuseR = diffuseCoeff *diffuseColor.getRed()/255.0 * (objectColor.getRed() / 255.0f) * diffuseFactor;
        double diffuseG = diffuseCoeff *diffuseColor.getGreen()/255.0 * (objectColor.getGreen() / 255.0f) * diffuseFactor;
        double diffuseB = diffuseCoeff *diffuseColor.getBlue()/255.0 * (objectColor.getBlue() / 255.0f) * diffuseFactor;
/*        double diffuseR = diffuseCoeff * diffuseColor.getRed() * diffuseFactor;
        double diffuseG = diffuseCoeff * diffuseColor.getGreen() * diffuseFactor;
        double diffuseB = diffuseCoeff * diffuseColor.getBlue() * diffuseFactor;*/
        
        RealVector reflectDir = reflect(lightDir.mapMultiply(-1), normal);
        double specularFactor = Math.pow(Math.max(0, reflectDir.dotProduct(viewDir)), shininess);
        double specularR = specularCoeff * specularColor.getRed()/255.0 * specularFactor;
        double specularG = specularCoeff * specularColor.getGreen()/255.0 * specularFactor;
        double specularB = specularCoeff * specularColor.getBlue()/255.0 * specularFactor;

        int r = Math.min(255, (int) ((ambientR+diffuseR+specularR)*255));
        int g = Math.min(255, (int) ((ambientG+diffuseG+specularG)*255));
        int b = Math.min(255, (int) ((ambientB+diffuseB+specularB)*255));

        return new Color(r, g, b);
    }

    private static RealVector reflect(RealVector L, RealVector N) {
        return L.subtract(N.mapMultiply(2 * L.dotProduct(N)));
    }
}
