package tage.nodeControllers;
import tage.*;
import org.joml.*;

import a3.FoodStation;

/**
* A RotationController is a node controller that, when enabled, causes any object
* it is attached to to rotate in place around the tilt axis specified.
* @author Scott Gordon
*/
public class BounceController extends NodeController { 
    private Engine engine; 
    private float cycleTime;
    private float translationRate; 
    private float direction;
    private Matrix4f curTranslation, newTranslation; 

    public BounceController(Engine e, float ctime) { 
        super(); 
        engine = e; 
        cycleTime = ctime; 
        translationRate = .003f; 
        direction = 1.0f; 
        newTranslation = new Matrix4f(); 
    } 
    public void apply(GameObject go) { 
        float elapsedTime = super.getElapsedTime(); 
        
        FoodStation fs = (FoodStation) go;
        if (fs.getBounceTimer() > cycleTime) { return; }

        direction = fs.getBounceTimer() <= 2.0f ? 1 : -1;

        fs.setBounceTimer(fs.getBounceTimer() + elapsedTime/1000.0f);
        
        float translationAmt = direction * translationRate * elapsedTime; 

        Vector3f oldPosition, fwdDirection, newLocation;
		oldPosition = go.getWorldLocation(); 
		fwdDirection = go.getWorldUpVector(); // N vector 
		newLocation = oldPosition.add(fwdDirection.mul(translationAmt)); 

		// prevents gameobject from moving below the plane
		if(newLocation.y() < 3.3f) { 
			newLocation.set(newLocation.x(), 3.3f, newLocation.z());
		}
		go.setLocalLocation(newLocation); 
    } 
}
