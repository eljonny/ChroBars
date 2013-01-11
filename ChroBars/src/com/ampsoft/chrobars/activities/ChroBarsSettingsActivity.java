package com.ampsoft.chrobars.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.Toast;

import com.ampsoft.chrobars.BarsRenderer;
import com.ampsoft.chrobars.ChroBar;
import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.util.ChroUtils;
import com.ampsoft.chrobars.util.ColorPickerDialog;
import com.ampsoft.chrobars.util.ColorPickerDialog.OnColorChangedListener;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsSettingsActivity extends Activity
									  implements OnClickListener {
	
	private static OnColorChangedListener listening;
	
	private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	
	private static SlidingDrawer settingsDrawer;
	private static TableLayout settingsLayoutContainer;
	
	/**
	 * For this instance, get the current ChroBars.
	 */
	private final ChroBar hour = BarsRenderer.getChroBar(ChroType.HOUR),
						  minute = BarsRenderer.getChroBar(ChroType.MINUTE),
						  second = BarsRenderer.getChroBar(ChroType.SECOND),
						  millisecond = BarsRenderer.getChroBar(ChroType.MILLIS);

	private ArrayList<SeekBar> sliders = new ArrayList<SeekBar>();
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.menu_settings_chro_bars);
		
		settingsDrawer = (SlidingDrawer)findViewById(R.id.chrobars_settings_slidingDrawer);
		settingsLayoutContainer = (TableLayout) settingsDrawer.getContent();
		getLayoutInflater().inflate(R.layout.chrobars_settings, (ViewGroup) settingsLayoutContainer);
		settingsDrawer.animateToggle();
		
		processTouchableUIElements();
	}

	/**
	 * 
	 */
	@Override
	public void onClick(View v) {
		
		if(v instanceof CheckBox)
			setChroBarVisibility((CheckBox)v);
		else if(v instanceof Button)
			pickBarColor((Button)v);
	}
	
	/**
	 * 
	 * @param button
	 */
	private void pickBarColor(Button button) {

		switch(button.getId()) {
		
		case R.id.chrobars_settings_slidingDrawer_btnHoursColorPicker:
			changeBarColorWithPicker(hour);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnMinutesColorPicker:
			changeBarColorWithPicker(minute);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnSecondsColorPicker:
			changeBarColorWithPicker(second);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnMillisecondsColorPicker:
			changeBarColorWithPicker(millisecond);
			return;
		default:
			switchSettings(button);
		}
	}
	
	/**
	 * 
	 */
	private void switchSettings(Button button) {

		settingsDrawer.close();
		settingsLayoutContainer.removeAllViews();
		
		switch(button.getId()) {
		
		case R.id.chrobars_settings_slidingDrawer_chrobarsGeneralHandleButton:
			getLayoutInflater().inflate(R.layout.chrobars_general_settings, (ViewGroup) settingsLayoutContainer);
			processTouchableUIElements();
			settingsDrawer.animateOpen();
			break;
		case R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton:
			getLayoutInflater().inflate(R.layout.chrobars_settings, (ViewGroup) settingsLayoutContainer);
			processTouchableUIElements();
			settingsDrawer.animateOpen();
			break;
		default:
			//Do nothing for now, unknown button
		}
	}

	/**
	 * 
	 * @param barToChange
	 */
	private void changeBarColorWithPicker(final ChroBar barToChange) {

		/**
		 * 
		 */
		listening =	new OnColorChangedListener() {
							
							/**
							 * 
							 */
							private ChroBar bar = barToChange;
							
							/**
							 * 
							 */
							@Override
							public void colorChanged(int alpha, int rgb) {
								
								ChroUtils.barColorChosen(Color.argb
										(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb)));
								ChroUtils.changeChroBarColor(bar,
										Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb)));
							}
						};
						
		 ColorPickerDialog picker = new ColorPickerDialog(this, listening, barToChange.getBarColor());
		 picker.show();
	}

	/**
	 * 
	 * @param box
	 */
	private void setChroBarVisibility(CheckBox box) {
		
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
			//do nothing
		}
	}

	/**
	 * 
	 */
	@Override
	public void onBackPressed() {

		if(atLeastOneCheckBoxChecked())
			finish();
		else {
			Toast message = new Toast(this);
			message.setText(R.string.settings_bars_toastMessage_noneChecked);
			message.show();
		}
	}
	
	/**
	 * 
	 */
	private void processTouchableUIElements() {

		checkBoxes.clear();
		buttons.clear();
		sliders.clear();
		
		for(View touchable : settingsLayoutContainer.getTouchables()) {
			
			if(touchable instanceof CheckBox)
				checkBoxes.add((CheckBox)touchable);
			else if(touchable instanceof Button)
				buttons.add((Button)touchable);
			else if(touchable instanceof SeekBar)
				sliders.add((SeekBar)touchable);
		}
		
		buttons.add((Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsGeneralHandleButton));
		buttons.add((Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton));
		
		checkCheckBoxes();
		
		for(Button button : buttons)
			button.setOnClickListener(this);
	}
	
	/**
	 * 
	 */
	private void checkCheckBoxes() {

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
	}

	/**
	 * 
	 * @return
	 */
	private boolean atLeastOneCheckBoxChecked() {

		byte checkedBoxes = 0;
		
		for(CheckBox check : checkBoxes)
			if(check.isChecked())
				++checkedBoxes;
		
		return (checkedBoxes > 0);
	}
}
