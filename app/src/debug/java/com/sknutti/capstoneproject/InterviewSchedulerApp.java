package com.sknutti.capstoneproject;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class InterviewSchedulerApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);
    LeakCanary.install(this);

      Timber.plant(new Timber.DebugTree());

      Stetho.initialize(
              Stetho.newInitializerBuilder(this)
                      .enableDumpapp(
                              Stetho.defaultDumperPluginsProvider(this))
                      .enableWebKitInspector(
                              Stetho.defaultInspectorModulesProvider(this))
                      .build());

  }

}
