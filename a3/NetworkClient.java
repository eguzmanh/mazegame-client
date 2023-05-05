package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.java.games.input.Event;

// import org.w3c.dom.events.Event;

import tage.ObjShape;
import tage.TextureImage;
import tage.input.action.AbstractInputAction;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkClient {

    private MazeGame game;
    private GhostManager gm;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;

    private ObjShape ghostS;
    private TextureImage ghostTx;
	
	
	private ObjShape npcS;
    private TextureImage npcTx;

	
    public NetworkClient(MazeGame game, String serverAddress, int serverPort, String protocol) {
        this.game = game;
        gm = new GhostManager(game);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
        this.isClientConnected = false;

		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
    }

    public GhostManager getGhostManager() { return gm; }

    public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostTx; }
	public ObjShape getNPCshape() { return npcS; }
	public TextureImage getNPCtexture() { return npcTx; }

	public void setNPCshape(ObjShape shape) { npcS = shape; }
	public void setNPCtexture(TextureImage texture) { npcTx = texture; }
    public void setGhostShape(ObjShape ghostShape) { ghostS = ghostShape; }
	public void setGhostTexture(TextureImage ghostTexture) { ghostTx = ghostTexture; }

    public void setupNetworking() {
		isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, game);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null) {
			protClient.processPackets();
		}
	}

    public void runProcessNetworking(float elapsTime) { processNetworking(elapsTime); }

	public ProtocolClient getProtClient() { return protClient; }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction {	
        @Override
        public void performAction(float time, Event evt) {
            if(protClient != null && isClientConnected == true) {	
                protClient.sendByeMessage();
			}

        }

    
	}
}
