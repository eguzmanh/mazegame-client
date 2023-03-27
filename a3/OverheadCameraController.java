package a3;

import tage.*;
import tage.input.InputManager;
import tage.shapes.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math; 

/**
 * OverheadCameraController contains the Actions for the overhead camera
 *  - The current actions allow zooming and panning of the overhead camera
 */
public class OverheadCameraController  { 
    private Engine engine; 
    private Camera camera; // the camera being controlled 
    private GameObject avatar; // the target avatar the camera looks at 
    private float cameraAzimuth; // rotation around target Y axis 
    private float cameraElevation; // elevation of camera above target 
    private float cameraRadius; // distance between camera and target 
    

    public OverheadCameraController(Camera cam, GameObject av, Engine e) { 
        engine = e; 
        camera = cam; 
        avatar = av; 
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target 
        cameraElevation = 50.0f; // elevation is in degrees 
        cameraRadius = 20.0f; // distance from camera to avatar 
        setupInputs(); 
        updateCameraPosition( 0.0f, 0.0f, -40.0f); 
        // camera.lookAt(avatar);  
    } 
    
    private void setupInputs() { 
        OverheadPanAction panAction = new OverheadPanAction(); 
        OverheadZoomAction zoomAction = new OverheadZoomAction(); 

        InputManager im = engine.getInputManager(); 

        // pan movements
        im.associateActionWithAllKeyboards( 
            net.java.games.input.Component.Identifier.Key.NUMPAD8, panAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.NUMPAD6, panAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.NUMPAD4, panAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards( 
            net.java.games.input.Component.Identifier.Key.NUMPAD2, panAction, 
            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.POV, panAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
        // Zoom in/out
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.NUMPAD9, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.NUMPAD7, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Z, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    } 

    public void updateCameraPosition(float uVal, float vVal, float nVal) {  
        Vector3f cameraLocation = camera.getLocation(); 
		Vector3f cameraU = camera.getU(); 
		Vector3f cameraV = camera.getV(); 
		Vector3f cameraN = camera.getN(); 

        cameraU.mul(uVal);
        cameraV.mul(vVal);
        cameraN.mul(nVal);

		cameraLocation.add(cameraV).add(cameraN).add(cameraU);
		camera.setLocation(cameraLocation);
    }

    private class OverheadPanAction extends AbstractInputAction  { 
        public void performAction(float time, Event event) { 
            float deadzone = 0.2f;
            String evtName = event.getComponent().getName();
            float evtValue = event.getValue();

            if (evtValue > -deadzone && evtValue < deadzone) return;

            if (evtName.equals("Num 8")     || (evtName.equals("Hat Switch") && evtValue == 0.25f)) updateCameraPosition(0.0f, 1.0f, 0.0f);
            else if(evtName.equals("Num 6") || (evtName.equals("Hat Switch") && evtValue == 0.5f)) updateCameraPosition(1.0f, 0.0f, 0.0f);
            else if(evtName.equals("Num 4") || (evtName.equals("Hat Switch") && evtValue == 1.0f)) updateCameraPosition(-1.0f, 0.0f, 0.0f);
            else if(evtName.equals("Num 2") || (evtName.equals("Hat Switch") && evtValue == 0.75f)) updateCameraPosition(0.0f, -1.0f, 0.0f);
        } 
    } 

    private class OverheadZoomAction extends AbstractInputAction  { 
        public void performAction(float time, Event event) {      
            float deadzone = 0.2f;
            String evtName = event.getComponent().getName();
            float evtValue = event.getValue();
            
            if (evtValue > -deadzone && evtValue < deadzone) return;
            // System.out.println("HMM: " + evtName);
            if(      evtName.equals("Num 7") || (evtName.equals("Z Axis") && evtValue > deadzone)) updateCameraPosition(0.0f, 0.0f, -1.0f);
            else if (camera.getLocation().y() <= 2.0f) return; // don't allow zooming in if y is negative
            else if ( evtName.equals("Num 9") || (evtName.equals("Z Axis") && evtValue < -deadzone) ) updateCameraPosition(0.0f, 0.0f, 1.0f); 
        } 
    }
}
