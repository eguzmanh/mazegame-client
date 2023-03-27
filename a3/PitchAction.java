package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;

import net.java.games.input.Event;
import tage.*;
import org.joml.*;


// Class to deal with pitching for the camera and the dolphin
// This class will allow different devices to use it
public class PitchAction extends AbstractInputAction {
    private MyGame game;
    // private GameObject dolphin;

    public PitchAction(MyGame g) {
        game = g;
    }

    public void associateDeviceInputs() {
        // Associate Pitch  Motion (Up and down)
		game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.J, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.K, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._4, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
        game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._5, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 
    }

    @Override
    public void performAction(float time, Event evt) {
        if(game.isSuspended()) return;

        // float deadzone = 0.2f;
        String evtName = evt.getComponent().getName();
        float evtValue = evt.getValue(); 
        
        // return if deadzone
        // if (evtValue > -deadzone && evtValue < deadzone) return;
        
        setSpeed(game.getElapsedSpeed());
        // System.out.println("Evt for pitch: " + evtName + "---tessss");
        // System.out.println("Evt str for pitch: " + evt.getComponent().toString() + "----tessss");

        // if(evtName.equals("Button 4")) {System.out.println("This should be outputting...");}
        float newSpeed = getSpeed();
        if(evtName.equals("K") || evtName.equals("Button 4")) {
            System.out.println("Pitching up");
            newSpeed *= 1;
        } else if(evtName.equals("J") || evtName.equals("Button 5")) {
            System.out.println("Pitching down");
            newSpeed *= -1;
        }

        game.pitchAction(newSpeed/8.0f);
    }
}