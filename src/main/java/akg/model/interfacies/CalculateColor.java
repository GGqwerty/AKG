package akg.model.interfacies;

import java.awt.*;

@FunctionalInterface
public interface CalculateColor {
    Color calculate(Double distance, Color light, Color src, Double angle);
}
