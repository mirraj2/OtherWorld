package ow.client;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import jexxus.client.ClientConnection;
import jexxus.common.Delivery;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import ow.client.arch.SGraphics;
import ow.client.model.ClientModel;
import ow.client.model.Ship;

public class OWClient extends BasicGame {

  private static final Logger logger = Logger.getLogger(OWClient.class);

  private static final boolean FULLSCREEN = false;

  private static final String SERVER_IP = "localhost";
  private static final int PORT = 19883;

  private final ClientModel model = new ClientModel();
  private final NetworkHandler networkHandler = new NetworkHandler(this, model);
  private ClientConnection conn;
  private Ship myShip = null;
  private BackgroundRenderer backgroundRenderer;
  private GameContainer container;
  private int mouseX, mouseY;

  public OWClient() {
    super("Other World");

    logger.info("Connecting to the server....");
    conn = new ClientConnection(networkHandler, SERVER_IP, PORT, false);
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
    model.tick(delta);
  }

  @Override
  public void mousePressed(int button, int x, int y) {
    if (button != 0) {
      return;
    }

    mouseX = x;
    mouseY = y;

    JsonObject o = new JsonObject();
    o.addProperty("command", "shoot");
    sendToServer(o);
  }

  @Override
  public void mouseDragged(int oldX, int oldY, int newX, int newY) {
    mouseX = newX;
    mouseY = newY;

    pointShipAtMouse();
  }

  @Override
  public void mouseMoved(int oldX, int oldY, int newX, int newY) {
    mouseX = newX;
    mouseY = newY;

    pointShipAtMouse();
  }

  private void pointShipAtMouse() {
    if (myShip == null) {
      return;
    }

    Rectangle r = model.getCameraBounds(container.getWidth(), container.getHeight());
    myShip.rotateToTarget(r.x + mouseX, r.y + mouseY);
    sendShipUpdate();
  }

  @Override
  public void mouseReleased(int button, int x, int y) {
  }

  private final Map<Integer, Double> movementDirections = ImmutableMap.of(Input.KEY_W, 0d,
      Input.KEY_S, Math.PI, Input.KEY_A, Math.PI / 2, Input.KEY_D, -Math.PI / 2);

  @Override
  public void keyPressed(int key, char c) {
    Double d = movementDirections.get(key);
    if (d != null) {
      myShip.moving = true;
      myShip.movementDirection = d;
      sendShipUpdate();
    } else if (key == Input.KEY_ESCAPE) {
      container.exit();
      System.exit(0);
    }
  }

  @Override
  public void keyReleased(int key, char c) {
    if (movementDirections.containsKey(key)) {
      for (Integer i : movementDirections.keySet()) {
        if (Keyboard.isKeyDown(i)) {
          return;
        }
      }
      myShip.moving = false;
      myShip.movementDirection = 0;
      sendShipUpdate();
    }
  }

  @Override
  public void mouseWheelMoved(int change) {
    if (change < 0) {
      model.zoomOut();
    } else {
      model.zoomIn();
    }
  }

  private void sendShipUpdate() {
    JsonObject o = new JsonObject();
    o.addProperty("command", "update");
    o.addProperty("id", myShip.id);
    o.addProperty("x", myShip.x);
    o.addProperty("y", myShip.y);
    o.addProperty("rotation", myShip.rotation);
    o.addProperty("moving", myShip.moving);
    o.addProperty("direction", myShip.movementDirection);
    sendToServer(o);
  }

  private void sendToServer(JsonObject o) {
    conn.send(o.toString().getBytes(Charsets.UTF_8), Delivery.RELIABLE);
  }

  public void setMyShip(Ship ship){
    this.myShip = ship;
  }

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    AppGameContainer container = new AppGameContainer(new OWClient());

    DisplayMode mode = Display.getDesktopDisplayMode();

    container.setAlwaysRender(true);
    container.setTargetFrameRate(60);
    container.setVSync(true);

    if (FULLSCREEN) {
      container.setDisplayMode(mode.getWidth(), mode.getHeight(), true);
    } else {
      container.setDisplayMode(3 * mode.getWidth() / 4, 3 * mode.getHeight() / 4, false);
    }

    container.start();
  }

}
