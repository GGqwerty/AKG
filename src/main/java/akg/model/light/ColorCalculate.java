package akg.model.light;

import java.awt.*;

public class ColorCalculate {

    public static Color calculateColorNoDist(Double distance, Color light, Color src, Double angle){
            if(angle<0)
                return new Color(0);
            return new Color(
                    Math.max(0, Math.min(255, (int) (light.getRed()/255.0 * angle * src.getRed()))),
                    Math.max(0, Math.min(255, (int) (light.getGreen()/255.0*angle * src.getGreen()))),
                    Math.max(0, Math.min(255, (int) (light.getBlue()/255.0*angle * src.getBlue()))));
    }
}
