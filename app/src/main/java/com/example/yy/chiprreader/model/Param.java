package com.example.yy.chiprreader.model;

public class Param {
    private int fmin;
    private int fmax;
    private int fs;
    private int B;
    private float T;
    private float micDist;

    public int getFmin() {
        return fmin;
    }

    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

    public int getFmax() {
        return fmax;
    }

    public void setFmax(int fmax) {
        this.fmax = fmax;
    }

    public int getFs() {
        return fs;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    public float getT() {
        return T;
    }

    public void setT(float t) {
        T = t;
    }

    public float getMicDist() {
        return micDist;
    }

    public void setMicDist(float micDist) {
        this.micDist = micDist;
    }
}
