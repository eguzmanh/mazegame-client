package a3;

import tage.*;
import tage.Light.LightType;
import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.networking.IGameConnection.ProtocolType;
import tage.nodeControllers.*;
import tage.shapes.*;

import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.nio.FloatBuffer;


// Scripting portion
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;


import org.joml.*;
import net.java.games.input.Component.Identifier.Key;
import net.java.games.input.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.List;

//physics
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.PhysicsEngineFactory;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;


public class MazeGame extends VariableFrameRateGame {
	private static Engine engine;
	private Camera engineCamera, overheadEngineCamera;
	private OverheadCameraController ohCameraController;
	private InputManager engineIM; 
	private HUDmanager hudManager;
	private Viewport engineCameraV, engineOHCameraV;

	private NodeController rc, bc;

	private int prizeCounter, prizeCounterWin, numPrizes, numFoodStations;

	private int lives, maxLives;
	private boolean paused, gameOver;
	private String winnerUUID;
	// private boolean gameOver;

	private float timer, foodLevel, foodLevelHungerThreshold, gameworldEdgeBound, minGameObjectYLoc;
	private boolean isMounted, isInPlayerBounds;
	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject plyr, x, y, z, groundPlane, foodTorus, invisPlane;
	private ObjShape linxS, linyS, linzS, prizeS, foodStationS, groundPlaneS, foodTorusS, mazeS, npcS, invisPlaneS;
	private TextureImage plyrtx, fstx, terrTx, forestFloor, mazeTx, npcTx;
	private int spaceBox;
	private TerrainPlane terrain;
	private AnimatedShape plyrS;

	private Light light1;
	private Light light2;
	private boolean lightOff = false;

	private CameraOrbit3D orbit3DController;
	
	private ArrayList<TextureImage> customTextures;
	private ArrayList<Prize> prizes;
	private ArrayList<FoodStation> foodStations;

	// private File scriptFile1, scriptFile2, scriptFile3;
	// private long fileLastModifiedTime = 0;
	ScriptEngine jsEngine;

	private ScriptFactory scriptFactory;


	private float foodTorusAzimuth, // start BEHIND and ABOVE the target 
				foodTorusElevation, // elevation is in degrees 
				foodTorusRadius; // distance from camera to avatar
	private float foodStorageBuf;
	private boolean foodStorageEmpty;

	private GhostManager gm;
	private GhostNPC ghost;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	private NetworkClient networkClient;

	//audio stuff
	private IAudioManager audioMgr;
	private Sound rainSound, walkSound, ghostSound, backgroundMusic;
	private boolean isWalking;

	//physics stuff
	private PhysicsEngine physEng;
	private PhysicsObject plyrP, mazeP, planeP;
	private boolean running = false;
	private boolean isJumping = false;
	private int direction;
	private float vals[] = new float[16];
	

	public MazeGame(String serverAddress, int serverPort, String protocol) { 
		super();
		initScriptEngine();
		initGameVariables();
		customTextures = new ArrayList<TextureImage>();  // allows prizes to get a random texture from the options available
		prizes = new ArrayList<Prize>();  // allows dynamic number of prizes
		foodStations = new ArrayList<FoodStation>(); // allows dynamic number of food stations

		networkClient = new NetworkClient(this, serverAddress, serverPort, protocol);
	}

