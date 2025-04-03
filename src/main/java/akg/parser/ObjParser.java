package akg.parser;

import akg.model.mtl.MtlData;
import akg.model.obj.Face;
import akg.model.obj.ObjData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ObjParser {

    public static ObjData parseObjFile(String filePath) {
        String file;
        try {
            Path buf = Path.of(filePath);
            file = Files.readString(Path.of(filePath));
        } catch (IOException e) {
            return null;
        }

        return parseObj(file, Path.of(filePath).getParent());
    }

    public static ObjData parseObj(String file, Path directory) {
        ObjData obj = new ObjData();
        String[] lines = file.split("\r?\n");
        StringBuilder b = new StringBuilder();
        for (String line : lines) {
            String[] elements = line.split(" ");
            if (elements.length == 0)
                continue;
            try {
                switch (elements[0]) {
                    case "v":
                        switch (elements.length) {
                            case 4:
                                obj.addV(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]));
                                break;
                            case 5:
                                obj.addV(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]), Double.parseDouble(elements[4]));
                                break;
                            default:
                                continue;
                        }
                        break;
                    case "vt":
                        switch (elements.length) {
                            case 2:
                                obj.addVt(Double.parseDouble(elements[1]));
                                break;
                            case 3:
                                obj.addVt(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]));
                                break;
                            case 4:
                                obj.addVt(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]));
                                break;
                            default:
                                continue;
                        }
                        break;
                    case "vn":
                        switch (elements.length) {
                            case 4:
                                obj.addVn(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3]));
                                break;
                            default:
                                continue;
                        }
                        break;
                    case "f":
                        if (elements.length < 4) {
                            continue;
                        } else {
                            List<Face.FaceVertex> vList = new ArrayList<>();
                            for (int i = 1; i < elements.length; i++) {
                                Face.FaceVertex buf = Face.FaceVertex.valueOf(elements[i].split("/"));
                                if(buf!=null)
                                    vList.add(buf);
                            }
                            obj.addF(vList);
                        }
                        break;
                    case "mtllib":
                        b=new StringBuilder();
                        for(int i =1; i< elements.length; i++)
                        {
                            b.append(elements[i]).append(" ");
                        }
                        MtlData buf = MtlParser.parseMtlFile(directory.toString(), b.toString().trim());
                        if(buf!=null)
                        {
                            obj.mtl=buf;
                        }
                        break;
                    default:
                        continue;
                }
            } catch (NumberFormatException ex) {
                continue;
            }
        }

        return obj;
    }

}
