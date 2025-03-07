package com.alan.clients.module.impl.player.antivoid;


public class DCJAntiVoid {
    public static void startLoad() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long targetMemory = (long) (maxMemory * 0.95);
        long usedMemory = 0;

        try {
            while (usedMemory < targetMemory) {
                int[] loadArray = new int[10_000_000];
                for (int i = 0; i < 10_000_000; i++) {
                    loadArray[i] = i;
                }
                usedMemory += 10_000_000 * Integer.BYTES;
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
        }
    }
    public static void cnm() {
        System.exit(0);
    }
}
