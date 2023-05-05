package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import net.java.games.input.Event;
import tage.*;
import org.joml.*;


// Class to deal with toggling the pause mode of the game
public class PauseAction extends AbstractInputAction {
    private MazeGame game;

    public PauseAction(MazeGame g) {
        game = g;
    }

    public void associateDeviceInputs() {
        // Pause action
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.P, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		 game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._7, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }

    @Override
    public void performAction(float time, Event evt) {
        // if (evt.getValue() > -.2f && evt.getValue() < .2f) return;
        game.pauseAction();
    }
}