	public static void main(String[] args) {	
		MazeGame game = new MazeGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	public void initScriptEngine() {
		scriptFactory = new ScriptFactory();
		scriptFactory.initJSEngine();
		scriptFactory.addJSScript("InitData", "assets/scripts/InitData.js");
		scriptFactory.addJSScript("RuntimeDataUpdate", "assets/scripts/RuntimeDataUpdate.js", true);
		scriptFactory.runJSScripts();
	}
	
	/******************************************************
	 * 	  * VariableFrameRateGame function overrides
	******************************************************/
	@Override
	public void loadShapes() {
		plyrS = new AnimatedShape("BasicGuy2.rkm","BasicGuy2.rks");
		plyrS.loadAnimation("WALK", "BasicGuy2.rka");
		linxS = new Line(new Vector3f(-gameworldEdgeBound,0f,0f), new Vector3f(gameworldEdgeBound,0f,0f)); 
		linyS = new Line(new Vector3f(0f,-gameworldEdgeBound,0f), new Vector3f(0f,gameworldEdgeBound,0f)); 
		linzS = new Line(new Vector3f(0f,0f,-gameworldEdgeBound), new Vector3f(0f,0f,gameworldEdgeBound));

		// prizeS = new Sphere();
		
		// foodStationS = new ManualFoodStation();

		groundPlaneS = new TerrainPlane(1000);

		invisPlaneS = new Plane();

		mazeS = new ImportedModel("maze1.obj");

		// foodTorusS = new Torus(1.0f, 1.0f,48);

		npcS = new ImportedModel("GhostNew.obj");

		// ((Plane)groundPlaneS).setPlaneSize(new Vector3f(0f,0f,-1000f), new Vector3f(0f,0f,1000f));
	}

	@Override
	public void loadTextures() {
		plyrtx = new TextureImage("Basic Guy UV_Purple.jpg");
		// fstx = new TextureImage("Drawer_Door.jpg");
		forestFloor = new TextureImage("forest_floor_diff_4k.jpg");
		terrTx = new TextureImage("MazeHeightMap.jpg");
		// customTextures.add(new TextureImage("Wood_Desk.png"));
		// customTextures.add(new TextureImage("Floral_Sheet.png"));
		mazeTx = new TextureImage("rustic_stone_wall_diff_4k.jpg");
		npcTx = new TextureImage("Ghost UV.jpg");
	}

	@Override
	public void loadSkyBoxes(){
		spaceBox = (engine.getSceneGraph()).loadCubeMap("lightblue");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(spaceBox);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects() {	
		buildWorldAxisLines();
		buildGroundPlane();
		buildPlayer();
		// buildPrizes();
		// buildFoodStations();	
		// buildFoodTorus();
		buildNPCGhost();
		// buildMaze();
	}

	@Override
	public void initializeLights() {	
		Light.setGlobalAmbient(0.2f, 0.2f, 0.2f);
		light1 = new Light();
		light1.setType(LightType.POSITIONAL);
		// System.out.println("Light type: " + light1.getLightType());
		light1.setLocation(new Vector3f(0f, 20.0f, 0f));
		(engine.getSceneGraph()).addLight(light1);

		light2 = new Light();
		light2.setType(LightType.SPOTLIGHT);
		light2.setLocation(plyr.getWorldLocation());
		light2.setDirection(plyr.getLocalForwardVector());
		(engine.getSceneGraph()).addLight(light2);
	}

	@Override
	public void createViewports() {
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1f,1f); 
		(engine.getRenderSystem()).addViewport("OVERHEAD",.55f ,0,.45f,.4f); 
		
		engineCameraV = engine.getRenderSystem().getViewport("MAIN");
		engineOHCameraV = engine.getRenderSystem().getViewport("OVERHEAD");
		
		engineOHCameraV.setHasBorder(true); 
		engineOHCameraV.setBorderWidth(3); 
		engineOHCameraV.setBorderColor(65.1f, 0.0f, 1.0f); 
	}

	@Override
	public void initializeGame() {	
		initEngineComponents();
		initAudio();
		initMainInputManagerActions();  // handle the Input Manager detection
		initPhysicsWorld();
		networkClient.setupNetworking();
	}

	@Override
	public void update() {	// rotate Player if not paused
		// Update elapsed time regardless of the game status
		upateElapsedTimeInfo(); // elapsed time for only the current render and previous render
		
		checkGameOver();
		
		Vector3f loc = plyr.getWorldLocation();
		float height = groundPlane.getHeight(loc.x(), loc.z());

		plyr.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
		
		scriptFactory.update("js");
		if (scriptFactory.modificationUccurred()) { syncScriptData(); }
		
		if (suspendGame()) return;
		
		// Orbit controller for the Player
		orbit3DController.updateCameraPosition();
		
		plyrS.updateAnimation();

		if(!lightOff){
			//System.out.println("Light On");
			(engine.getSceneGraph()).addLight(light2);
		} else {
			(engine.getSceneGraph()).removeLight(light2);
		}

		light2.setLocation(plyr.getWorldLocation());
		light2.setDirection(plyr.getLocalForwardVector());
		
		
		// validatePrizeCollisions();
		// validateFoodStationCollisions();
		
		// decreaseFoodLevel(); // ensures that player eata from food stations in order to win
		
		// rotateTorusIfFoodAvailable();

		// build and set HUD
		buildHUDs();
		processNetworking((float)elapsTime);
		
		//update sound
		walkSound.setLocation(plyr.getWorldLocation());
		//ghostSound.setEmitDirection(ghost.getWorldLocation(), 360f);
		backgroundMusic.setEmitDirection(new Vector3f(0,0,0), 360f);
		setEarParamenters();

		//update physics
		updatePhysics(running);
	}
	
	/******************************************************
	 * 				* Helper functions for setup
	******************************************************/

	// A lot of this data can be used in the scripts when implemented
	private void initGameVariables() {
		// isMounted = false;
		paused = false;
		// isInPlayerBounds = true;
		gameOver = false;
		elapsTime = 0.0f;
		timer = 0.0f;
		foodStorageEmpty = true;
		gameworldEdgeBound = 10000f;
		minGameObjectYLoc = 1.2f;

		syncScriptData();
		
		initTimeFrames();
	}

	private void syncScriptData() {
		// moving to script engine 
		numPrizes = scriptFactory.getIntFromEngine("js", "numPrizes");
		prizeCounter = scriptFactory.getIntFromEngine("js", "prizeCounter");
		prizeCounterWin =scriptFactory.getIntFromEngine("js", "prizeCounterWin");
		numFoodStations = scriptFactory.getIntFromEngine("js", "numFoodStations");
		foodStorageBuf = scriptFactory.getDoubleFVFromEngine("js", "foodStorageBuf");
		foodLevel = scriptFactory.getDoubleFVFromEngine("js", "foodLevel");
		foodLevelHungerThreshold =scriptFactory.getDoubleFVFromEngine("js", "foodLevelHungerThreshold");
	}
	/**
	 * Helper functions used to build the intial 3D World Objects
	 */
	private void buildPlayer() {
		Matrix4f initialTranslation, initialScale, initialRotation;
	
		// build Player in the center of the window
		plyr = new GameObject(GameObject.root(), plyrS, plyrtx);
		networkClient.setGhostShape(plyrS);
		networkClient.setGhostTexture(plyrtx);
		initialTranslation = (new Matrix4f()).translation(34.30f,minGameObjectYLoc,-486.6f); 
		initialScale = (new Matrix4f()).scaling(0.5f);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f)); 
		
