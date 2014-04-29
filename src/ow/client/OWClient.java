package ow.client;

import java.awt.Rectangle;
import java.io.IOException;

import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.server.ServerConnection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OWClient extends BasicGame implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWClient.class);

  private static final String SERVER_IP = "localhost";
  private static final int PORT = 19883;
  private static final JsonParser parser = new JsonParser();

  private ClientConnection conn;
  private final ClientModel model = new ClientModel();
  private Ship myShip = null;
  private BackgroundRenderer backgroundRenderer;
  private GameContainer container;
  private boolean mouseDown = false;
  private int mouseX, mouseY;

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

    int w = container.getWidth();
    int h = container.getHeight();

    g.setColor(Color.black).fillRect(0, 0, w, h);

    Rectangle cameraLocation = model.getCameraBounds(w, h);
    backgroundRenderer.render(cameraLocation, g);

    model.render(g, w, h);
  }

  @Override
  public void init(GameContainer container) throws SlickException {
    this.container = container;
    backgroundRenderer = new BackgroundRenderer();
  }

  @Override
  public void update(GameContainer container, int delta) throws SlickException {
    if (mouseDown) {
      Rectangle r = model.getCameraBounds(container.getWidth(), container.getHeight());
      myShip.targetX = r.x + mouseX;
      myShip.targetY = r.y + mouseY;
    }

    model.tick(delta);
  }

  @Override
  public void mousePressed(int button, int x, int y) {
    if (button != 0) {
      return;
    }

    mouseDown = true;
    mouseX = x;
    mouseY = y;
    orderShipToClick();
  }

  @Override
  public void mouseDragged(int oldX, int oldY, int newX, int newY) {
    mouseX = newX;
    mouseY = newY;
    orderShipToClick();
  }

  @Override
  public void mouseMoved(int oldX, int oldY, int newX, int newY) {
    mouseX = newX;
    mouseY = newY;
  }

  @Override
  public void mouseReleased(int button, int x, int y) {
    if (button != 0) {
      return;
    }

    mouseDown = false;
    myShip.halt();
  }

  @Override
  public void keyPressed(int key, char c) {
    if (key == Input.KEY_ESCAPE) {
      container.exit();
      System.exit(0);
    }
  }

  private void orderShipToClick() {
    Rectangle r = model.getCameraBounds(container.getWidth(), container.getHeight());

    myShip.targetX = r.x + mouseX;
    myShip.targetY = r.y + mouseY;
    myShip.rotateToTarget();
  }

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
        myShip = new Ship("Mini.png", 200);
        model.add(myShip);
        model.focus(myShip);
      }

      model.add(new Ship("Mini.png", 200).setLocation(x, y).halt());
      // next step, make server spawn ships and have server move them around randomly

      myShip.setLocation(x, y).halt();
    }
  }

  @Override
  public void clientConnected(ServerConnection conn) {}

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    AppGameContainer appgc = new AppGameContainer(new OWClient());
    DisplayMode mode = Display.getDesktopDisplayMode();

    appgc.setDisplayMode(mode.getWidth(), mode.getHeight(), true);
    appgc.start();
  }

}
