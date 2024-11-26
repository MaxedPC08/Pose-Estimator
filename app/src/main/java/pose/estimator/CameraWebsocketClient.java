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
    private static final String ip = "ws://10.42.0.118:50000";
    private static String message = "";
    private static boolean messageViewed = true;
    private static CountDownLatch latch;

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

    private String getMessage() {
        latch = new CountDownLatch(1);
        try {
            latch.await(5, TimeUnit.SECONDS); // Wait for the message or timeout after 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        messageViewed = true;
        return message;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to WebSocket server");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        CameraWebsocketClient.message = message;
        messageViewed = false;
        latch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Disconnected from WebSocket server");
        System.out.println("Reason: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("An error occurred:");
        throwable.printStackTrace();
    }

    public void sendMessage(Session session, String pMessage) {
        System.out.println("Sending message: " + pMessage);
        session.getAsyncRemote().sendText(pMessage);
    }

    public Info getInfo(Session session) {
        sendMessage(session, "info");
        String newMessage = getMessage();
        return getInfoFromString(newMessage);
    }

    public JsonObject decodeJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, JsonObject.class);
    }

    public static void main(String[] args) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI(ip);
            Session session = container.connectToServer(CameraWebsocketClient.class, uri);
            CameraWebsocketClient app = new CameraWebsocketClient();
            System.out.println("Connected?");
            Info info = app.getInfo(session);
            if (info != null) {
                System.out.println("Camera Name: " + info.cameraName);
                // Print other fields as needed
            } else {
                System.out.println("Failed to get info");
            }
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}