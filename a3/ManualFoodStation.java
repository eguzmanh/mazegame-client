package a3;

import tage.*; 
import tage.shapes.*; 
import org.joml.*; 

public class ManualFoodStation extends ManualObject {
	float[] vertices = { 
      -1.0f,  1.50f, -1.0f,    -1.0f, -1.50f, -1.0f,     1.0f, -1.50f, -1.0f, // back face lower left
      1.0f, -1.50f, -1.0f,     1.0f,  1.50f, -1.0f,    -1.0f,  1.50f, -1.0f, // back face upper right
      1.0f, -1.50f, -1.0f,     1.0f, -1.50f,  1.0f,     1.0f,  1.50f, -1.0f, // right face lower back
      1.0f, -1.50f,  1.0f,     1.0f,  1.50f,  1.0f,     1.0f,  1.50f, -1.0f, // right face upper front
      1.0f, -1.50f,  1.0f,     -1.0f, -1.50f,  1.0f,    1.0f,  1.50f,  1.0f, // front face lower right
      -1.0f, -1.50f,  1.0f,    -1.0f,  1.50f,  1.0f,     1.0f,  1.50f,  1.0f, // front face upper left
      -1.0f, -1.50f,  1.0f,    -1.0f, -1.50f, -1.0f,    -1.0f,  1.50f,  1.0f, // left face lower front
      -1.0f, -1.50f, -1.0f,    -1.0f,  1.50f, -1.0f,    -1.0f,  1.50f,  1.0f, // left face upper back
      -1.0f, -1.50f,  1.0f,     1.0f, -1.50f,  1.0f,     1.0f, -1.50f, -1.0f, // bottom face right front
      1.0f, -1.50f, -1.0f,    -1.0f, -1.50f, -1.0f,    -1.0f, -1.50f,  1.0f, // bottom face left back
      -1.0f,  1.50f, -1.0f,     1.0f,  1.50f, -1.0f,     1.0f,  1.50f,  1.0f, // top face right back
      1.0f,  1.50f,  1.0f,    -1.0f,  1.50f,  1.0f,    -1.0f,  1.50f, -1.0f  // top face left front
    }; 

	float[] texCoords = new float[] { 
      1.0f, 1.0f,  1.0f, 0.0f,  0.0f, 0.0f, // back face
      0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,

      1.0f, 0.0f,  0.0f, 0.0f,  1.0f, 1.0f, // right face
      0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,

      1.0f, 0.0f,  0.0f, 0.0f,  1.0f, 1.0f, // front face
      0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,

      1.0f, 0.0f,  0.0f, 0.0f,  1.0f, 1.0f, // left face
      0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,

      0.0f, 1.0f,  1.0f, 1.0f,  1.0f, 0.0f, // bottom face
      1.0f, 0.0f,  0.0f, 0.0f,  0.0f, 1.0f,

      0.0f, 1.0f,  1.0f, 1.0f,  1.0f, 0.0f, // top face
      1.0f, 0.0f,  0.0f, 0.0f,  0.0f, 1.0f 
   };

	float[] normals = new float[] { 
      0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f, // back face
      0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,
      1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f, // right face
      1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
      0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f, // front face
      0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,
      -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f, // left face
      -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,
      0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f, // bottom face
      0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,
      0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f, // top face
      0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f 
   };

	/** Creates a 36-vertex rectangular prism with texture coordinates ranging from (0,0) to (1,1) on each side. */
	public ManualFoodStation() {	
      super();
      setNumVertices(36);
		setVertices(vertices);
		setTexCoords(texCoords);
		setNormals(normals);

        setMatAmb(Utils.goldAmbient()); 
        setMatDif(Utils.goldDiffuse()); 
        setMatSpe(Utils.goldSpecular()); 
        setMatShi(Utils.goldShininess()); 
		setWindingOrderCCW(false);
	}
}