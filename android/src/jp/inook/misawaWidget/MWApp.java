package jp.inook.misawaWidget;

import android.app.Application;

public class MWApp extends Application {
	private static MWApp mInstance = null;

    public MWApp() {
    	if (mInstance == null) {
    		mInstance = this;
    	}
    }
    
    static public MWApp getApplication() {
    	return mInstance;
    }
}

