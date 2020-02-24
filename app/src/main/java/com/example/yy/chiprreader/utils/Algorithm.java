package com.example.yy.chiprreader.utils;

import java.util.List;

public class Algorithm {

    /*cd
    *  将chirp与left进行相关，并找出startIndex
    * */
    public native static float correlationJni(float[] chirp,float[] left);

    public native static int correlationJni2(short[] chirp,short[] left);
    //public native static double[] correlationJni2(double[] chirp,double[] left,int n);

    /*
    *  混频
    * */
    public native static float[] mixFrequenceJni(float[] s1, float[] s2);

    public native static float mixAndSumJni(float[] s1,float[] s2);

    /*
    *  FFT
    * */
    public native static float[] fftJni(float[] signal,int len);

    public native static float[] normolizeArrayJni(short[] arr);

}
