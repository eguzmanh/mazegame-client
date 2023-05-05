package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;

import net.java.games.input.Event;
import tage.*;
import org.joml.*;

public class JumpAction extends AbstractInputAction {
    private MazeGame game;

    public JumpAction(MazeGame g) {
        game = g;
    }

    public void associateDeviceInputs() {
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.SPACE, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	// 	 game.getEngineInputManager().associateActionWithAllGamepads( 
	// 		net.java.games.input.Component.Identifier.Button._7, this, 
	// 		InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }

    public void performAction(float time, Event e){
        game.jumpAction();
    }

}
