package pose.estimator;

import java.util.ArrayList;
import java.util.HashMap;
import pose.estimator.Utils.ChassisSpeeds;

public class Estimator {
    private Vision visionSystem;
    public Estimator(){
        this.visionSystem = new Vision(
            Constants.VisionConstants.ipAddress, 
            Constants.VisionConstants.CameraRotations, 
            null); 
    }
    public static void main(String[] args){
        Estimator estimator = new Estimator();

        estimator.init();
        while (true){
            estimator.periodic();
        }
    }

    public void init() {
        
    }

    public void periodic() {
        ChassisSpeeds output = visionSystem.getPieceDrive(2, 0.5, -10, 0 );
        output.print();
    }
        
}
