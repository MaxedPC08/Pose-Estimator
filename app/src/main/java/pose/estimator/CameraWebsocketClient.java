package pose.estimator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.websocket.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class CameraWebsocketClient {
    private String ip = "ws://10.42.0.118:50000";
    private String message = "";
    private CountDownLatch latch;
    private Session wSession;
    private double rotation;

    public static class Color {
        public double red;
        public double green;
        public double blue;
        public double difference;
        public double blur;
    }

    public static class Info {
        public String cameraName;
        public String identifier;
        public double horizontalFocalLength;
        public double verticalFocalLength;
        public int height;
        public int horizontalResolutionPixels;
        public int verticalResolutionPixels;
        public int processingScale;
        public double tiltAngleRadians;
        public double horizontalFieldOfViewRadians;
        public double verticalFieldOfViewRadians;
        public List<Color> colorList;
        public int activeColor;
    }

    public static class Apriltag {
        public String tagId;
        public double[] position;
        public double[] orientation;
        public double distance;
        public double horizontalAngle;
        public double verticalAngle;
    }

    public CameraWebsocketClient() {}

    public CameraWebsocketClient(String ip) {
        this.ip = ip;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to WebSocket server");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        this.message = message;
        latch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Disconnected from WebSocket server");
        System.out.println("Reason: " + closeReason);
        setupConnection();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("An error occurred:");
        throwable.printStackTrace();
    }

    public boolean setupConnection() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI(this.ip);
            wSession = container.connectToServer(CameraWebsocketClient.class, uri);
            CameraWebsocketClient app = new CameraWebsocketClient();
            System.out.println("Connected?");
            Info info = app.getInfo();
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    private void sendMessage(Session session, String pMessage) {
        System.out.println("Sending message: " + pMessage);
        session.getAsyncRemote().sendText(pMessage);
    }

    private String getMessage() {
        latch = new CountDownLatch(1);
        try {
            latch.await(5, TimeUnit.SECONDS); // Wait for the message or timeout after 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message;
    }

    public JsonObject decodeJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, JsonObject.class);
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public Info getInfo() {
        try {
            sendMessage(wSession, "info");
            String newMessage = getMessage();
            return getInfoFromString(newMessage);
        } catch (Exception e) {
            e.printStackTrace();
            if (setupConnection()){
                sendMessage(wSession, "info");
                String newMessage = getMessage();
                return getInfoFromString(newMessage);
            }
            return null;
        }
    }

    private Info getInfoFromString(String pMessage) {
        try {
            JsonObject json = decodeJson(pMessage);
            Info info = new Info();
            info.cameraName = json.get("cam_name").getAsString();
            info.identifier = json.get("identifier").getAsString();
            info.horizontalFocalLength = json.get("horizontal_focal_length").getAsDouble();
            info.verticalFocalLength = json.get("vertical_focal_length").getAsDouble();
            info.height = json.get("height").getAsInt();
            info.horizontalResolutionPixels = json.get("horizontal_resolution_pixels").getAsInt();
            info.verticalResolutionPixels = json.get("vertical_resolution_pixels").getAsInt();
            info.processingScale = json.get("processing_scale").getAsInt();
            info.tiltAngleRadians = json.get("tilt_angle_radians").getAsDouble();
            info.horizontalFieldOfViewRadians = json.get("horizontal_field_of_view_radians").getAsDouble();
            info.verticalFieldOfViewRadians = json.get("vertical_field_of_view_radians").getAsDouble();
            info.activeColor = json.get("active_color").getAsInt();
            info.colorList = new ArrayList<>();
            for (var colorJson : json.getAsJsonArray("color_list")) {
                Color color = new Color();
                JsonObject colorObject = colorJson.getAsJsonObject();
                color.red = colorObject.get("red").getAsDouble();
                color.green = colorObject.get("green").getAsDouble();
                color.blue = colorObject.get("blue").getAsDouble();
                color.difference = colorObject.get("difference").getAsDouble();
                color.blur = colorObject.get("blur").getAsDouble();
                info.colorList.add(color);
            }
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Apriltag> getApriltags() {
        try {
            sendMessage(wSession, "fa");
            String newMessage = getMessage();
            return getApriltagsFromString(newMessage);
        } catch (Exception e) {
            e.printStackTrace();
            if (setupConnection()){
                sendMessage(wSession, "fa");
                String newMessage = getMessage();
                return getApriltagsFromString(newMessage);
            }
            return null;
        }
    }


    private List<Apriltag> getApriltagsFromString(String pMessage) {
        try {
            JsonObject json = decodeJson(pMessage);
            List<Apriltag> apriltags = new ArrayList<>();
            for (var apriltagJson : json.getAsJsonArray()) {
                Apriltag apriltag = new Apriltag();
                JsonObject apriltagObject = apriltagJson.getAsJsonObject();
                apriltag.tagId = apriltagObject.get("tag_id").getAsString();
                apriltag.position = new double[]{
                    apriltagObject.get("position").getAsJsonArray().get(0).getAsDouble(),
                    apriltagObject.get("position").getAsJsonArray().get(1).getAsDouble(),
                    apriltagObject.get("position").getAsJsonArray().get(2).getAsDouble()
                };
                apriltag.orientation = new double[]{
                    apriltagObject.get("orientation").getAsJsonArray().get(0).getAsDouble(),
                    apriltagObject.get("orientation").getAsJsonArray().get(1).getAsDouble(),
                    apriltagObject.get("orientation").getAsJsonArray().get(2).getAsDouble()
                };
                apriltag.distance = apriltagObject.get("distance").getAsDouble();
                apriltag.horizontalAngle = apriltagObject.get("horizontal_angle").getAsDouble();
                apriltag.verticalAngle = apriltagObject.get("vertical_angle").getAsDouble();
                apriltags.add(apriltag);
            }
            return apriltags;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}