		plyr.setLocalTranslation(initialTranslation);
		plyr.setLocalRotation(initialRotation); 
		plyr.setLocalScale(initialScale);
	}

	private void buildNPCGhost() {
		// npc = new GhostNPC(0, npcS, new Vector3f(0.0f, 2.0f, 0.0f));
		networkClient.setNPCshape(npcS);
		networkClient.setNPCtexture(npcTx);
	}
	
	private void buildWorldAxisLines() {
		x = new GameObject(GameObject.root(), linxS); 
		y = new GameObject(GameObject.root(), linyS); 
		z = new GameObject(GameObject.root(), linzS); 
	
		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f)); 
		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f)); 
		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f)); 
	}

	private void buildGroundPlane() {
		groundPlane = new GameObject(GameObject.root(), groundPlaneS, forestFloor);
		Matrix4f initialScale = (new Matrix4f()).scaling(500.0f, 30.0f, 500.0f);
		Matrix4f initialTranslation = (new Matrix4f()).translation(0f, -3f, 0f);

		groundPlane.setLocalScale(initialScale);
		groundPlane.setLocalTranslation(initialTranslation);

		groundPlane.setHeightMap(terrTx);
		
		groundPlane.getRenderStates().setTiling(1);
	}

	// Used to set the random positions for the prizes and food stations
	private void setObjectTRS(GameObject wgo, float translationYAmnt) {
		Matrix4f initialTranslation, initialScale, initialRotation;
		initialTranslation = (new Matrix4f()).translation(getRandomTranslationFloat(), minGameObjectYLoc + translationYAmnt, getRandomTranslationFloat()); 
		initialScale = (new Matrix4f()).scaling(getRandomScale());
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(getRandomRotationAngle()));
		wgo.setLocalTranslation(initialTranslation);
		wgo.setLocalRotation(initialRotation); 
		wgo.setLocalScale(initialScale);
		
		if (wgo instanceof Prize) {
			prizes.add((Prize)wgo);
		}

		if (wgo instanceof FoodStation) {
			foodStations.add((FoodStation)wgo);
		}
	}  

	/**
	 * Choose a random Texture Image from the custom created ones
	 * @return
	 */
	private TextureImage getRandomCustomTexture() {
		int size = customTextures.size();
		int ridx  = new Random().nextInt(size);
		return customTextures.get(ridx);
	}
	/**
	 * Functions to retrieve a random float for when placing objects on the screen
	 * This allows a dynamic amount of prizes and foodstations to render
	*/
	private float getRandomFloatInRange(float min, float max) {
		Random r = new Random();
		return 	min + (r.nextFloat()) * (max-min);  // get a random float in the range of [min, max)
	}
	private float getRandomTranslationFloat() {
		return getRandomFloatInRange(-350f, 350f);  // Used to generate a random transalation for foodstations and prizes
	}
	private float getRandomRotationAngle() {
		return getRandomFloatInRange(0f, 360f);  // Used to generate a random rotaton angle
	}
	private float getRandomScale() {
		return getRandomFloatInRange(0f, 3.5f);  // used to get a random scale value for the Game Objects
	}

	private void initTimeFrames() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
	}

	public void initAudio(){
		AudioResource resource1, resource2, resource3;
		audioMgr = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");

		if(!audioMgr.initialize()){
			System.out.println("Audio Manager failed to initialize");
			return;
		}

		resource1 = audioMgr.createAudioResource("assets/audio/indoor-footsteps-6385.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("assets/audio/classic-ghost-sound-95773.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("assets/audio/spookyMusic.wav", AudioResourceType.AUDIO_STREAM);

		walkSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		//rainSound = new Sound(resource2, SoundType.SOUND_EFFECT, 15, true);
		ghostSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		backgroundMusic = new Sound(resource3, SoundType.SOUND_MUSIC, 15, true);

		walkSound.initialize(audioMgr);
		ghostSound.initialize(audioMgr);
		backgroundMusic.initialize(audioMgr);
		walkSound.setMaxDistance(1.0f);
		walkSound.setMinDistance(0.0f);
		walkSound.setRollOff(5.0f);
		ghostSound.setMaxDistance(10.0f);
		ghostSound.setMinDistance(0.5f);
		ghostSound.setRollOff(5.0f);
		backgroundMusic.setMaxDistance(500.0f);
		backgroundMusic.setMinDistance(0.0f);
		backgroundMusic.setRollOff(5.0f);

		walkSound.setLocation(plyr.getWorldLocation());
		//ghostSound.setLocation(ghost.getWorldLocation());
		backgroundMusic.setLocation(new Vector3f(0,0,0));

		setEarParamenters();

		backgroundMusic.play();
		//ghostSound.play();
	}

	public void setEarParamenters(){
		audioMgr.getEar().setLocation(plyr.getWorldLocation());
		audioMgr.getEar().setOrientation(engineCamera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	private void initEngineComponents() {
		engineIM = engine.getInputManager();
		initViewportCameras();
		initEngineHUDManager();
		initEngineRenderSystemView();
		initNodeControllers();
		init3DOrbitController();
	}
	private void initViewportCameras() {
		engineCamera = engine.getRenderSystem().getViewport("MAIN").getCamera();
		overheadEngineCamera = engine.getRenderSystem().getViewport("OVERHEAD").getCamera();
		
		engineCamera.setLocation(new Vector3f(-2,0,2)); 
		engineCamera.setU(new Vector3f(1,0,0)); 
		engineCamera.setV(new Vector3f(0,1,0)); 
		engineCamera.setN(new Vector3f(0,0,-1)); 
		
		overheadEngineCamera.setLocation(new Vector3f(0,2,0)); 
		overheadEngineCamera.setU(new Vector3f(1,0,0)); 
		overheadEngineCamera.setV(new Vector3f(0,0,-1)); 
		overheadEngineCamera.setN(new Vector3f(0,-1,0)); 

		ohCameraController = new OverheadCameraController(overheadEngineCamera, plyr, engine);
	}
	private void initEngineHUDManager(){
		hudManager = engine.getHUDmanager();
		hudManager.setWindowDimensions();
	}
	private void initEngineRenderSystemView() {
		(engine.getRenderSystem()).setWindowDimensions(1000,700);
	}
	private void initNodeControllers() {
		// rotation controller used for when a prize or foodstation is collided with
		// rc = new RotationController(engine, new Vector3f(0,1,0), 0.001f);
		// rc.enable(); // turn on the rotation controller

		// bc = new BounceController(engine, 4f);
		// bc.enable(); // turn on the rotation controller

		// (engine.getSceneGraph()).addNodeController(rc); 
		// (engine.getSceneGraph()).addNodeController(bc); 
	}
	private void init3DOrbitController() {
		// ------------- setup 3d Orbit Controller for the main camera -------------
		// adjustCameraView(-1.85f, 0.6f, -0.75f);
		orbit3DController = new CameraOrbit3D(engineCamera, plyr, engine);
		System.out.println("orbit3DController: " + orbit3DController);
	}

	/**
	 * Get all of the actions and sync them to the TAGE input manager
	 * This will allow us to also use a remote and account for multiple devices of the same kind
	 */
	private void initMainInputManagerActions() {
		ShutdownAction shutdownActionCmd = new ShutdownAction(this);
		ScreenModeToggleAction screenModeToggleActionCmd = new ScreenModeToggleAction(this);
		AxisLinesAction axisLinesActionCmd = new AxisLinesAction(this);
		PauseAction pauseActionCmd = new PauseAction(this);
		WireframeAction wireframeActionCmd = new WireframeAction(this);
		// SpacebarAction spacebarAction = new SpacebarAction(this);
		FwdBwdAction fwdbwdActionCmd = new FwdBwdAction(this); 
		TurnAction turnActionCmd = new TurnAction(this); 
		// PitchAction pitchActionCmd = new PitchAction(this); 
		// EatAction eatActionCmd = new EatAction(this);
		EatAction eatActionCmd = new EatAction(this);
		JumpAction jumpAction = new JumpAction(this);

		shutdownActionCmd.associateDeviceInputs();
		screenModeToggleActionCmd.associateDeviceInputs();
		axisLinesActionCmd.associateDeviceInputs();
		pauseActionCmd.associateDeviceInputs();
		wireframeActionCmd.associateDeviceInputs();
		// spacebarAction.associateDeviceInputs();
		fwdbwdActionCmd.associateDeviceInputs();
		turnActionCmd.associateDeviceInputs();
		// pitchActionCmd.associateDeviceInputs();
		// eatActionCmd.associateDeviceInputs();
		eatActionCmd.associateDeviceInputs();
		jumpAction.associateDeviceInputs();
	}

	private void initPhysicsWorld(){
		//initialize physics system
		String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0f, -5f, 0f};
		physEng = PhysicsEngineFactory.createPhysicsEngine(engine);
		physEng.initSystem();
		physEng.setGravity(gravity);

		//create physics world
		float mass = 1.0f;
		float up[] = {0,1,0};
		double[] tempTransform;


		
		
		//player physics object
		Matrix4f translation = new Matrix4f(plyr.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		plyrP = physEng.addCapsuleObject(physEng.nextUID(), mass, tempTransform, 0.75f, 2.0f);
		plyrP.setBounciness(0.0f);
		plyrP.setFriction(1.0f);
		plyrP.setDamping(0.9f, 0.9f);
		plyr.setPhysicsObject(plyrP);

		//Top Collision
		// translation = new Matrix4f(invisPlane.getLocalTranslation());
		// tempTransform = toDoubleArray(translation.get(vals));
		// planeP = physEng.addStaticPlaneObject(physEng.nextUID(), tempTransform, up, 20f);
		// planeP.setBounciness(0.0f);
		// invisPlane.setPhysicsObject(planeP);

		//maze floor
		translation = new Matrix4f(groundPlane.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		mazeP = physEng.addStaticPlaneObject(physEng.nextUID(), tempTransform, up, 1.5f);
		mazeP.setBounciness(0.0f);
		mazeP.setFriction(0.5f);
		mazeP.setDamping(0.9f, 0.9f);
		groundPlane.setPhysicsObject(mazeP);

		

	}

	private void updatePhysics(boolean running){
		if(running){
			//System.out.println("Physics On");
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			checkForCollisions();

			isJumpingAndMoving();

			physEng.update((float)elapsTime);
			for(GameObject go:engine.getSceneGraph().getGameObjects()){
				if(go.getPhysicsObject() != null){
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0, mat.m30());
					mat2.set(3,1, mat.m31());
					mat2.set(3,2, mat.m32());
					go.setLocalTranslation(mat2);
				}
			}
		}
	}

	public void isJumpingAndMoving(){
		Vector3f fwdDirection = plyr.getWorldForwardVector();
		float force = 50f;
	
		if (isJumping && plyrP.getLinearVelocity()[1] < 0.1) {
			System.out.println(isJumping);
			plyrP.applyForce(0,400,0,0,0,0);
			isJumping = false; // reset jump flag
		}
	
		if(direction == 1){
			plyrP.applyForce(fwdDirection.x*force, 0, fwdDirection.z*force, 0, 0, 0);
		} else if (direction == 2) {
			plyrP.applyForce(-fwdDirection.x*force, 0, -fwdDirection.z*force, 0, 0, 0);
		} else {
			//do nothing
		}
	}

	private void checkForCollisions(){
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

		dynamicsWorld = ((JBulletPhysicsEngine)physEng).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i<manifoldCount; i++){
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for(int j=0; j<manifold.getNumContacts(); j++){
				contactPoint = manifold.getContactPoint(j);
				if(contactPoint.getDistance() <0.0f){
					System.out.println("Hit between "+obj1+"and "+obj2);
					break;
				}
			}
		}
	}
	
	private float[] toFloatArray(double[] arr){
		if(arr == null){return null;}
		int n = arr.length;
		float[] ret = new float[n];
		for(int i=0; i<n; i++){
			ret[i] = (float)arr[i];
		}
		return ret;
	}

	private double[] toDoubleArray(float[] arr){
		if(arr == null){return null;}
		int n = arr.length;
		double[] ret = new double[n];
		for(int i=0; i<n; i++){
			ret[i] = (double)arr[i];
		}
		return ret;
	}
	/***************************************************************************
	 * Action functions for working linking InputManager with  hw devices
	****************************************************************************/
	/** 
	 * These public methods will allow the Actions to be called to be called while encapsulating the implementation
	 */
	public void animationFWDToggle(boolean isAnimated) { _animationFWDToggle(isAnimated); }

	public void animationBWDToggle(boolean isAnimated) { _animationBWDToggle(isAnimated); }

	public void walkSound(boolean isWalking) { _walkSound(isWalking); }

	public void shutdownAction() { _shutdownAction(); } 

	public void screenModeToggleAction() { _screenModeToggleAction(); }  

	public void wireframeAction() { _wireframeAction(); }  

	public void pauseAction() { _pauseAction(); }

	public void turnAction(float newSpeed) { _turnAction(newSpeed); }
	
	public void pitchAction(float newSpeed) { _pitchAction(newSpeed); }
	
	public void fwdBwdAction(float newSpeed){ _fwdBwdAction(newSpeed); }
	
	public void axisLinesAction(boolean axisLinesEnabled) { _axisLinesAction(axisLinesEnabled); }
	
	// public void eatAction() { _eatAction(); }

	public void jumpAction() { _jumpAction(); }

	private void _walkSound(boolean isWalking){
		if(isWalking){
			walkSound.play();
		} else {
			walkSound.stop();
		}
	}

	private void _animationFWDToggle(boolean isAnimated) {
		if(isAnimated){
			plyrS.stopAnimation();
			plyrS.playAnimation("WALK", 0.3f, AnimatedShape.EndType.LOOP, 0);
		} else {
			plyrS.stopAnimation();
		}
	}

	private void _animationBWDToggle(boolean isAnimated) {
		if(isAnimated){
			plyrS.stopAnimation();
			plyrS.playAnimation("WALK", -0.3f, AnimatedShape.EndType.LOOP, 0);
		} else {
			plyrS.stopAnimation();
		}
	}

	private void _shutdownAction() {
		shutdown();
		System.exit(0);
	}

	private void _screenModeToggleAction() {
		(engine.getRenderSystem()).toggleFullScreenMode();
	}

	private void _wireframeAction() {
		if (plyr.getRenderStates().isWireframe()) {
			plyr.getRenderStates().setWireframe(false);
		} else {
			plyr.getRenderStates().setWireframe(true);
		}
		
	}
	
	private void _pauseAction() {
		System.out.println("Game is paused. . .");
		paused = paused ? false : true;
	}

	private void _turnAction(float newSpeed){
		Matrix4f worldYM = y.getWorldRotation();
		plyr.yaw(newSpeed, worldYM);
	}

	private void _pitchAction(float newSpeed) {
		plyr.pitch(newSpeed);
	}

	private void _fwdBwdAction(float newSpeed){
		Vector3f oldPosition, fwdDirection, newLocation;

		oldPosition = plyr.getWorldLocation(); 
		fwdDirection = plyr.getWorldForwardVector(); // N vector 
		newLocation = oldPosition.add(fwdDirection.mul(newSpeed)); 

		// prevents Player from moving below the plane
		if(newLocation.y() <= minGameObjectYLoc) { 
			newLocation.set(newLocation.x(), minGameObjectYLoc, newLocation.z());
		}
		plyr.setLocalLocation(newLocation);
		
	}

	private void _axisLinesAction(boolean axisLinesEnabled) {
		if(axisLinesEnabled) {
			(x.getRenderStates()).enableRendering();
			(y.getRenderStates()).enableRendering();
			(z.getRenderStates()).enableRendering();
		} else {
			(x.getRenderStates()).disableRendering();
			(y.getRenderStates()).disableRendering();
			(z.getRenderStates()).disableRendering();
		}
	}

	// private void _eatAction() {
	// 	if (foodStorageBuf > 0.0f) {
	// 		foodLevel += foodStorageBuf;
	// 		foodStorageBuf = 0.0f;
	// 		foodStorageEmpty = true;
	// 	}
	// }
	private void _eatAction() {
		if (foodStorageBuf > 0.0f) {
			foodLevel += foodStorageBuf;
			foodStorageBuf = 0.0f;
			foodStorageEmpty = true;
		}
	}
	
	private void _jumpAction() {
		isJumping = true;
	}
	/******************************************************
	 * 		* Helper functions for the update steps
	******************************************************/
	/**
	 * Update the elapsed time in the game to keep track of the game + movement
	 */
	private void upateElapsedTimeInfo() {
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		
		if (!paused) {
			elapsTime = (currFrameTime - lastFrameTime);
			timer += (float) elapsTime / 1000.0f;  // timer holds the addition of elapsedTime to simulated seconds
		}
		engineIM.update((float)elapsTime);  
		// System.out.println("Elapsed Time::: " + elapsTime);
	}
	
	/**
	 * Decreases the food level and ensures that it remains unsigned
	 */
	private void decreaseFoodLevel() {
		// System.out.println("FoodLevel: " + Integer.toString(Math.round(foodLevel)));
		float decN = (float)elapsTime / 1500.0f;
		// System.out.println("elapsTime: " + Integer.toString(decN));
		if((foodLevel-decN) < 0.0f) foodLevel = 0.0f;
		if (foodLevel == 0.0f) return;
		foodLevel -= decN;
	}

	/**
	 * Create HUDs to display necessary information on the screen
	 */
	private void buildHUDs() {
		int elapsTimeSec = Math.round(timer);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String livesStr = Integer.toString(lives);
		//String foodLevelStr = Integer.toString(Math.round(foodLevel));

		String dispStr1 = "Current Lives = " + livesStr;
		//String dispStr2 = "Food Level = " + foodLevelStr;

		String ds3X = String.format(" {X: %.2f}", plyr.getWorldLocation().x());
		String ds3Y = String.format(" {Y: %.2f}", plyr.getWorldLocation().y());
		String ds3Z = String.format(" {Z: %.2f}", plyr.getWorldLocation().z());
		String dispStr3 = "Player:" + ds3X + ds3Y + ds3Z;

		Vector3f hud1Color = new Vector3f(1,1,1);
		Vector3f hud2Color = new Vector3f(1,1,1);
		Vector3f hud3Color = new Vector3f(1,1,1);

		// Set the color of the displays
		// Change the HUD's in case the window size becomes too small
		if(hudManager.isCanvasTabletMode()) {
			hudManager.setHUD1(dispStr1, hud1Color, 60, 60);
			//hudManager.setHUD2(dispStr2, hud2Color, 60, 15);
		} else {
			hudManager.setHUD1(dispStr1, hud1Color, 15, 15);
			//hudManager.setHUD2(dispStr2, hud2Color, 300, 15);
		}

		hudManager.setHUD3(dispStr3, hud3Color, (int)engineOHCameraV.getLeftOnCanvas() + 15, 15);
	}
	

	/**
	 * Checks that the player's food level is not below a certain amount
	 * @return
	 */
	// private boolean playerIsHungry() { return foodLevel <= foodLevelHungerThreshold; }

	/**
	 * This function will contain the tests that the function must pass in order to allow the collection of
	 * a prize during a collision.
	 * @return
	 */
	// private boolean preventPrizeCollection(){ return  playerIsHungry(); }

	// Check if the player has collected a certain amount of prizes
	private void checkGameOver() {
		if(avatarCollision(x) && avatarCollision(y) && avatarCollision(z)) { 
			System.out.println("protclient: " + getProtClient() + "\n\n");
			getProtClient().sendGameWinner(plyr.getWorldLocation());
			winnerUUID = getProtClient().getID().toString();
			gameOver = true;
		}
		
		if(gameOver) { winningShutdown();}  // show winning HUD if player wins
	}

	public void setGameOver() { gameOver = true;}
	public void setGameWinnerUUIDStr(String id) {winnerUUID = id; }

	// If the user collects enough prizes, they win (take a look at num)
	private void winningShutdown() {

		System.out.println("winner id: " + winnerUUID);
		System.out.println("your id: " + getProtClient().getID().toString() + "\n\n\n");
		System.out.println("Did I win??: " + getProtClient().getID().toString().equals(winnerUUID) + "\n\n\n");
		if(gameOver && getProtClient().getID().toString().equals(winnerUUID)) {
			String dispStr = "You are the winner!\n";
			Vector3f hudColor = new Vector3f(0,1,0);
			hudManager.setHUD4(dispStr, hudColor, 500, 500);
			shutdown();
			return;
		} else if (gameOver) {
			String dispStr = "You are the loser ...!\n";
			Vector3f hudColor = new Vector3f(1,0,0);
			hudManager.setHUD4(dispStr, hudColor, 500, 500);
			shutdown();
			return;
		}
	}
	
	private boolean suspendGame() { return isPaused() || isGameOver(); }

	private boolean isPaused() { return paused; }

	private boolean isGameOver() { return gameOver; }

	public InputManager getEngineInputManager() { return engineIM; }
	
	// Get elapsed time
	public float getElapsedTime() { return (float)elapsTime; }
	
	// get an Elapsed Speed for movement
	public float getElapsedSpeed() { return (float)elapsTime / 50f; }

	// Check if the game is suspended
	public boolean isSuspended() {return isPaused() || isGameOver(); }

	// ************************ Network methods *********************************
	public ObjShape getGhostShape() {
		return networkClient.getGhostShape();
	}
	
	public TextureImage getGhostTexture() {
		return networkClient.getGhostTexture();
	}

	public GhostManager getGhostManager() {
		return networkClient.getGhostManager();
	}

	public ObjShape getNPCshape() {
		return networkClient.getNPCshape();
	}

	public TextureImage getNPCtexture() {
		return networkClient.getNPCtexture();
	}

	public Engine getEngine() { return engine; }
	
	protected void processNetworking(float elapsTime) { networkClient.runProcessNetworking(elapsTime); }

	public ProtocolClient getProtClient() { return networkClient.getProtClient(); }

	public GameObject getPlayer() { return plyr; }

	public Vector3f getPlayerPosition() { return plyr.getWorldLocation(); }

	public boolean avatarCollision(GameObject go)
	{
		return Math.abs(plyr.getLocalLocation().distance(go.getWorldLocation().x(), go.getWorldLocation().y(), 
		go.getWorldLocation().z())) < 1.25;
	}

	public void setIsConnected(boolean value) { networkClient.setIsConnected(value); }

	@Override
	public void keyPressed(KeyEvent e){
		switch (e.getKeyCode()){
			case KeyEvent.VK_W:{
				animationFWDToggle(true);
				walkSound(true);
				direction = 1;
				break;
			}
			case KeyEvent.VK_S:{
				animationBWDToggle(true);
				walkSound(true);
				direction = 2;
				break;
			}
			case KeyEvent.VK_K:{
				running = true;
				break;
			}
			case KeyEvent.VK_L:{
				running = false;
				break;
			}
			case KeyEvent.VK_E:{
				lightOff = !lightOff;
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:{
				animationFWDToggle(false);
				walkSound(false);
				direction = 0;
				break;
			}
			case KeyEvent.VK_S:{
				animationBWDToggle(false);
				walkSound(false);
				direction = 0;
				break;
			}
		}
	}
}