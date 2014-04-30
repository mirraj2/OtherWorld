package ow.client;

import java.awt.Rectangle;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
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
import ow.client.model.ClientModel;
import ow.client.model.Planet;
import ow.client.model.Ship;
import ow.client.model.Shot;
import ow.common.Faction;
import ow.common.ShipType;

public class OWClient extends BasicGame implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWClient.class);

  private static final boolean FULLSCREEN = false;

  private static final String SERVER_IP = "localhost";
  private static final int PORT = 19883;
  private static final JsonParser parser = new JsonParser();

  private ClientConnection conn;
  private final ClientModel model = new ClientModel();
  private Ship myShip = null;
  private BackgroundRenderer backgroundRenderer;
  private GameContainer container;
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

  @Override
  public void connectionBroken(Connection broken, boolean forced) {
    logger.info("Lost connection to the server.");
  }

  @Override
  public void receive(byte[] data, Connection from) {
    JsonObject o = parser.parse(new String(data, Charsets.UTF_8)).getAsJsonObject();

    String command = o.get("command").getAsString().toLowerCase();

    if (command.equals("ship")) {
      int id = o.get("id").getAsInt();
      double x = o.get("x").getAsDouble();
      double y = o.get("y").getAsDouble();
      double rotation = o.get("rotation").getAsDouble();

      Ship ship = new Ship(id, Faction.valueOf(o.get("faction").getAsString()),
          ShipType.valueOf(o.get("type").getAsString()))
          .setLocation(x, y).setRotation(rotation).halt();
      model.add(ship);

    } else if (command.equals("planet")) {
      model.add(new Planet(o.get("name").getAsString(), o.get("x").getAsDouble(), o.get("y")
          .getAsDouble()));
    } else if (command.equals("update")) {
      int id = o.get("id").getAsInt();
      Ship ship = model.getShip(id);
      ship.x = o.get("x").getAsDouble();
      ship.y = o.get("y").getAsDouble();
      ship.rotation = o.get("rotation").getAsDouble();
      ship.moving = o.get("moving").getAsBoolean();
    } else if (command.equals("shots")) {
      for (JsonElement e : o.getAsJsonArray("shots")) {
        JsonObject s = e.getAsJsonObject();
        model.add(new Shot(s.get("id").getAsInt(), s.get("x").getAsDouble(), s.get("y")
            .getAsDouble(), s.get("rotation").getAsDouble(), s.get("velocity").getAsDouble(), s
            .get("max_distance").getAsDouble()));
      }
    } else if (command.equals("take_control")) {
      int id = o.get("id").getAsInt();

      myShip = model.getShip(id);
      model.focus(myShip);
    } else {
      logger.warn("unknown message: " + o);
    }
  }

  @Override
  public void clientConnected(ServerConnection conn) {}

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    AppGameContainer container = new AppGameContainer(new OWClient());

    DisplayMode mode = Display.getDesktopDisplayMode();

    container.setAlwaysRender(true);

    if (FULLSCREEN) {
      container.setDisplayMode(mode.getWidth(), mode.getHeight(), true);
    } else {
      container.setDisplayMode(mode.getWidth() / 2, mode.getHeight() / 2, false);
    }

    container.start();
  }

}
