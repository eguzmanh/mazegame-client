package a3;

import tage.*;
import tage.shapes.*;

/**
 * The FoodStation class is dedicated to allow eating in the game
 * The player will need to have at least a food level of 39 to keep collecting prizes
 */
public class FoodStation extends GameObject {
    private float scale;
    private boolean enabled;
    private float proportion;
    private float bounceTimer;

    public FoodStation(GameObject p, ObjShape s, TextureImage t) {
        super(p, s, t);
        scale = 1.0f;
        enabled = true;
        proportion = 30.0f;
        bounceTimer = 0.0f;
    }
    // If you want to add custom params, you must init all of the changing data
    // In this case, we must manually set the newScale and the proportion
    public FoodStation(GameObject p, ObjShape s, TextureImage t, float newScale, float prop) {
        super(p, s, t);
        scale = newScale;
        enabled = true;
        proportion = prop;
        bounceTimer = 0.0f;
    }
    public FoodStation(GameObject p, ObjShape s, float newScale, float prop) {
        super(p, s);
        scale = newScale;
        enabled = true;
        proportion = prop;
        bounceTimer = 0.0f;
    }

    /**
     * Getters
     */

    public float getBounceTimer() {
        return bounceTimer;
    }
    public float getScale() {
        return scale;
    }
    public float getProportion() {
        return proportion;
    }
    
    /**
     * Set a custom scalar as the new scale factor
     */
    public void setScale(float newS) {
        scale = newS;
    }
    
    public void setBounceTimer(float newTime) {
        bounceTimer = newTime;
    }

    /**
     * Set the proportion of food trhe object will contain
     * @param newP
     */
    public void setProportion(float newP) {
        proportion = newP;
    }
    
    // Increase the scale factor of the station by a certain scalar
    public void multScale(float newS) {
        scale *= newS;
    }

    /**
     * Disable the Station if it is removed form the SceneGraph
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Check if the object is still in active in the world
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }
}   
