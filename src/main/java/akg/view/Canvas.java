package akg.view;

import akg.model.canvas.CanvasElement;
import akg.model.cubemap.CubeMap;
import akg.model.light.ColorCalculate;
import akg.model.obj.Lamp;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static akg.model.math.MatrixTransform.*;

public class Canvas extends JPanel {

    public List<CanvasElement> elements = new ArrayList<>();

    protected RealVector mainEye = new ArrayRealVector(new double[]{0, 0, 10});

    protected RealVector mainEyePolar = new ArrayRealVector(new double[]{10, 0, Math.PI/2});

    protected RealVector mainUp = new ArrayRealVector(new double[]{0, 1, 0});

    protected RealVector mainTarget = new ArrayRealVector(new double[]{0,0,0});

    protected CubeMap cubeMap = null;

    protected CanvasElement choosenElement;

    protected void setBackGroundColor(Color color){
        backGroundColor = color;
    }

    protected Color backGroundColor = Color.BLACK;

    protected int lastX, lastY;

    {
        copyRealVector(mainEye, polarToOrthogonal(mainEyePolar));
    }

    public Consumer<CanvasElement.DrawMode> addElementCallback;

    public void addElement(CanvasElement c){
        c.eye = this.mainEye;
        c.lamps.add(new Lamp(new ArrayRealVector(new double[]{3, 3, 3}), Color.WHITE, ColorCalculate::calculateColorNoDist));
        //c.lamps.add(new Lamp(new ArrayRealVector(new double[]{10, 10, 10}), Color.RED, ColorCalculate::calculateColorNoDist));
        mainUp = calculateUpVector(mainEye, mainTarget);
        c.up = mainUp;
        elements.add(c);
        choosenElement=c;
        if(addElementCallback!=null)
            addElementCallback.accept(choosenElement.drawMode);
        c.cubeMap=cubeMap;
    }

    public void setCubeMap(CubeMap cubeMap){
        this.cubeMap=cubeMap;
        if(cubeMap!=null) {
            cubeMap.eye = mainEye;
            cubeMap.target = mainTarget;
            cubeMap.up = mainUp;
            cubeMap.eyePolar=mainEyePolar;
        }
        for(CanvasElement element: elements)
        {
            element.cubeMap=cubeMap;
        }
    }

    {
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

                switch (e.getKeyCode()){
                    case KeyEvent.VK_W:
                        mainEyePolar.setEntry(2, Math.max((mainEyePolar.getEntry(2)-Math.PI/90.0), 0+0.001));
                        copyRealVector(mainEye, polarToOrthogonal(mainEyePolar));
                        break;
                    case KeyEvent.VK_S:
                        mainEyePolar.setEntry(2, Math.min((mainEyePolar.getEntry(2)+Math.PI/90.0), Math.PI-0.001));
                        copyRealVector(mainEye, polarToOrthogonal(mainEyePolar));
                        break;
                    case KeyEvent.VK_A:
                        mainEyePolar.setEntry(1, (mainEyePolar.getEntry(1)-Math.PI/90.0));
                        copyRealVector(mainEye, polarToOrthogonal(mainEyePolar));
                        break;
                    case KeyEvent.VK_D:
                        mainEyePolar.setEntry(1, mainEyePolar.getEntry(1)+Math.PI/90.0);
                        copyRealVector(mainEye, polarToOrthogonal(mainEyePolar));
                        break;
                    case KeyEvent.VK_UP:
                        choosenElement.translation.setEntry(2, choosenElement.translation.getEntry(2)+1);
                        break;
                    case KeyEvent.VK_DOWN:
                        choosenElement.translation.setEntry(2, choosenElement.translation.getEntry(2)-1);
                        break;
                    case KeyEvent.VK_LEFT:
                        choosenElement.translation.setEntry(0, choosenElement.translation.getEntry(0)+1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        choosenElement.translation.setEntry(0, choosenElement.translation.getEntry(0)-1);
                        break;
                    case KeyEvent.VK_ADD:
                        choosenElement.translation.setEntry(1, choosenElement.translation.getEntry(1)-1);
                        break;
                    case KeyEvent.VK_SUBTRACT:
                        choosenElement.translation.setEntry(1, choosenElement.translation.getEntry(1)+1);
                        break;
                    case KeyEvent.VK_MULTIPLY:
                        choosenElement.scale=choosenElement.scale.mapMultiply(1.1);
                        break;
                    case KeyEvent.VK_DIVIDE:
                        choosenElement.scale=choosenElement.scale.mapDivide(1.1);
                        break;

                }
                copyRealVector(mainUp, calculateUpVector(mainEye, mainTarget));
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        setFocusable(true);
    }

    {
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double buf = mainEyePolar.getEntry(0);
                double notches = e.getWheelRotation();
                if(buf<1&&notches<0)
                    return;
                double scale = (buf + notches*(buf>10*Math.abs(notches)?10:0.5))/buf;
                mainEyePolar.setEntry(0, mainEyePolar.getEntry(0)*scale);
                copyRealVector(mainEye, mainTarget.add(polarToOrthogonal(mainEyePolar)));
                copyRealVector(mainUp, calculateUpVector(mainEye, mainTarget));
                repaint();
            }
        });
        setFocusable(true);
    }

    {
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dy = lastX-e.getX();
                int dx = lastY-e.getY();
                choosenElement.angle.setEntry(0, choosenElement.angle.getEntry(0)+Math.toRadians(-dx/10.0));
                choosenElement.angle.setEntry(1, choosenElement.angle.getEntry(1)+Math.toRadians(-dy/10.0));
                lastX=e.getX();
                lastY=e.getY();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastX=e.getX();
                lastY=e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }


    @Override
    public void paintComponent(Graphics g){
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i<image.getWidth(); i++)
        {
            for(int j=0; j<image.getHeight();j++)
            {
                image.setRGB(i, j, backGroundColor.getRGB());
            }
        }
        if(cubeMap!=null) {
            cubeMap.drawMap(image);
        }
        /*for(CanvasElement e: elements)
        {
            e.drawElement(image);
        }*/
        choosenElement.drawElement(image);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}
