package pose.estimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pose.estimator.CameraWebsocketClient.Apriltag;

public class Estimator {
    private String ip;
    private ArrayList<CameraWebsocketClient> camClientList = new ArrayList<CameraWebsocketClient>();
    private HashMap<String, Integer> apriltagAngles;
    
    public Estimator(String ipAddress, int[] cameraRotation, HashMap<String, Integer> apriltagAngles) {
        // This constructor is not ideal but it works for the example. IRL you would want to use the other constructor so you can still have a list of cameras outside of the estimator.
        // Camera Rotation is the rotation of each camera in degrees. 0 is the default rotation.
        // Apriltag Angles is a hashmap of the apriltag id to the angle of the tag in degrees. 0 is facing the camera.

        this.ip = ipAddress;
        this.apriltagAngles = apriltagAngles;
        boolean failed = false;
        int i = 0;
        while(!failed) {
            System.out.println("Trying to commect to: " + ip + ":" + (i + 50000));
            CameraWebsocketClient newCam = new CameraWebsocketClient(ip + ":" + (i + 50000));
            newCam.setupConnection();

            if(newCam.isConnected()) {
                newCam.setRotation(cameraRotation[i]);
                camClientList.add(newCam);
                i++;
            } else {
                failed = true;
            }
        }

    }

    public Estimator(CameraWebsocketClient[] camList, HashMap<String, Integer> apriltagAngles) {
        this.apriltagAngles = apriltagAngles;
        for(CameraWebsocketClient newCam : camList) {
            if(newCam.isConnected()) {
                camClientList.add(newCam);
            }
        }

    }

    public double getZAngle(int maxTags) {
        // This function returns the average calculated angle of the robot in degrees on the z axis, aka the only one the robot turns on. Limit the number of tags to use with maxTags if you want.

        double ZAngle = 0;
        double numTags = 0;
        for(CameraWebsocketClient cam : camClientList) {
            List<CameraWebsocketClient.Apriltag> tags = cam.getApriltags();

            System.out.println(tags.size());

            for (CameraWebsocketClient.Apriltag tag : tags) {
                int tagAngle = apriltagAngles.getOrDefault(tag.tagId, 0);
                ZAngle += tagAngle * (180/Math.PI) + cam.getRotation() + tag.orientation[1];
                numTags++;
                if(numTags == maxTags) {
                    break;
                }
            }
            if(numTags == maxTags) {
                break;
            }
        }
        if(numTags == 0) {
            return 69420.0; // nice
        }
        return ZAngle/numTags % 360;
    }

    public double getZAngle() {

        return getZAngle(4);
    }

    

    public static void main(String[] args){
        HashMap<String, Integer> apriltagAngles = new HashMap<>();
        apriltagAngles.put("tag1", 30);
        apriltagAngles.put("tag2", 45);
        apriltagAngles.put("tag3", 60);
        apriltagAngles.put("tag4", 90);

        int[] cameraRotation = {0, 90, 180, 270};

        Estimator estimator = new Estimator("ws://10.42.0.118", cameraRotation, apriltagAngles);

        while (true) {
            System.out.println(estimator.getZAngle());
        }
    }
        
}
