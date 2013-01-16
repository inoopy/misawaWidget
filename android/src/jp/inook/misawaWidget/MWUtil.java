package jp.inook.misawaWidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.inook.misawaWidget.widget.MWWidgetProvider;
import jp.inook.misawaWidget.widget.MWWidgetProvider.MyService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 更新間隔の定義。
 * ミリ秒単位で設定する。
 */
enum Interval {
	LIST1("3秒", 3000, false),
	LIST2("5秒", 5000, false),
	LIST3("10秒", 10000, true),
	LIST4("30秒", 30000, false),
	LIST10("1分", 60000, false),
	LIST11("2分", 120000, false),
	LIST99("更新しない", -1, false);
	
	private final String label;
	private final int value;
	private final boolean isDef;
	Interval(String label, int value, boolean def) {
		this.label = label;
		this.value = value;
		this.isDef = def;
	}
	public String label() { return label; }
	public int value() { return value; }
	public boolean isDefault() { return isDef; }
}

/**
 * ウィジェット全体で利用するメソッド群。
 */
public class MWUtil {
	//-----
	// debug
	/** リリース時はfalseにする */
	static final private boolean DEBUG_MODE = false;
	
	//-----
	// action define
	static final public String ACTION_START_MISAWA_WIDGET
		= MWApp.getApplication().getResources().getString(R.string.action_start);
	static final public String ACTION_SETTING_MISAWA_WIDGET
		= MWApp.getApplication().getResources().getString(R.string.action_setting);
	static final public String ACTION_CHANGE_MISAWA_WIDGET
		= MWApp.getApplication().getResources().getString(R.string.action_change_text);
	static final public String ACTION_OPEN_MISAWA_WIDGET
		= MWApp.getApplication().getResources().getString(R.string.action_open);
	static final public String ACTION_COPY_MISAWA_WIDGET
		= MWApp.getApplication().getResources().getString(R.string.action_copy);
	static final public String ACTION_CHANGE_INTERVAL
		= MWApp.getApplication().getResources().getString(R.string.action_change_interval);
	//-----
	// other define
	static final public String SEPARATE = "\t";
	static final public String BASE_URL_MISAWA = "http://jigokuno.com/";
	static final public String BASE_URL_MISAWA_ARTICLE = BASE_URL_MISAWA + "?eid=";
	static final public String KEY_INTERVAL_POSITION = "interval_position";
	
	//-----
	// fields
	/** singletonパターン */
	static private MWUtil mInstance;
	/** 名言を参照 */
	private String mPhrase = "";
	/** 名言の元記事へのURL */
	private String mUrl = "";
	/** 次の名言を選択するランダム値 */
	private static final Random random = new Random();
	
	//-----
	// initializing methods
	//-----
	static public MWUtil getInstance() {
		if (mInstance == null) {
			mInstance = new MWUtil();
		}
		return mInstance;
	}
	
	static public void log(String str) {
		if (DEBUG_MODE) {
			Log.d("MWWidget", str);
		}
	}
	
	//-----
	// public methods
	//-----
	
	// getter and setter for interval
	public int getIntervalValue() {
		int pos = getIntervalPosition();
		Interval[] interval = Interval.values();
		
		MWUtil.log("got interval value is " + interval[pos].value());
		return interval[pos].value();
	}
	public int getIntervalPosition() {
		int defaultPosition = 0;
		Interval[] interval = Interval.values();
		int len = interval.length;
		
		for (int i = 0; i < len; i++) {
			if (interval[i].isDefault()) {
				defaultPosition = i;
			}
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MWApp.getApplication());
		int position = sp.getInt(KEY_INTERVAL_POSITION, defaultPosition);
		
		if (position < 0 || len <= position) {
			MWUtil.log("interval's position error:"+position);
			position = defaultPosition;
		}
		
		MWUtil.log("got position is " + position);
		return position;
	}
	public void setIntervalWithPosition(int position) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MWApp.getApplication());
		Editor e = sp.edit();
		e.putInt(KEY_INTERVAL_POSITION, position);
		e.commit();
		
		MWUtil.log("set position is " + position);
	}
	public String[] getIntervalLabels() {
		Interval[] interval = Interval.values();
		int len = interval.length;
		String[] returnVal = new String[len];
		for (int i = 0; i < len; i++) {
			returnVal[i] = interval[i].label();
		}
		return returnVal;
	}
	public int[] getIntervalValues() {
		Interval[] interval = Interval.values();
		int len = interval.length;
		int[] returnVal = new int[len];
		for (int i = 0; i < len; i++) {
			returnVal[i] = interval[i].value();
		}
		return returnVal;
	}
	
	public void setMisawaUrl(String url) {
		mUrl = url;
	}
	public String getMisawaUrl() {
		return mUrl;
	}
	public void setMisawaPhrase(String phrase) {
		mPhrase = phrase;
	}
	public String getMisawaPhrase() {
		return mPhrase;
	}
	public String[] getNextPhrase() {
		MWUtil.log("MWUtil#getNextPhrase");
		
		InputStream is = null;
        BufferedReader br = null;
        List<String> list = new ArrayList<String>();
        try {
            try {
                is = MWApp.getApplication().getResources().getAssets().open("data.txt");
                br = new BufferedReader(new InputStreamReader(is));
                
                String str = null;
                while ((str = br.readLine()) != null) {
                	list.add(str);
                }
            }
            finally {
                if (br != null) {
                    br.close();
                }
            }
        }
        catch (IOException e) {
        	MWUtil.log("IOException when opened data.txt");
        }
        
        int size = list.size();
        if (size == 0) {
        	return null;
        }
        String[] tmp = list.get(random.nextInt(size)).split(SEPARATE);
        if (tmp.length < 2) {
        	return null;
        }
        
        tmp[1] = BASE_URL_MISAWA_ARTICLE + tmp[1];
		return tmp;
	}
	public PendingIntent getPendingAlarmIntent(Context context) {
    	MWUtil.log("MWUtil#getPendingAlarmIntent");
    	
        Intent intent = new Intent(context, MWWidgetProvider.class);
        intent.setAction(MWUtil.ACTION_START_MISAWA_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }
	
	public void updateWidget(Context context) {
    	MWUtil.log("MWUtil#updateWidget");
    	
    	int interval = getIntervalValue();
    	if (interval <= 0) {
    		restAlarm(context);
    		return;
    	}
    	
    	restAlarm(context);
    	
        Intent intent = new Intent(context, MWWidgetProvider.class);
        intent.setAction(MWUtil.ACTION_START_MISAWA_WIDGET);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1;
        long after = now + interval - now % (interval);
        am.set(AlarmManager.RTC, after, sender);
    }
	public void stopAlarm(Context context) {
		MWUtil.log("MWUtil#stopAlarm");
		cancelAlarm(context, true);
	}
	public void restAlarm(Context context) { 
		MWUtil.log("MWUtil#restAlarm");
		cancelAlarm(context, false);
	}
	public boolean isServiceRunning(Context c, Class<?> cls) {
	    ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
	    for (RunningServiceInfo i : runningService) {
	        if (cls.getName().equals(i.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	//-----
	// private methods
	//-----
	private void cancelAlarm(Context context, boolean isStop) {
		// stop alarm
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = getPendingAlarmIntent(context);
        am.cancel(sender);
        
        // stop service
        if (isStop) {
        	Intent serviceIntent = new Intent(context, MyService.class);
        	context.stopService(serviceIntent);
        }
	}
}
