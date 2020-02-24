package com.example.yy.chiprreader.utils;

import android.util.Log;

import java.util.Random;

public class Test {

    public static void main(String[] args) {

    }

    private static void testPlay(){
        float[] chirp = SignalProc.upChirp(48000,1000,21000,1);
        String bi = "1100101111";
        int l = bi.length();
        System.out.println("bi:" + bi);

        byte a,b;

        if (l <= 8){
            a = 0;
        }else {
            boolean isNeg1 = l == 16;
            a = isNeg1 ? (byte) (-(string2Byte(bi.substring(1,8))))
                    : string2Byte(bi.substring(0,l-8));
        }

        if (l < 8){
            b = string2Byte(bi.substring(0,l));
        }else {
            boolean isNeg2 = bi.charAt(l-8) == '1';
            b = isNeg2 ? (byte) (-string2Byte(bi.substring(l-7,l)))
                    : string2Byte(bi.substring(l-8,l));
        }
        System.out.println(a+","+b);
    }

    private static byte string2Byte(String byteStr){
        byte sum = 0;
        int len = byteStr.length();
        for (int i = len-1;i>=0;i--){
            sum += (byteStr.charAt(i)-'0') * Math.pow(2,len-i-1);
        }
        return sum;
    }

    public static void testNormJni(){
        short[] arr = new short[4];
        arr[0] = 2;
        arr[1] = 3;
        arr[2] = 4;
        arr[3] = 1;
        float[] res = Algorithm.normolizeArrayJni(arr);
        for (double d : res){
            Log.d("test","norm:" + d);
        }
    }

    public static void testMixJni(){
        float[] a1 = new float[4];
        float[] a2 = new float[4];
        for (int i = 0;i<4;i++){
            a1[i] = i;
            a2[i] = i;
        }
        float[] res = Algorithm.mixFrequenceJni(a1,a2);
        for (double d : res){
            Log.d("test","mix:" + d);
        }
    }

    public static void testFFTJni(){
        float[] input = new float[4];
        input[0] = 1;
        input[1] = 2;
        input[2] = -1;
        input[3] = 3;
        float[] res = Algorithm.fftJni(input,4);
        for (double d : res ){
            Log.d("test","fft:" + d);
        }
    }

    public static void testCorr(){
        float[] a = new float[]{1,2,2,1,2,3,1,2,3,1,0,-1,2,5,2};
        float[] b = new float[]{1,2,3,1};
        //int c = Algorithm.correlationJni(1,a,b);
        //Log.d("test","c:" + c);
        //int res = Algorithm.correlationJni(a,b);
        //Log.d("test","shift:" + res);
    }

    public static void testCorr2(){
        short[] a = new short[]{9,7,8,2,3,4,5,6,7,8};
        short[] b = new short[]{6,7,8};
        long sT = System.currentTimeMillis();
        int idx = Algorithm.correlationJni2(a,b);//计算a超前b的长度
        long eT = System.currentTimeMillis();
        //for (int i : idx){
            Log.d("test","idx:" + idx + " time:" + (eT-sT));
        //}

    }
}
