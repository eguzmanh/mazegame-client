package a3;

import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import net.java.games.input.Event;
import tage.*;
import org.joml.*;

public class AxisLinesAction extends AbstractInputAction {
    private MyGame game;
    private boolean linesEnabled;

    public AxisLinesAction(MyGame g) {
        game = g;
        linesEnabled = true;
    }

    public void associateDeviceInputs() {
        // toggle Action lines
        game.getEngineInputManager().associateActionWithAllKeyboards( 
			net.java.games.input.Component.Identifier.Key.T, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		game.getEngineInputManager().associateActionWithAllGamepads( 
			net.java.games.input.Component.Identifier.Button._2, this, 
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }

    @Override
    public void performAction(float time, Event evt) {
        // if(linesEnabled)
        // linesEnabled = !linesEnabled;
        linesEnabled = linesEnabled ?  false : true;
        game.axisLinesAction(linesEnabled);
    }
}
