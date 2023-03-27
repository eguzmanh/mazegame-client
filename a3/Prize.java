package a3;

import tage.*;
import tage.shapes.*;

// import org.joml.*;
// import java.util.*;

/**
 *  Class created so a game object can keep track of its scale multiplier
 *  Also keeps a flag to disable any actions when the object is no longer in the scene graph
 */
public class Prize extends  GameObject {
    private float scale;
    private boolean enabled;

    public Prize(GameObject p, ObjShape s, TextureImage t) {
        super(p, s, t);
        scale = 1.0f; // default scale is for it to not change
        enabled = true;
    }
    public Prize(GameObject p, ObjShape s, TextureImage t, float newScale) {
        super(p, s, t);
        scale = newScale;
        enabled = true;
    }
    public Prize(GameObject p, ObjShape s, float newScale) {
        super(p, s);
        scale = newScale;
        enabled = true;
    }


    // Getters
    public float getScale() {
        return scale;
    }
    public boolean isEnabled() {
        return enabled;
    }
    
    // Setters
    public void setScale(float newS) {
        scale = newS;
    }
    public void multScale(float newS) {
        scale *= newS;
    }
    public void enable() {
        enabled = true;
    }
    public void disable() {
        enabled = false;
    }
}   
