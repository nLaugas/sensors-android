package com.example.niko.performance;

/**
 * Created by niko on 4/26/18.
 */

public class PidAngle {
    private float kp, ki, kd, integrator, smoothingStrength, differencesMean,
            previousDifference, lastError;

    public PidAngle(float kp, float ki, float kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        previousDifference = 0.0f;
        integrator = 0.0f;
        differencesMean = 0.0f;
    }

    public float getInput(float targetAngle, float currentAngle, float dt)
    {

        float error = targetAngle - currentAngle;

        float input = 0.0f;

        // Proportional part.
        input += error * kp;

        // Integral part.
        integrator += error * dt;
        integrator = saturationControl(integrator); //control de saturacion
        if (!equalSigns(error, lastError))
            integrator = 0;
        input += integrator * ki;

        // Derivative part.
        float derivative = (error - lastError) / dt;
        input += derivative * kd;
        lastError = error;

        return input;
    }


    public void setConst(float kp, float ki, float kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }
    private boolean equalSigns(float value1, float value2){
        return  ((value1 < 0) && (value2 <0)) || ((value1 > 0) && (value2 >0));
    }
    public float saturationControl(float value){
        if (value > 106)
            return  106;
        else if (value < -122)
            return -122;
        return value;
    }
    public void resetIntegrator()
    {
        integrator = 0.0f;
    }
}
