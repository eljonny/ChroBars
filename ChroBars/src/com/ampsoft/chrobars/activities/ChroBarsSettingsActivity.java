package com.ampsoft.chrobars.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;

import com.ampsoft.chrobars.BarsRenderer;
import com.ampsoft.chrobars.ChroBar;
import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.R;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsSettingsActivity extends Activity
									  implements OnClickListener {

	private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	
	private ChroBar hour = BarsRenderer.getChroBar(ChroType.HOUR),
					minute = BarsRenderer.getChroBar(ChroType.MINUTE),
					second = BarsRenderer.getChroBar(ChroType.SECOND),
					millisecond = BarsRenderer.getChroBar(ChroType.MILLIS);
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.menu_settings_chro_bars);
		((SlidingDrawer)findViewById(R.id.chrobars_settings_slidingDrawer)).animateToggle();
		
		TableLayout container = (TableLayout)findViewById(R.id.chrobars_settings_slidingDrawer_contentTableLayout);
		
		for(View touchable : container.getTouchables()) {
			
			if(touchable instanceof CheckBox)
				checkBoxes.add((CheckBox)touchable);
			else if(touchable instanceof Button)
				buttons.add((Button)touchable);
		}

		for(CheckBox box : checkBoxes) {
			
			switch(box.getId()) {
			
			case R.id.chrobars_settings_slidingDrawer_chkbxHours:
				box.setChecked(hour.isDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxMinutes:
				box.setChecked(minute.isDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxSeconds:
				box.setChecked(second.isDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds:
				box.setChecked(millisecond.isDrawn());
				break;
			}
			
			box.setOnClickListener(this);
		}
		
		for(Button button : buttons)
			button.setOnClickListener(this);
	}

	/**
	 * 
	 */
	@Override
	public void onClick(View v) {
		
		if(v instanceof CheckBox) {
			
			CheckBox box = (CheckBox)v;
			
			switch(box.getId()) {
			
			case R.id.chrobars_settings_slidingDrawer_chkbxHours:
				hour.setDrawBar(box.isChecked());
				return;
			case R.id.chrobars_settings_slidingDrawer_chkbxMinutes:
				minute.setDrawBar(box.isChecked());
				return;
			case R.id.chrobars_settings_slidingDrawer_chkbxSeconds:
				second.setDrawBar(box.isChecked());
				return;
			case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds:
				millisecond.setDrawBar(box.isChecked());
				return;
			default:
			}
		}
		
		//TODO Error-check to make sure there is at least one chrobar selected
		
		//TODO Handle buttons
	}
}
