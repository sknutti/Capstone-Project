package com.sknutti.capstoneproject;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class InterviewSchedulerApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);

    Timber.plant(new CrashReportingTree());
  }

  private static class CrashReportingTree extends Timber.Tree {
    @Override protected void log(int priority, String tag, String message, Throwable t) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return;
      }

      FakeCrashLibrary.log(priority, tag, message);

      if (t != null) {
        if (priority == Log.ERROR) {
          FakeCrashLibrary.logError(t);
        } else if (priority == Log.WARN) {
          FakeCrashLibrary.logWarning(t);
        }
      }
    }
  }

}
