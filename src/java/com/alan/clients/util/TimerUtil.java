package com.alan.clients.util;


public class TimerUtil {
   private boolean run = true;
   private long time = System.currentTimeMillis();
   public boolean hasElapsed(long milliseconds) {
      return System.currentTimeMillis() - this.time > milliseconds;
   }

   public TimerUtil(boolean run) {
      this.run = run;
   }

   public void start() {
      this.run = true;
   }

   public void stop() {
      this.run = false;
   }

   public void reset() {
      this.time = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return this.run ? System.currentTimeMillis() - this.time : 0L;
   }

   public boolean hasTimeElapsed(long milliseconds) {
      return this.run && this.getElapsedTime() >= milliseconds;
   }

   public void delay(long milliseconds) {
      this.time += milliseconds;
   }

   public static long getCurrentTime() {
      return System.currentTimeMillis();
   }

   public boolean isOver(long milliseconds) {
      return System.currentTimeMillis() - this.time > milliseconds;
   }

   public long remainingTime(long milliseconds) {
      long elapsedTime = System.currentTimeMillis() - this.time;
      return elapsedTime < milliseconds ? milliseconds - elapsedTime : 0L;
   }

   public boolean isRun() {
      return this.run;
   }

   public long getTime() {
      return this.time;
   }

   public void setRun(boolean run) {
      this.run = run;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public TimerUtil() {
   }
}
