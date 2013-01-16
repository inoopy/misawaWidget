package jp.inook.misawaWidget.activity;

import jp.inook.misawaWidget.MWUtil;
import jp.inook.misawaWidget.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MWMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// set official link
		TextView about = (TextView)findViewById(R.id.about_description);
		about.setMovementMethod(LinkMovementMethod.getInstance());
		
		// set author twitter
		TextView author = (TextView)findViewById(R.id.author_tw);
		author.setMovementMethod(LinkMovementMethod.getInstance());
		
		// set interval spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
											android.R.layout.simple_spinner_item);
		String [] interval_labels = MWUtil.getInstance().getIntervalLabels();
		int lim = interval_labels.length;
		for (int i = 0; i < lim; i++) {
			adapter.add(interval_labels[i]);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner interval = (Spinner)findViewById(R.id.interval_spinner);
		interval.setAdapter(adapter);
		interval.setSelection(MWUtil.getInstance().getIntervalPosition());
		interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	MWUtil.log("MWMainActivity#onItemSelected:"+position);
                MWUtil.getInstance().setIntervalWithPosition(position);
                
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MWUtil.ACTION_CHANGE_INTERVAL);
                getBaseContext().sendBroadcast(broadcastIntent);
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	MWUtil.log("MWMainActivity#onNothingSelected");
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}

}
