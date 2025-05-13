package icu.grely;

import arc.Application;
import arc.ApplicationListener;
import arc.Core;
import arc.Settings;
import arc.mock.MockFiles;
import arc.struct.Seq;
import arc.util.Log;
import icu.grely.bot.Loader;

import static icu.grely.SettingsLoader.saveSettings;
import static icu.grely.logger.Logger.loadLogger;

public class Main {
    public static void main(String[] args) {
        loadLogger();
        Log.info("Loading...");
        loadSettings();
        loadApp();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            Log.info("Saving settings, please, wait.");
            saveSettings();
        }));
        Loader.load();
    }

    public static void loadSettings() {
        Core.files = new MockFiles();
        Core.settings = new Settings();
        Core.settings.setAppName("kostylbot");
        Core.settings.setDataDirectory(Core.files.local("config"));
        Core.settings.load();
        SettingsLoader.loadSettings();
    }

    public static void loadApp() {
        Core.app = new Application() {
            @Override
            public Seq<ApplicationListener> getListeners(){
                return new Seq<ApplicationListener>();
            }

            @Override
            public ApplicationType getType() {
                Log.info("GetType used");
                return null;
            }

            @Override
            public String getClipboardText() {
                Log.info("getCLTestUsed used");
                return "";
            }

            @Override
            public void setClipboardText(String s) {
                Log.info("setClText used, text @", s);
            }

            @Override
            public synchronized void post(Runnable runnable){
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.err(e);
                }
            }

            @Override
            public void exit() {
                Log.info("Exit used");
                System.exit(0);
            }
        };
    }
}