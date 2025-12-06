package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.util.ElapsedTime;

public class elap {
    ElapsedTime elapObji = new ElapsedTime();

    double elapTime = (double) elapObji.nanoseconds() / 1000000000;
    boolean elapRuni = false;
    double elapLimt = 0.0;
    
    public void start(double setiLimt) {
        elapObji.reset();
        
        elapLimt = setiLimt;
        elapRuni = true;
    }
    
    public void updt() {
        elapTime = (double) elapObji.nanoseconds() / 1000000000;

        if (elapTime > elapLimt) {
            elapRuni = false;
        }
    }
    
    public double geti() {
        return elapTime;
    }
    
    public boolean runi() {
        return elapRuni;
    }
}
