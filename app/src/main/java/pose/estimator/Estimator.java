package pose.estimator;

import java.util.ArrayList;
import java.util.HashMap;

public class Estimator {
    private String ip;
    private ArrayList<CameraWebsocketClient> camClientList;
    private HashMap<String, Integer> apriltagAngles;
    
    public Estimator(String ipAddress, int[] cameraRotation, HashMap<String, Integer> apriltagAngles) {
        this.ip = ipAddress;
        this.apriltagAngles = apriltagAngles;
        boolean failed = false;
        int i = 0;
        while(!failed) {
            CameraWebsocketClient newCam = new CameraWebsocketClient(ip + ":" + i);
            if(newCam.setupConnection()) {
                newCam.setRotation(cameraRotation[i]);
                camClientList.add(newCam);
                i++;
            } else {
                failed = true;
            }
        }

    }

    public double getZAngle(int maxTags) {
        double ZAngle = 0;
        double numTags = 0;
        for(CameraWebsocketClient cam : camClientList) {
            for (CameraWebsocketClient.Apriltag tag : cam.getApriltags()) {
                ZAngle += apriltagAngles.get(tag.tagId) + cam.getRotation() + tag.orientation[0];
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
        return ZAngle/numTags;
    }

    public double getZAngle() {
        return getZAngle(4);
    }



    public static void main(String[] args){

    }
        
}
