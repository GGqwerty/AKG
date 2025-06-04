package akg;

import akg.model.canvas.CanvasObj;
import akg.model.obj.ObjData;
import akg.parser.ObjParser;
import akg.view.Holst;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args){
        //String path = "D:\\АКГ\\Лаба 1\\escandalosos.obj";
        //String path = "D:\\АКГ\\Лаба 1\\blank_body.obj";
        //String path = "D:\\АКГ\\Лаба 1\\cude\\cube.obj";
        String path = "D:\\АКГ\\Лаба 1\\Robot Steampunk\\robot_steampunk.obj";
        //String path = "D:\\АКГ\\Лаба 1\\fem.obj";
        //String path = "D:\\АКГ\\Лаба 1\\model.obj";
        ObjData o = ObjParser.parseObjFile(path);
        CanvasObj c = new CanvasObj(o);

/*        String path2 = "D:\\АКГ\\Лаба 1\\Lamba.obj";
        ObjData o2 = ObjParser.parseObjFile(path2);
        CanvasObj c2 = new CanvasObj(o2);*/

        JFrame mainFrame = new JFrame();
        Holst canvas = new Holst();

        canvas.canvas.addElement(c);
/*        canvas.addElement(c2);*/

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setSize(new Dimension(1200, 600));

        mainFrame.getContentPane().add(canvas);
        mainFrame.setVisible(true);
        o=null;
    }
}
