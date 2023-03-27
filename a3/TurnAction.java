package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;

// Class to deal with yawing for the camera and the dolphin
// This class will allow different devices to use it
public class TurnAction extends AbstractInputAction {
    private MyGame game;

    public TurnAction(MyGame g){
        game = g;
    }
    
    public void associateDeviceInputs() {
        // Associate Yawing  Motion (left/right)
		game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.A, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.D, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Axis.X, this, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 

    }
    @Override
    public void performAction(float time, Event evt) {
        if(game.isSuspended()) return;

        float deadzone = 0.2f;
        String evtName = evt.getComponent().getName();
        float evtValue = evt.getValue();

        // return if deadzone
        if (evtValue > -deadzone&& evtValue < deadzone) return;

        setSpeed(game.getElapsedSpeed());
        float newSpeed = getSpeed();
        
        if(evtName.equals("A") || evtValue < -deadzone) {
            System.out.println("Moving Left");
            newSpeed *= 1;
                
        } else if(evtName.equals("B") || evtValue > deadzone) {
            System.out.println("Moving Right");
            newSpeed *= -1;
        }
        game.turnAction(newSpeed/10.0f);
    }
}
