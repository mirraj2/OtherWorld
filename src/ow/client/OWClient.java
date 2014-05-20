package ow.client;

import java.awt.Rectangle;
import java.io.IOException;

import ow.client.arch.SGraphics;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import jexxus.client.ClientConnection;
import jexxus.common.Delivery;
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

    myShip.halt();
    sendShipUpdate();
  }

  @Override
  public void keyPressed(int key, char c) {
    if (key == Input.KEY_SPACE) {
      JsonObject o = new JsonObject();
      o.addProperty("command", "shoot");
      sendToServer(o);
    } else if (key == Input.KEY_ESCAPE) {
      container.exit();
      System.exit(0);
    }
  }

  private void orderShipToClick() {
    Rectangle r = model.getCameraBounds(container.getWidth(), container.getHeight());

    myShip.rotateToTarget(r.x + mouseX, r.y + mouseY);
    myShip.moving = true;

    sendShipUpdate();
  }

  private void sendShipUpdate() {
    JsonObject o = new JsonObject();
    o.addProperty("command", "update");
    o.addProperty("id", myShip.id);
    o.addProperty("x", myShip.x);
    o.addProperty("y", myShip.y);
    o.addProperty("rotation", myShip.rotation);
    o.addProperty("moving", myShip.moving);
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

    if (FULLSCREEN) {
      container.setDisplayMode(mode.getWidth(), mode.getHeight(), true);
    } else {
      container.setDisplayMode(2 * mode.getWidth() / 3, 2 * mode.getHeight() / 3, false);
    }

    container.start();
  }

}
