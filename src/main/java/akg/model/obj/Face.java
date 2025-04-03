package akg.model.obj;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Face {

    public int id;

    public List<FaceVertex> vert;

    public Face(int id, List<FaceVertex> vertexes){
        this.id=id;
        vert = vertexes;
    }

    public static class FaceVertex{
        public int vId;
        public int vtId;
        public int vnId;

        public FaceVertex(){}

        public FaceVertex(int vId, int vtId, int vnId){
            this.vId=vId;
            this.vtId=vtId;
            this.vnId=vnId;
        }

        public static FaceVertex valueOf(String[] verts){
            switch(verts.length)
            {
                case 1:
                    return new FaceVertex(Integer.parseInt(verts[0]), 0, 0);
                case 2:
                    return new FaceVertex(Integer.parseInt(verts[0]), Integer.parseInt(verts[1]), 0);
                case 3:
                    if(verts[1].isEmpty())
                    {
                        return new FaceVertex(Integer.parseInt(verts[0]), 0, Integer.parseInt(verts[2]));
                    }
                    else {
                        return new FaceVertex(Integer.parseInt(verts[0]), Integer.parseInt(verts[1]), Integer.parseInt(verts[2]));
                    }
                default:
                    return null;
            }
        }
    }
}
