package akg.view;

import akg.model.canvas.CanvasObj;
import akg.model.cubemap.CubeMap;
import akg.model.obj.ObjData;
import akg.parser.CubeMapParser;
import akg.parser.ObjParser;

import javax.swing.*;
import java.awt.*;

public class Holst extends JPanel {

    public Canvas canvas = new Canvas();

    public ToolBar toolBar = new ToolBar(Color.BLACK);

    {
        add(canvas);
        add(toolBar);
        toolBar.colorChooserBack=(x->
        {canvas.choosenElement.color=x.getRGB();
            canvas.repaint();
        });
        toolBar.backgroundColorChooserBack=(x->
        {canvas.setBackGroundColor(x);
            canvas.repaint();
        });
        canvas.addElementCallback=(x->{
            toolBar.modes.setSelectedIndex(x.ordinal());
        });
        toolBar.fileChooserBack=(x->
        {canvas.elements.clear();
            ObjData o = ObjParser.parseObjFile(x.getPath());
            CanvasObj c = new CanvasObj(o);
            canvas.elements.clear();
            canvas.addElement(c);
            canvas.choosenElement=c;
            canvas.repaint();
        });
        toolBar.directoryChooserBack=(x->
        {
            CubeMap cubeMap = CubeMapParser.parseCubeMp(x);
            canvas.setCubeMap(cubeMap);
            canvas.repaint();
        });
        toolBar.modesChooserBack=(x->
        {
            if(canvas.choosenElement!=null)
                canvas.choosenElement.drawMode=x;
            canvas.repaint();
        });
        toolBar.modes.setSelectedIndex(2);
    }

    public Holst(){

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setLayout(null);
        canvas.setBounds(0, 50, getWidth(), getHeight());
        toolBar.setBounds(0, 0, getWidth(), 50);
    }
}
