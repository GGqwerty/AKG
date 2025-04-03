package akg.model.canvas;

import akg.model.obj.Lamp;
import org.apache.commons.math3.linear.RealVector;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

public abstract class CanvasElement {

     public int jjj=0;

     public abstract void drawElement(BufferedImage image);

     public RealVector eye;

     public RealVector target;

     public RealVector up;

     public RealVector angle;

     public RealVector scale;

     public RealVector translation;

     public int color;

     public double ambientCoef = 0.1;
     public double diffuseCoef = 1;
     public double specularCoef = 1;
     public double shininess = 16;

     public List<Lamp> lamps = new ArrayList<>();

     public DrawMode drawMode;

     public enum DrawMode{
          SKELETON("Скелет"), FACE_SCAN_LINE("Грани линия"), FACE_HALF_SPACE("Грани половна пространство"),
          FONG("Модель по фонгу"), TEXTURE("Текстура");

          public String text;

          DrawMode(String text){
               this.text=text;
          }
          @Override
          public String toString(){
               return text;
          }
     }
}
