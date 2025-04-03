package akg.parser;

import akg.model.mtl.MtlData;
import akg.model.obj.Face;
import akg.model.obj.ObjData;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MtlParser {

    public static MtlData parseMtlFile(String filepath, String filename) {
        String file;
        try {
            file = Files.readString(Path.of(filepath+"\\"+filename));
        } catch (IOException e) {
            return null;
        }

        return parseMtl(file, filepath);
    }

    public static MtlData parseMtl(String file, String directory) {
        MtlData obj = new MtlData();
        String[] lines = file.split("\r?\n");
        MtlData.Material material = null;
        for (String line : lines) {
            String[] elements = line.split(" ");
            StringBuilder b = new StringBuilder();
            if (elements.length == 0)
                continue;
            try {
                switch (elements[0]) {
                    case "newmtl":
                        if(material!=null)
                            obj.materials.add(material);
                        material = new MtlData.Material();
                        material.materialName=elements[1];
                        break;
                    case "Ns":
                        if(material!=null) {
                            material.Ns = Double.parseDouble(elements[1]);
                        }
                        break;
                    case "d":
                    case "Tr":
                        if(material!=null) {
                            material.Tr = Double.parseDouble(elements[1]);
                        }
                        break;
                    case "Ti":
                        if(material!=null) {
                            material.Ni = Double.parseDouble(elements[1]);
                        }
                        break;
                    case "Ka":
                        if(material!=null && elements.length==4) {
                            material.Ka = new ArrayRealVector(new double[]{Double.parseDouble(elements[1]),
                                    Double.parseDouble(elements[2]), Double.parseDouble(elements[3])});
                        }
                        break;
                    case "Kd":
                        if(material!=null && elements.length==4) {
                            material.Kd = new ArrayRealVector(new double[]{Double.parseDouble(elements[1]),
                                    Double.parseDouble(elements[2]), Double.parseDouble(elements[3])});
                        }
                        break;
                    case "Ks":
                        if(material!=null && elements.length==4) {
                            material.Ks = new ArrayRealVector(new double[]{Double.parseDouble(elements[1]),
                                    Double.parseDouble(elements[2]), Double.parseDouble(elements[3])});
                        }
                        break;
                    case "Ke":
                        if(material!=null && elements.length==4) {
                            material.Ke = new ArrayRealVector(new double[]{Double.parseDouble(elements[1]),
                                    Double.parseDouble(elements[2]), Double.parseDouble(elements[3])});
                        }
                        break;
                    case "illum":
                        if(material!=null) {
                            material.illum = Integer.parseInt(elements[1]);
                        }
                        break;
                    case "map_Ks":
                        if(material!=null) {
                            b=new StringBuilder(directory+"\\");
                            for(int i =1; i< elements.length; i++)
                            {
                                b.append(elements[i]).append(" ");
                            }
                            material.map_Ks = ImageIO.read(new File(b.toString()));
                        }
                        break;
                    case "map_Kd":
                        if(material!=null) {
                            b=new StringBuilder(directory+"\\");
                            for(int i =1; i< elements.length; i++)
                            {
                                b.append(elements[i]).append(" ");
                            }
                            material.map_Kd = ImageIO.read(new File(b.toString()));
                        }
                        break;
                    case "map_Ke":
                        if(material!=null) {
                            b=new StringBuilder(directory+"\\");
                            for(int i =1; i< elements.length; i++)
                            {
                                b.append(elements[i]).append(" ");
                            }
                            material.map_Ke = ImageIO.read(new File(b.toString()));
                        }
                        break;
                    case "norm":
                    case "map_Bump":
                        if(material!=null) {
                            b=new StringBuilder(directory+"\\");
                            for(int i =1; i< elements.length; i++)
                            {
                                b.append(elements[i]).append(" ");
                            }
                            material.map_Bump = ImageIO.read(new File(b.toString()));
                        }
                        break;
                    default:
                        continue;
                }
            } catch (NumberFormatException | IOException ex) {
                continue;
            }
        }
        if(material!=null)
            obj.materials.add(material);
        return obj;
    }

}
