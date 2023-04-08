package a3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

// Scripting portion
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.Invocable;


/**
 * This class will be the manager for the scripting engine
 * The egine used will be javascript and there is no current reason to use another one
 */
public class ScriptFactory {
    private HashMap<String, Script> scripts;
    private HashMap<String, ScriptEngine> engines;
    private ScriptEngineManager manager;
    private boolean modificationOccurred;
    // private long fileLastModifiedTime = 0;

    public ScriptFactory() {
        engines = new HashMap<String, ScriptEngine>();
        scripts = new HashMap<String, Script>();
        manager = new ScriptEngineManager();
        modificationOccurred = false;
    }

    public void init() {
        initJSEngine();
    }
    public void initJSEngine() {
        engines.put("js", manager.getEngineByName("js"));
    }

    public void addScript(String scriptID, String path, boolean dynamicState) {
        Script newScript = new Script(path, dynamicState);
        scripts.put(scriptID, newScript);
    }

    public void addScript(String scriptID, String path) {
        Script newScript = new Script(path, false);
        scripts.put(scriptID, newScript);
    }

    public void runScript(String engineName, String sName) {
        scripts.get(sName).run(engines.get(engineName));
    }

    public void runScript(String engineName, Script s) {
        s.run(engines.get(engineName));
    }


    public void addJSScript(String scriptID, String path, boolean dynamicState) {
        addScript(scriptID, path, dynamicState);
        runScript("js", scriptID);
    }

    public void addJSScript(String scriptID, String path) {
        addScript(scriptID, path);
        runScript("js", scriptID);
    }

    public void update(String engineName) {
        modificationOccurred = false;
        for (Script s : scripts.values()) {
            if(s.isDynamic() && s.hasBeenModified()) {
                if (!modificationOccurred) modificationOccurred = true;
                runScript(engineName, s);
            }
        }
    }

    public boolean modificationUccurred() {
        return modificationOccurred;
    }

    public void runJSScripts() {
        for (Script s : scripts.values()) {
                runScript("js", s);
        }
    }

    public int getIntFromEngine(String engineName, String key) {
        return (int)(engines.get(engineName).get(key));
    }
    public float getDoubleFVFromEngine(String engineName, String key) {
        return ((Double)(engines.get(engineName).get(key))).floatValue();
    }

    
    private class Script {
        private File script;
        private long currModTime, fileLastModifiedTime;
        private boolean isDynamic;

        public Script(String path, boolean dynamicState) {
            script = new File(path);
            fileLastModifiedTime = 0;
            isDynamic = dynamicState;
        }
        
        public void run(ScriptEngine engine) { 
            try { 
                FileReader fileReader = new FileReader(script);
                engine.eval(fileReader);
                fileReader.close();
            }
            catch (FileNotFoundException e1) { System.out.println(script + " not found " + e1); }
            catch (IOException e2) { System.out.println("IO problem with " + script + e2); }
            catch (ScriptException e3) { System.out.println("ScriptException in " + script + e3); }
            catch (NullPointerException e4) { System.out.println ("Null ptr exception reading " + script + e4); } 
        }

        public boolean isDynamic() {
            return isDynamic;
        }
        
        public void enableDynamicUpdate() {
            isDynamic = true;
        }
        public void disableDynamicUpdate() {
            isDynamic = false;
        }

        public long lastModifiedTime() {
            return fileLastModifiedTime;
        }

        public boolean hasBeenModified() {
            currModTime = script.lastModified();
            System.out.println("this should be runing\n\n\n\\n");
            if (currModTime > fileLastModifiedTime) { 
                fileLastModifiedTime = currModTime;
                return true;
            }
            return false;
        }
    }
    
}