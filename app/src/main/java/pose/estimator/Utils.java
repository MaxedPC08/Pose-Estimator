package pose.estimator;

class Utils{
    static class PIDController{
        double kP;
        double kI;
        double kD;
        double setpoint;
        double prevError;
        double integral;

        public PIDController(double kP, double kI, double kD) {
            this.kP = kP;
            this.kI = kI;
            this.kD = kD;
            this.setpoint = 0;
            this.prevError = 0;
            this.integral = 0;
        }

        public void setSetpoint(double setpoint) {
            this.setpoint = setpoint;
        }

        public void enableContinuousInput(double minInput, double maxInput) {
            // Not implemented
        }

        public double calculate(double input) {
            double error = setpoint - input;
            integral += error;
            double derivative = error - prevError;
            prevError = error;

            return (kP * error) + (kI * integral) + (kD * derivative);
        }
    }

    static class ChassisSpeeds{
        double vxMetersPerSecond;
        double vyMetersPerSecond;
        double omegaRadiansPerSecond;

        public ChassisSpeeds(double vxMetersPerSecond, double vyMetersPerSecond, double omegaRadiansPerSecond) {
            this.vxMetersPerSecond = vxMetersPerSecond;
            this.vyMetersPerSecond = vyMetersPerSecond;
            this.omegaRadiansPerSecond = omegaRadiansPerSecond;
        }
        public void print(){
            System.out.println("vx: " + vxMetersPerSecond + ", vy: " + vyMetersPerSecond + ", omega: " + omegaRadiansPerSecond);
        }
    }
}