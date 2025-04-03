package akg.model.obj;

import akg.model.interfacies.CalculateColor;
import org.apache.commons.math3.linear.RealVector;

import java.awt.*;

public class Lamp {
    public RealVector light;
    public Color color;
    public CalculateColor coef;

    public Lamp(RealVector light, Color color, CalculateColor coef){
        this.light=light;
        this.color=color;
        this.coef=coef;
    }
}
