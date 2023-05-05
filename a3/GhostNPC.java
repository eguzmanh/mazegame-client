package a3;

import java.util.UUID;

import tage.*;
import org.joml.*;

// A ghost MUST be connected as a child of the root,
// so that it will be rendered, and for future removal.
// The ObjShape and TextureImage associated with the ghost
// must have already been created during loadShapes() and
// loadTextures(), before the game loop is started.

// ID can start at 0 because the id will never change for NPCS and 
// for now we will only have one NPM
public class GhostNPC extends GameObject
{
	private int id;

	public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) 
	{	super(GameObject.root(), s, t);
		this.id = id;
		setPosition(p);
	}

	public GhostNPC(int id, ObjShape s, Vector3f p) 
	{	super(GameObject.root(), s);
		this.id = id;
		setPosition(p);
	}

	// public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p)
	// { 
	// 	super(GameObject.root(), s, t);
	// 	this.id = id;
	// 	setPosition(p);
	// }
	public void setSize(boolean big)
	{ 
		if (!big) { this.setLocalScale((new Matrix4f()).scaling(0.5f)); }
		else { this.setLocalScale((new Matrix4f()).scaling(1.0f)); }
	}
	
	public int getID() { return id; }
	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }
}
