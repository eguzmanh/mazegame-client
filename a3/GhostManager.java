package a3;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;

public class GhostManager
{
	private MazeGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();

	private GhostNPC ghostNPC;

	public GhostManager(VariableFrameRateGame vfrg)
	{	game = (MazeGame)vfrg;
	}
	
	public void createGhostAvatar(UUID id, Vector3f position) throws IOException
	{	System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(0.25f);
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
	}
	
	public void removeGhostAvatar(UUID id)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		{	
			game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		}
		else
		{	System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	private GhostAvatar findAvatar(UUID id)
	{	GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext())
		{	ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0)
			{	return ghostAvatar;
			}
		}		
		return null;
	}
	
	public void updateGhostAvatar(UUID id, Vector3f position)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null)
		{	ghostAvatar.setPosition(position);
		}
		else
		{	System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}


	public void createGhostNPC(Vector3f position) throws IOException
	{ 
		if (ghostNPC == null)
			ghostNPC = new GhostNPC(0, game.getNPCshape(), position);
	}

	public void updateGhostNPC(Vector3f position, double gsize) {
		boolean gs;
		if (ghostNPC == null) { 
			System.out.println("Creating an NPC for the first time");
			try { 
				createGhostNPC(position);
			} catch (IOException e) { 
				System.out.println("error creating npc"); 
			}
		}
		
		ghostNPC.setPosition(position);
		if (gsize == 1.0) gs=false; else gs=true;
		ghostNPC.setSize(gs);
	}


}
