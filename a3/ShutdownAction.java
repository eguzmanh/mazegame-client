package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import net.java.games.input.Event;
import tage.*;
import org.joml.*;


// Class to deal with toggling riding the dolphin
public class ShutdownAction extends AbstractInputAction {
    private MyGame game;

    public ShutdownAction(MyGame g) {
        game = g;
    }

    public void associateDeviceInputs() {
        // Spacebar action
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.ESCAPE, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }
    @Override
    public void performAction(float time, Event evt) {
        // return if deadzone
        // if (evt.getValue() > -.2f && evt.getValue() < .2f) return;
        // if(game.isSuspended()) return;
        game.shutdownAction();
    }
}