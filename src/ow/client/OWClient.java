package ow.client;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.server.ServerConnection;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class OWClient extends BasicGame implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWClient.class);

  private static final String SERVER_IP = "localhost";
  private static final int PORT = 19883;
  private static final JsonParser parser = new JsonParser();

  private ClientConnection conn;
  private final ClientModel model = new ClientModel();
  private Ship myShip = null;

  public OWClient() {
    super("Other World");

    logger.info("Connecting to the server....");
    conn = new ClientConnection(this, SERVER_IP, PORT, false);
    try {
      conn.connect(1000);
      logger.info("Connected!");
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void render(GameContainer container, Graphics gg) throws SlickException {
    SGraphics g = new SGraphics(gg);
    model.render(g, container.getWidth(), container.getHeight());
  }

  @Override
  public void init(GameContainer container) throws SlickException {}

  @Override
  public void update(GameContainer container, int delta) throws SlickException {}

  @Override
  public void connectionBroken(Connection broken, boolean forced) {
    logger.info("Lost connection to the server.");
  }

  @Override
  public void receive(byte[] data, Connection from) {
    JsonObject o = parser.parse(new String(data, Charsets.UTF_8)).getAsJsonObject();
    logger.debug(o);

    String command = o.get("command").getAsString().toLowerCase();

    if (command.equals("spawn")) {
      double x = o.get("x").getAsDouble();
      double y = o.get("y").getAsDouble();

      if (myShip == null) {
        myShip = new Ship();
        myShip.setImage("Mini.png");
        model.add(myShip);
        model.focus(myShip);
      }

      myShip.setLocation(x, y);
    }
  }

  @Override
  public void clientConnected(ServerConnection conn) {}

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    AppGameContainer appgc = new AppGameContainer(new OWClient());
    appgc.setDisplayMode(1200, 800, false);
    appgc.start();
  }

}
