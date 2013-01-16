package jp.inook.misawaWidget.widget;

import jp.inook.misawaWidget.MWApp;
import jp.inook.misawaWidget.MWUtil;
import jp.inook.misawaWidget.R;
import jp.inook.misawaWidget.activity.MWMainActivity;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MWWidgetProvider extends AppWidgetProvider {
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		MWUtil.log("onEnabled");
	}
	@Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] awi) {
		super.onUpdate(context,awm,awi);
		MWUtil.log("onUpdate");
		
		Intent serviceIntent = new Intent(context, MyService.class);
        context.startService(serviceIntent);
		MWUtil.getInstance().updateWidget(context);
    }
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		MWUtil.log("onReceive");
		
		if (MWUtil.ACTION_START_MISAWA_WIDGET.equals(intent.getAction())) {
			MWUtil.log("onReceive#ACTION_START_MISAWA_WIDGET");
			Intent serviceIntent = new Intent(context, MyService.class);
	        context.startService(serviceIntent);
	        MWUtil.getInstance().updateWidget(context);
		}
	}
	@Override
	public void onDisabled(Context context) {
		MWUtil.log("onDisabled");
		MWUtil.getInstance().stopAlarm(context);
		
		super.onDisabled(context);
	}
    
    //-----
    // Service
    //-----
    public static class MyService extends Service {
    	@Override
    	public void onCreate() {
    		MWUtil.log("[[[[[[[oncrecre");
    	}
        @Override
        public void onStart(Intent intent, int startId) {
        	MWUtil.log("MyService#onStart");
        	
        	// set broadcast receiver
        	setBroadcastReceiver();
            
            // update widget
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_main);
            String[] data = MWUtil.getInstance().getNextPhrase();
            if (data == null || data.length < 2) {
            	MWUtil.log("not found phrase and url");
            }
            else {
            	MWUtil.log("got a phrase["+data[0]+"] and a url["+data[1]+"]");
            	remoteViews.setTextViewText(R.id.mwTextViewPhrase, data[0]);
            	MWUtil.getInstance().setMisawaPhrase(data[0]);
            	MWUtil.getInstance().setMisawaUrl(data[1]);
            }
            
            // set click event
            setClickEvents(remoteViews);
            
            // execute updating
            ComponentName thisWidget = new ComponentName(this, MWWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, remoteViews);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public void onDestroy() {
        	MWUtil.log("MyService#onDestroy");
        	super.onDestroy();
        }
        
        //-----
        // private methods(in Service)
        //-----
        private void setClickEvents(RemoteViews remoteViews) {
        	// setting
        	Intent settingIntent = new Intent();
            settingIntent.setAction(MWUtil.ACTION_SETTING_MISAWA_WIDGET);
            PendingIntent pendingSettingIntent = PendingIntent.getBroadcast(this, 0, settingIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.mwTextViewSetting, pendingSettingIntent);
            
            // open browser
            Intent openIntent = new Intent();
            openIntent.setAction(MWUtil.ACTION_OPEN_MISAWA_WIDGET);
            PendingIntent pendingOpenIntent = PendingIntent.getBroadcast(this, 0, openIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.mwTextViewOpenBrowser, pendingOpenIntent);
            
            // change text
            Intent changeIntent = new Intent();
            changeIntent.setAction(MWUtil.ACTION_CHANGE_MISAWA_WIDGET);
            PendingIntent pendingChangeIntent = PendingIntent.getBroadcast(this, 0, changeIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.mwTextViewPhrase, pendingChangeIntent);
            
            // copy text
            Intent copyIntent = new Intent();
            copyIntent.setAction(MWUtil.ACTION_COPY_MISAWA_WIDGET);
            PendingIntent pendingCopyIntent = PendingIntent.getBroadcast(this, 0, copyIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.mwTextViewCopy, pendingCopyIntent);
        }
        private void setBroadcastReceiver() {
        	IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(MWUtil.ACTION_SETTING_MISAWA_WIDGET);
            filter.addAction(MWUtil.ACTION_CHANGE_MISAWA_WIDGET);
            filter.addAction(MWUtil.ACTION_OPEN_MISAWA_WIDGET);
            filter.addAction(MWUtil.ACTION_COPY_MISAWA_WIDGET);
            filter.addAction(MWUtil.ACTION_CHANGE_INTERVAL);
            registerReceiver(mBroadcastReceiver, filter);
        }
    }
    
    //-----
    // BroadcastReceiver
    //-----
    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		MWUtil.log("MyBroadcastReceiver#onReceive");
    		String action = intent.getAction();
    		if (action == null) {
    			return;
    		}
    		if (Intent.ACTION_SCREEN_OFF.equals(action)) {
    			MWUtil.log("ACTION_SCREEN_OFF");
    			// 端末の電源がOFF
    			MWUtil.getInstance().restAlarm(context);
    		}
    		else if (Intent.ACTION_SCREEN_ON.equals(action)) {
    			MWUtil.log("ACTION_SCREEN_ON");
    			// 端末の電源がON
    			Intent serviceIntent = new Intent(context, MyService.class);
    	        context.startService(serviceIntent);
    			MWUtil.getInstance().updateWidget(context);
    		}
    		else if (MWUtil.ACTION_SETTING_MISAWA_WIDGET.equals(action)) {
    			MWUtil.log("ACTION_SETTING_MISAWA_WIDGET");
    			// 設定を開く
    			Intent openIntent = new Intent(context, MWMainActivity.class);
    			openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(openIntent);
    		}
    		else if (MWUtil.ACTION_CHANGE_MISAWA_WIDGET.equals(action)) {
    			MWUtil.log("ACTION_CHANGE_MISAWA_WIDGET");
    			// widgetの文言変更
    			Intent serviceIntent = new Intent(context, MyService.class);
    	        context.startService(serviceIntent);
    	        MWUtil.getInstance().updateWidget(context);
    		}
    		else if (MWUtil.ACTION_OPEN_MISAWA_WIDGET.equals(action)) {
    			MWUtil.log("ACTION_OPEN_MISAWA_WIDGET");
    			// ミサワサイトを開く
    			openMisawaUrl(context);
    		}
    		else if (MWUtil.ACTION_COPY_MISAWA_WIDGET.equals(action)) {
    			MWUtil.log("ACTION_COPY_MISAWA_WIDGET");
    			// 名言をコピー
    			copyMisawaPhrase(context);
    		}
    		else if (MWUtil.ACTION_CHANGE_INTERVAL.equals(action)) {
    			MWUtil.log("ACTION_CHANGE_INTERVAL");
    			// activityでintervalが変更された
    			MWUtil.getInstance().updateWidget(context);
    		}
    	}
    	
    	//-----
    	// private methods(in Broadcast)
    	//-----
    	private void copyMisawaPhrase(Context context) {
    		String phrase = MWUtil.getInstance().getMisawaPhrase();
			if (phrase.length() == 0) {
				return;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    			copyMisawaPhraseUpper11(phrase);
    	    }
			else {
    	    	copyMisawaPhraseUnder11(phrase);
    	    }
			Toast.makeText(context, R.string.finish_copy, Toast.LENGTH_LONG).show();
    	}
    	@TargetApi(11)
    	private void copyMisawaPhraseUpper11(String phrase) {
			android.content.ClipboardManager clipboardManager =
					(android.content.ClipboardManager)MWApp.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
		    ClipData.Item item = new ClipData.Item(phrase);
		    String[] mimeTypes = new String[] {
		            ClipDescription.MIMETYPE_TEXT_PLAIN
		    };
		    ClipData clip = new ClipData("data", mimeTypes, item);
		    clipboardManager.setPrimaryClip(clip);
    	}
    	@SuppressWarnings("deprecation")
    	private void copyMisawaPhraseUnder11(String phrase) {
    		android.text.ClipboardManager clipboardManager =
	                (android.text.ClipboardManager)MWApp.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
	        clipboardManager.setText(phrase);
    	}
    	
    	private void openMisawaUrl(Context context) {
    		String url = MWUtil.getInstance().getMisawaUrl();
			if (url.length() == 0) {
				MWUtil.log("not found url");
				return;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
    	}
	};
}