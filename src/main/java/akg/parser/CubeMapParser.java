package akg.parser;

import akg.model.cubemap.CubeMap;
import akg.model.mtl.MtlData;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CubeMapParser {

    public static CubeMap parseCubeMp(File directory) {
        CubeMap cubeMap = new CubeMap();
        for (File f : directory.listFiles()) {
            try {
                switch (f.getName().split("\\.")[0]) {
                    case "posx":
                        cubeMap.posx = ImageIO.read(f);
                        break;
                    case "posy":
                        cubeMap.posy = ImageIO.read(f);
                        break;
                    case "posz":
                        cubeMap.posz = ImageIO.read(f);
                        break;
                    case "negx":
                        cubeMap.negx = ImageIO.read(f);
                        break;
                    case "negy":
                        cubeMap.negy = ImageIO.read(f);
                        break;
                    case "negz":
                        cubeMap.negz = ImageIO.read(f);
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(cubeMap.posx==null
            || cubeMap.posy==null
            || cubeMap.posz==null
            || cubeMap.negx==null
            || cubeMap.negy==null
            || cubeMap.negz==null) {
            return null;
        }
        else{
            return cubeMap;
        }
    }

}
