package pose.estimator;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.lang.reflect.Method;

import java.net.URI;
import java.net.URISyntaxException;

public class WSClient {
    private static WebSocketClient client;
    private static String ip = "wss://echo.websocket.org";
    private String message;
    private boolean messageViewed = true;

    public WSClient(String ipAndPort) {
        setupWebsocket();
        ip = ipAndPort;
    }

    public WSClient(){
        setupWebsocket();
    }

    private void setupWebsocket() {
        try {
            client = new WebSocketClient(new URI(ip)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server");
                }

                @Override
                public void onMessage(String newMessage) {
                    message = newMessage;
                    System.out.println("Received message: " + message);
                    messageViewed = false;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from WebSocket server");
                    System.out.println("Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
                    setupWebsocket();
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("An error occurred:");
                    ex.printStackTrace();
                    
                }
            };
            client.connect();
        
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    public void sendMessage(String message) {
        client.send(message);
    }

    public void closeConnection() {
        client.close();
    }

    public void printCallback(String message) {
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        WSClient app = new WSClient("wss://echo.websocket.org");
        app.setupWebsocket();
    }
}