package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import net.java.games.input.Event;
import tage.*;
import org.joml.*;

public class WireframeAction extends AbstractInputAction {
    private MazeGame game;
    // private GameObject dolphin;

    public WireframeAction(MazeGame g) {
        game = g;
    }

    public void associateDeviceInputs() {
        // Associate Forward/Backward motion
		 game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key._1, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._3, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }
    @Override
    public void performAction(float time, Event evt) {
        // String evtName = evt.getComponent().getName();

        // if(game.isSuspended()) return;
        // System.out.println("numpad number: " + evt.getComponent().getName());

        // if(evtName == "1") {
            // System.out.println("key 2 pressed");
            game.wireframeAction();
        // } 
        // if(eevtName == "2") {
            // System.out.println("numpad3 pressed");
            // game.wireframeAction();
        // }

        // game.wireframeAction(isFrameActive);
    }
}   
