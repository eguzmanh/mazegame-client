package tage;

import tage.input.InputManager;
import tage.shapes.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math; 

public class CameraOrbit3D  { 
    private Engine engine; 
    private Camera camera; // the camera being controlled 
    private GameObject avatar; // the target avatar the camera looks at 
    private float cameraAzimuth; // rotation around target Y axis 
    private float cameraElevation; // elevation of camera above target 
    private float cameraRadius; // distance between camera and target 
    public CameraOrbit3D(Camera cam, GameObject av, Engine e) { 
        engine = e; 
        camera = cam; 
        avatar = av; 
        cameraAzimuth = -35.0f; // start BEHIND and ABOVE the target 
        cameraElevation = 15.0f; // elevation is in degrees 
        cameraRadius = 10.0f; // distance from camera to avatar 
        setupInputs();
        updateCameraPosition(); 
    } 
    private void setupInputs() { 
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction(); 
        OrbitElevationAction elevAction = new OrbitElevationAction(); 
        OrbitZoomAction zoomAction = new OrbitZoomAction(); 

        InputManager im = engine.getInputManager(); 

        // * Orbit left/right
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.LEFT, azmAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.RIGHT, azmAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.RX, azmAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
        // im.associateActionWithAllGamepads(
        //     net.java.games.input.Component.Identifier.Axis.RX, azmAction, 
        //     InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // * Orbit up/down
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.UP, elevAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.DOWN, elevAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllGamepads( 
                net.java.games.input.Component.Identifier.Axis.RY, elevAction, 
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 

        // * Zoom in/out
        im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.I, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.O, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._8, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._9, zoomAction, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    } 

    // Compute the camera’s azimuth, elevation, and distance, relative to 
    // the target in spherical coordinates, then convert to world Cartesian  
    // coordinates and set the camera position from that. 
    public void updateCameraPosition() { 
        Vector3f avatarRot = avatar.getWorldForwardVector(); 
        double avatarAngle = Math.toDegrees((double) avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0))); 
        float totalAz = cameraAzimuth - (float)avatarAngle;
        double theta = Math.toRadians(cameraAzimuth); 
        double phi = Math.toRadians(cameraElevation); 
        float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta)); 
        float y = cameraRadius * (float)(Math.sin(phi)); 
        float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta)); 
        camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation())); 
        camera.lookAt(avatar);  
    }

    private class OrbitAzimuthAction extends AbstractInputAction  { 
        public void performAction(float time, Event event) { 
            float deadzone = 0.2f;
            String evtName = event.getComponent().getName();
            float evtValue = event.getValue();
            
            if (evtValue > -deadzone && evtValue < deadzone) return;

            // System.out.println("Event Value: " + event.getComponent().toString());
            // String keyStr = event.getComponent().getName();
            float rotAmount; 
            if (evtValue < -deadzone || evtName == "Left") { 
                rotAmount=0.8f;  
            } else if (evtValue > deadzone || evtName == "Right") { 
                rotAmount=-0.8f;  
            } else { 
                rotAmount=0.0f;  
            }  

            cameraAzimuth += rotAmount; 
            cameraAzimuth = cameraAzimuth % 360; 
            updateCameraPosition(); 
        } 
    } 

    private class OrbitElevationAction extends AbstractInputAction  { 
        public void performAction(float time, Event event) { 
            float deadzone = 0.2f;
            String evtName = event.getComponent().getName();
            float evtValue = event.getValue();
        
            if (evtValue > -deadzone && evtValue < deadzone) return;
            
            float rotAmount; 
            if (evtValue < -deadzone || evtName == "Down") { 
                rotAmount=-0.8f;  
            } else if (evtValue > deadzone || evtName == "Up") { 
                rotAmount=0.8f;  
            } else { 
                rotAmount=0.0f;  
            }  

            System.out.println("Camera Elevation: " + cameraElevation);

            cameraElevation += rotAmount; 

            cameraElevation = cameraElevation % 180; 

            // ensures that camera view stays above the ground plane
            if (cameraElevation < -3.0f) cameraElevation = 180f;
        

            updateCameraPosition(); 
        } 
    } 

    private class OrbitZoomAction extends AbstractInputAction  { 
        public void performAction(float time, Event event) { 
            // float deadzone = 0.2f;
            String evtName = event.getComponent().getName();
            float evtValue = event.getValue();
            float rotAmount; 

            if (evtName.equals("O") || evtName.equals("Button 8")) { rotAmount = 0.8f; }
            else if ((evtName.equals("I")  || evtName.equals("Button 9")) && camera.getLocation().y() > 2.0f) rotAmount = -0.8f; 
            else rotAmount = 0.0f;

            // if (camera.getLocation().y() <= 2.0f) rotAmount = 0.0f; // don't allow zooming in if y is negative
            //     rotAmount=0.0f;  
            // }  

            cameraRadius += rotAmount; 
            cameraRadius = cameraRadius % 360; 
            updateCameraPosition(); 
        } 
    } 
}
