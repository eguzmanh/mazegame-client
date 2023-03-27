package a3;

import tage.*;
import tage.input.InputManager;
import tage.nodeControllers.*;
import tage.shapes.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import net.java.games.input.Component.Identifier.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private Camera engineCamera, overheadEngineCamera;
	private OverheadCameraController ohCameraController;
	private InputManager engineIM; 
	private HUDmanager hudManager;
	private Viewport engineCameraV, engineOHCameraV;

	private NodeController rc, bc;

	private int prizeCounter, prizeCounterWin, numPrizes, numFoodStations;
	private float timer, foodLevel, foodLevelHungerThreshold, gameworldEdgeBound, minGameObjectYLoc;
	private boolean isMounted, paused, isInDolphinBounds, gameOver;
	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject dol, x, y, z, groundPlane, foodTorus;
	private ObjShape dolS, linxS, linyS, linzS, prizeS, foodStationS, groundPlaneS, foodTorusS;
	private TextureImage doltx, fstx;

	private Light light1;

	private CameraOrbit3D orbit3DController;
	
	private ArrayList<TextureImage> customTextures;
	private ArrayList<Prize> prizes;
	private ArrayList<FoodStation> foodStations;


	private float foodTorusAzimuth, // start BEHIND and ABOVE the target 
				foodTorusElevation, // elevation is in degrees 
				foodTorusRadius; // distance from camera to avatar
	private float foodStorageBuf;
	private boolean foodStorageEmpty;
	

	public MyGame() { 
		super();
		initGameVariables();
		customTextures = new ArrayList<TextureImage>();  // allows prizes to get a random texture from the options available
		prizes = new ArrayList<Prize>();  // allows dynamic number of prizes
		foodStations = new ArrayList<FoodStation>(); // allows dynamic number of food stations
	}

	public static void main(String[] args) {	
		MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	/******************************************************
	 * 	  * VariableFrameRateGame function overrides
	******************************************************/
	@Override
	public void loadShapes() {
		dolS = new ImportedModel("dolphinHighPoly.obj");

		
		linxS = new Line(new Vector3f(-gameworldEdgeBound,0f,0f), new Vector3f(gameworldEdgeBound,0f,0f)); 
		linyS = new Line(new Vector3f(0f,-gameworldEdgeBound,0f), new Vector3f(0f,gameworldEdgeBound,0f)); 
		linzS = new Line(new Vector3f(0f,0f,-gameworldEdgeBound), new Vector3f(0f,0f,gameworldEdgeBound));

		prizeS = new Sphere();
		
		foodStationS = new ManualFoodStation();

		groundPlaneS = new Plane();

		foodTorusS = new Torus(1.0f, 1.0f,48);

		// ((Plane)groundPlaneS).setPlaneSize(new Vector3f(0f,0f,-1000f), new Vector3f(0f,0f,1000f));
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		fstx = new TextureImage("Drawer_Door.jpg");
		customTextures.add(new TextureImage("Wood_Desk.png"));
		customTextures.add(new TextureImage("Floral_Sheet.png"));
	}

	@Override
	public void buildObjects() {	
		buildWorldAxisLines();
		buildGroundPlane();
		buildDolphin();
		buildPrizes();
		buildFoodStations();	
		buildFoodTorus();
	}

	@Override
	public void initializeLights() {	
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		// System.out.println("Light type: " + light1.getLightType());
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
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
		initMainInputManagerActions();  // handle the Input Manager detection
		foodTorusAzimuth = 0.0f;
		foodTorusElevation = 0.0f;
		foodTorusRadius = 4.0f;
	}

	@Override
	public void update() {	// rotate dolphin if not paused

		// Update elapsed time regardless of the game status
		upateElapsedTimeInfo(); // elapsed time for only the current render and previous render
		 
		checkGameOver();

		if (suspendGame()) return;
		
		// Orbit controller for the dolphin
		orbit3DController.updateCameraPosition();
		
		validatePrizeCollisions();
		validateFoodStationCollisions();
		
		decreaseFoodLevel(); // ensures that player eata from food stations in order to win
		
		rotateTorusIfFoodAvailable();

		// build and set HUD
		buildHUDs();


	}
	
	/******************************************************
	 * 				* Helper functions for setup
	******************************************************/


	// A lot of this data can be used in the scripts when implemented
	private void initGameVariables() {
		isMounted = false;
		paused = false;
		isInDolphinBounds = true;
		gameOver = false;
		elapsTime = 0.0f;
		timer = 0.0f;
		numPrizes = 200;
		prizeCounter = 0;
		prizeCounterWin = 30;
		numFoodStations = 30;
		foodStorageBuf = 0.0f;
		foodStorageEmpty = true;
		foodLevel = 200f;
		foodLevelHungerThreshold  = 50.0f;
		gameworldEdgeBound = 10000f;
		minGameObjectYLoc = 1.2f;

		initTimeFrames();
	}

	/**
	 * Helper functions used to build the intial 3D World Objects
	 */
	private void buildDolphin() {
		Matrix4f initialTranslation, initialScale, initialRotation;
	
		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f,minGameObjectYLoc,1f); 
		initialScale = (new Matrix4f()).scaling(3.0f);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f)); 
		
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalRotation(initialRotation); 
		dol.setLocalScale(initialScale);
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
		groundPlane = new GameObject(GameObject.root(), groundPlaneS);
	
		// Not ouputting the correct color for some reason!!!
		(groundPlane.getRenderStates()).setColor(new Vector3f(0f, 0.5608f, 1f)); 

		Matrix4f initialScale = (new Matrix4f()).scaling(gameworldEdgeBound);
		Matrix4f initialTranslation = (new Matrix4f()).translation(0f, 0f, 0f);

		groundPlane.setLocalScale(initialScale);
		groundPlane.setLocalTranslation(initialTranslation);
	}

	/**
	 * This function uses an int to determine the number of prizes to load in the game
	 * The prizes will render with a random translation, rotation, and scale 
	*/
	private void buildPrizes() {	

		for(int i = 0; i < numPrizes; i++) {
			System.out.println("Loading prize: " + i);
			Prize tempO = new Prize(GameObject.root(), prizeS, getRandomCustomTexture());
			setObjectTRS(tempO, 3f);
		}
	}
	
	/**
	 * This function uses an int to determine the number of foodstations to load in the game
	 * The food stations will render with a random translation, rotation, and scale 
	*/
	private void buildFoodStations() {	
		for(int i = 0; i < numFoodStations; i++) {
			System.out.println("Loadin food station: " + i);
			FoodStation fsO = new FoodStation(GameObject.root(), foodStationS, fstx);
			setObjectTRS(fsO, 3.5f);
		}
	}

	private void buildFoodTorus() {
		// Adding Hierarchy
		Matrix4f initialTranslation, initialScale, initialRotation;
		foodTorus = new GameObject(GameObject.root(), foodTorusS);
		// initialTranslation = (new Matrix4f()).translation(-3,0,0);
		initialScale = (new Matrix4f()).scaling(0.07f);
		// foodTorus.setLocalTranslation(initialTranslation); 
		foodTorus.setLocalScale(initialScale);
		foodTorus.setParent(dol); 
		foodTorus.propagateTranslation(false); 
		foodTorus.propagateRotation(false); 
		foodTorus.applyParentRotationToPosition(false); 
		foodTorus.getRenderStates().setTiling(1);
		
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

		ohCameraController = new OverheadCameraController(overheadEngineCamera, dol, engine);
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
		rc = new RotationController(engine, new Vector3f(0,1,0), 0.001f);
		rc.enable(); // turn on the rotation controller

		bc = new BounceController(engine, 4f);
		bc.enable(); // turn on the rotation controller

		(engine.getSceneGraph()).addNodeController(rc); 
		(engine.getSceneGraph()).addNodeController(bc); 
	}
	private void init3DOrbitController() {
		// ------------- setup 3d Orbit Controller for the main camera -------------
		// adjustCameraView(-1.85f, 0.6f, -0.75f);
		orbit3DController = new CameraOrbit3D(engineCamera, dol, engine);
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
		EatAction eatActionCmd = new EatAction(this);

		shutdownActionCmd.associateDeviceInputs();
		screenModeToggleActionCmd.associateDeviceInputs();
		axisLinesActionCmd.associateDeviceInputs();
		pauseActionCmd.associateDeviceInputs();
		wireframeActionCmd.associateDeviceInputs();
		// spacebarAction.associateDeviceInputs();
		fwdbwdActionCmd.associateDeviceInputs();
		turnActionCmd.associateDeviceInputs();
		// pitchActionCmd.associateDeviceInputs();
		eatActionCmd.associateDeviceInputs();
	}

	
	/***************************************************************************
	 * Action functions for working linking InputManager with  hw devices
	****************************************************************************/
	/** 
	 * These public methods will allow the Actions to be called to be called while encapsulating the implementation
	 */

	public void shutdownAction() { _shutdownAction(); } 

	public void screenModeToggleAction() { _screenModeToggleAction(); }  

	public void wireframeAction() { _wireframeAction(); }  

	public void pauseAction() { _pauseAction(); }

	public void turnAction(float newSpeed) { _turnAction(newSpeed); }
	
	public void pitchAction(float newSpeed) { _pitchAction(newSpeed); }
	
	public void fwdBwdAction(float newSpeed){ _fwdBwdAction(newSpeed); }
	
	public void axisLinesAction(boolean axisLinesEnabled) { _axisLinesAction(axisLinesEnabled); }
	
	public void eatAction() { _eatAction(); }


	private void _shutdownAction() {
		shutdown();
		System.exit(0);
	}

	private void _screenModeToggleAction() {
		(engine.getRenderSystem()).toggleFullScreenMode();
	}

	private void _wireframeAction() {
		if (dol.getRenderStates().isWireframe()) {
			dol.getRenderStates().setWireframe(false);
		} else {
			dol.getRenderStates().setWireframe(true);
		}
		
	}
	
	private void _pauseAction() {
		System.out.println("Game is paused. . .");
		paused = paused ? false : true;
	}

	private void _turnAction(float newSpeed){
		Matrix4f worldYM = y.getWorldRotation();
		dol.yaw(newSpeed, worldYM);
	}

	private void _pitchAction(float newSpeed) {
		dol.pitch(newSpeed);
	}

	private void _fwdBwdAction(float newSpeed){
		Vector3f oldPosition, fwdDirection, newLocation;

		oldPosition = dol.getWorldLocation(); 
		fwdDirection = dol.getWorldForwardVector(); // N vector 
		newLocation = oldPosition.add(fwdDirection.mul(newSpeed)); 

		// prevents dolphin from moving below the plane
		if(newLocation.y() <= minGameObjectYLoc) { 
			newLocation.set(newLocation.x(), minGameObjectYLoc, newLocation.z());
		}
		dol.setLocalLocation(newLocation); 
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

	private void _eatAction() {
		if (foodStorageBuf > 0.0f) {
			foodLevel += foodStorageBuf;
			foodStorageBuf = 0.0f;
			foodStorageEmpty = true;
		}
	}
	/******************************************************
	 * 		* Helper functions for the update steps
	******************************************************/
	
	private void rotateTorusIfFoodAvailable() {
		if(foodStorageEmpty) {
			if ((foodTorus.getRenderStates()).isEnabled()) { (foodTorus.getRenderStates()).disableRendering(); } 
			return;
		}

		if (!(foodTorus.getRenderStates()).isEnabled()) { (foodTorus.getRenderStates()).enableRendering(); }

		float rotAmount = 10f;
		foodTorusAzimuth += rotAmount; 
		foodTorusAzimuth = foodTorusAzimuth % 360; 

		double theta = Math.toRadians(foodTorusAzimuth); 
		double phi = Math.toRadians(foodTorusElevation); 

		float x = foodTorusRadius * (float)(Math.cos(phi) * Math.sin(theta)); 
		float y = foodTorusRadius * (float)(Math.sin(phi)); 
		float z = foodTorusRadius * (float)(Math.cos(phi) * Math.cos(theta)); 

		foodTorus.setLocalLocation(new Vector3f(x,y,z).add(dol.getWorldLocation())); 
		
	}
	
	/**
	 * Ensures that prizes cannot be collected during a collision if certain criteria is not met
	 * Criteria written in preventPrizeCollection()
	 * If a successful collision occurs, the prizeCounter is incremented 
	 * The comparison is done by checking the distance between the camera and the prize object
	 */
	private void validatePrizeCollisions() {
		if (preventPrizeCollection()) return;
		Vector3f dolLoc = dol.getLocalLocation();
		for (Prize go : prizes) {
			if (go.isEnabled() && dolLoc.distance(go.getLocalLocation()) < 4.5f) {
				System.out.println("Collision detected!!");
				go.disable();
				rc.addTarget(go);
				prizeCounter++;
			}
		}
	}

	/**
	 * Iterates through the Food Station Array List and will go through collision detection
	 * A player must be OFF the dolphin to collect the food source
	 */
	private void validateFoodStationCollisions() {
		// if (isMounted) return;
		Vector3f dolLoc = dol.getLocalLocation();
		for (FoodStation go : foodStations) {
			if (go.isEnabled() && dolLoc.distance(go.getLocalLocation()) < 4.5f) {
				System.out.println("Collision detected!!");
				bc.addTarget(go);
				rc.addTarget(go);

				go.disable();
				foodStorageBuf += go.getProportion();
				if(foodStorageEmpty) foodStorageEmpty = false;
			}
		}
	}

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
		String prizeCounterStr = Integer.toString(prizeCounter);
		String foodLevelStr = Integer.toString(Math.round(foodLevel));

		String dispStr1 = "Prizes Collected = " + prizeCounterStr;
		String dispStr2 = "Food Level = " + foodLevelStr;

		String ds3X = String.format(" {X: %.2f}", dol.getWorldLocation().x());
		String ds3Y = String.format(" {Y: %.2f}", dol.getWorldLocation().y());
		String ds3Z = String.format(" {Z: %.2f}", dol.getWorldLocation().z());
		String dispStr3 = "Dolphin:" + ds3X + ds3Y + ds3Z;

		Vector3f hud1Color = new Vector3f(1,1,1);
		Vector3f hud2Color = new Vector3f(1,1,1);
		Vector3f hud3Color = new Vector3f(1,1,1);

		// Set the color of the displays
		// Change the HUD's in case the window size becomes too small
		if(hudManager.isCanvasTabletMode()) {
			hudManager.setHUD1(dispStr1, hud1Color, 60, 60);
			hudManager.setHUD2(dispStr2, hud2Color, 60, 15);
		} else {
			hudManager.setHUD1(dispStr1, hud1Color, 15, 15);
			hudManager.setHUD2(dispStr2, hud2Color, 300, 15);
		}

		hudManager.setHUD3(dispStr3, hud3Color, (int)engineOHCameraV.getLeftOnCanvas() + 15, 15);
	}
	

	/**
	 * Checks that the player's food level is not below a certain amount
	 * @return
	 */
	private boolean playerIsHungry() { return foodLevel <= foodLevelHungerThreshold; }

	/**
	 * This function will contain the tests that the function must pass in order to allow the collection of
	 * a prize during a collision.
	 * @return
	 */
	private boolean preventPrizeCollection(){ return  playerIsHungry(); }

	// Check if the player has collected a certain amount of prizes
	private void checkGameOver() {
		if(prizeCounter == prizeCounterWin) { gameOver = true;}
		if(gameOver) { winningShutdown();}  // show winning HUD if player wins
	}

	// If the user collects enough prizes, they win (take a look at num)
	private void winningShutdown() {
		if(gameOver) {
			String dispStr = "You are the winner!\nYou collected: " + prizeCounter;
			Vector3f hudColor = new Vector3f(0,1,0);
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

}