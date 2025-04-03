package akg.view;

import java.awt.image.BufferedImage;

public class ImageDrawer {

    public static void drawPoint(BufferedImage i, int x, int y, int color)
    {
        if(x<0 || x>=i.getWidth() || y<0 || y>=i.getHeight())
            return;
        i.setRGB(x, y, color);
    }

    public static void drawPointThroughZBuffer(BufferedImage i, int x, int y, double z, int color, double zNear, double zFar, double[][] zBuffer){
        if(z<zNear || z>zFar || x<0 || x>=i.getWidth() || y<0 || y>=i.getHeight() || z>zBuffer[x][y]) {
            return;
        }
        zBuffer[x][y]=z;
        i.setRGB(x, y, color);
    }

    public static void drawLine(BufferedImage i, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPoint(i, x1, y1, color);

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    public static void drawYLineThroughZBuffer(BufferedImage image, int y, int x1, double z1, int x2, double z2, int color, double zNear, double zFar, double[][] zBuffer) {
        int dx = Math.abs(x2 - x1);
        int sx = x1 < x2 ? 1 : -1;
        int x = x1;
        double z = z1;
        double sz = (z2-z1)/(dx-1!=0?dx-1:1);
        for(int i =0; i<=dx; i++)
        {
            drawPointThroughZBuffer(image, x, y, z, color, zNear, zFar, zBuffer);
            x+=sx;
            z+=sz;
        }
    }
}
