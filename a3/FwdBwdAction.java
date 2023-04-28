package a3;

// import net.java.games.input.Event;
// import org.joml.Vector3f;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;

import net.java.games.input.Event;
import tage.*;
import org.joml.*;

// Class to deal with moving forward and backward for the camera and the dolphin
// This class will allow different devices to use it
public class FwdBwdAction extends AbstractInputAction {
    private MazeGame game;
    // private ProtocolClient protClient;

    public FwdBwdAction(MazeGame g){
        game = g;
        // protClient = p;
    }

    public void associateDeviceInputs() {
        // Associate Forward/Backward motion
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.W, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.S, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.Y, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
    }

    @Override
    public void performAction(float time, Event evt) {
        // return if deadzone
        if(game.isSuspended()) return;
        
        float deadzone = 0.3f;
        String evtName = evt.getComponent().getName();
        float evtValue = evt.getValue(); 
        if (evtValue > -deadzone && evtValue < deadzone) return;

        setSpeed(game.getElapsedSpeed());
        float newSpeed = getSpeed();
        if(evtName.equals("W") || evtValue < -deadzone) {
            newSpeed *= 1;
            System.out.println("Moving Forward");
            game.animationToggle();
        } 
        
        else if(evtName.equals("S") || evtValue > deadzone) {
            System.out.println("Moving Bacward");
            newSpeed *= -1;
        }

        game.fwdBwdAction(newSpeed * 2);
        System.out.println(game.getProtClient());
        System.out.println(game.getPlayerPosition());
        if(game.getProtClient() != null) {
            System.out.println("This should be running.");
            game.getProtClient().sendMoveMessage(game.getPlayerPosition());
        }
    }
}





