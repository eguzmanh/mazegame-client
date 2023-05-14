package a3;

import java.util.UUID;

import tage.*;
import org.joml.*;

import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;

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
	private IAudioManager audioMgr;
	private Sound ghostSound;
	private MazeGame game;

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

	public void initAudio(){
		AudioResource resource1, resource2, resource3;
		audioMgr = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");

		if(!audioMgr.initialize()){
			System.out.println("Audio Manager failed to initialize");
			return;
		}

		resource2 = audioMgr.createAudioResource("assets/audio/classic-ghost-sound-95773.wav", AudioResourceType.AUDIO_SAMPLE);

		ghostSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);

		ghostSound.initialize(audioMgr);
		ghostSound.setMaxDistance(10.0f);
		ghostSound.setMinDistance(0.5f);
		ghostSound.setRollOff(5.0f);

		ghostSound.setLocation(this.getPosition());

		game.setEarParamenters();

		ghostSound.play();
	}

	// public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p)
	// { 
	// 	super(GameObject.root(), s, t);
	// 	this.id = id;
	// 	setPosition(p);
	// }
	public void setSize(boolean big)
	{ 
		if (!big) { this.setLocalScale((new Matrix4f()).scaling(2.0f)); }
		else { this.setLocalScale((new Matrix4f()).scaling(4.0f)); }
	}
	
	public int getID() { return id; }
	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getLocalLocation(); }
}
