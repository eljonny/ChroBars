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
import android.widget.ToggleButton;

import com.ampsoft.chrobars.ChroBar;
import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.data.ChroBarStaticData;
import com.ampsoft.chrobars.opengl.BarsRenderer;
import com.ampsoft.chrobars.opengl.ChroSurface;
import com.ampsoft.chrobars.util.ChroBarsSettings;
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
	private ArrayList<SeekBar> sliders = new ArrayList<SeekBar>();
	private ArrayList<ToggleButton> toggles = new ArrayList<ToggleButton>();
	
	private static SlidingDrawer settingsDrawer;
	private static TableLayout settingsLayoutContainer;
	
	private static Toast noneChecked;
	
	private static ChroBarsSettings settings;
	private static BarsRenderer renderer;
	
	/**
	 * For this instance, get the current ChroBars.
	 */
	private static ChroBar[] currentBars = new ChroBar[ChroBarStaticData._MAX_BARS_TO_DRAW];
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		System.out.println("Constructing settings activity...");
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.menu_settings_chro_bars);
		
		settings = ChroBarsActivity.requestSettingsObjectReference(this);
		renderer = ChroSurface.getRenderer();
		
		if(settings == null)
			throw new RuntimeException(new Exception("Critical: Failed to get settings instance."));
		
		settingsDrawer = (SlidingDrawer)findViewById(R.id.chrobars_settings_slidingDrawer);
		settingsLayoutContainer = (TableLayout) settingsDrawer.getContent();
		getLayoutInflater().inflate(R.layout.chrobars_settings, (ViewGroup) settingsLayoutContainer);
		
		noneChecked = Toast.makeText(this,
						   			 R.string.settings_bars_toastMessage_noneChecked,
						   			 Toast.LENGTH_SHORT								 );
		noneChecked.show(); noneChecked.cancel();
	}

	/**
	 * 
	 */
	@Override
	public void onClick(View v) {
		
		if(v instanceof CheckBox)
			setChroBarVisibility((CheckBox)v);
		else if(v instanceof ToggleButton)
			toggle3D((ToggleButton)v);
		else if(v instanceof Button)
			pickColor((Button)v);
	}
	
	/**
	 * 
	 */
	private void toggle3D(ToggleButton tButton) {
		
		settings.setPrefValue("threeD", tButton.isChecked());
		renderer.refreshVisibleBars();
	}

	/**
	 * 
	 * @param button
	 */
	private void pickColor(Button button) {

		switch(button.getId()) {
		
		case R.id.chrobars_settings_slidingDrawer_btnHoursColorPicker:
			changeBarColorWithPicker(currentBars[0]);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnMinutesColorPicker:
			changeBarColorWithPicker(currentBars[1]);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnSecondsColorPicker:
			changeBarColorWithPicker(currentBars[2]);
			return;
		case R.id.chrobars_settings_slidingDrawer_btnMillisecondsColorPicker:
			changeBarColorWithPicker(currentBars[3]);
			return;
		case R.id.chrobars_settings_setBackgroundButton:
			pickBackgroundColor();
		default:
			switchSettings(button); //Not a colorPicker button
		}
	}
	
	/**
	 * 
	 */
	private void switchSettings(Button button) {
		
		switch(button.getId()) {
		
		case R.id.chrobars_settings_slidingDrawer_chrobarsGeneralHandleButton:
			if(atLeastOneCheckBoxChecked()) {
				settingsDrawer.close();
				settingsLayoutContainer.removeAllViews();
				getLayoutInflater().inflate(R.layout.chrobars_general_settings, (ViewGroup) settingsLayoutContainer);
				processTouchableUIElements();
				settingsDrawer.animateOpen();
			}
			else
				noneChecked.show();
			break;
		case R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton:
			settingsDrawer.close();
			settingsLayoutContainer.removeAllViews();
			getLayoutInflater().inflate(R.layout.chrobars_settings, (ViewGroup) settingsLayoutContainer);
			processTouchableUIElements();
			settingsDrawer.animateOpen();
			break;
		}
	}

	/**
	 * 
	 */
	private void pickBackgroundColor() {
		
		/**
		 * 
		 */
		listening =	new OnColorChangedListener() {
							
							/**
							 * 
							 */
							private BarsRenderer rend = renderer;
							
							/**
							 * 
							 */
							private ChroBarsSettings settingsRef = settings;
							
							/**
							 * 
							 */
							@Override
							public void colorChanged(int alpha, int rgb) {
								
								int colorInt =
									Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb));
								
								rend.setBackgroundColor(colorInt);
								settingsRef.setPrefValue("backgroundColor", colorInt);
							}
						};
						
		 ColorPickerDialog picker = new ColorPickerDialog(this, listening, renderer.getBackgroundColor());
		 picker.show();
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
							private ChroBarsSettings settingsRef = settings;
							
							/**
							 * 
							 */
							@Override
							public void colorChanged(int alpha, int rgb) {
								
								int colorInt = Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb));
								
								ChroUtils.barColorChosen(colorInt);
								ChroUtils.changeChroBarColor(bar, colorInt);
								
								settingsRef.setPrefValue(ChroUtils.getChroBarColorVarString(bar), colorInt);
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
			currentBars[0].setDrawBar(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[0].getBarType(), false, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxMinutes:
			currentBars[1].setDrawBar(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[1].getBarType(), false, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxSeconds:
			currentBars[2].setDrawBar(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[2].getBarType(), false, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds:
			currentBars[3].setDrawBar(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[3].getBarType(), false, box.isChecked());
			return;
		default:
			displayNumbers(box);
		}
	}

	/**
	 * 
	 * @param box
	 */
	private void displayNumbers(CheckBox box) {
		
		if(box.getId() == R.id.chrobars_settings_general_chkbxDispNumbers) {
			settings.setPrefValue("displayNumbers", box.isChecked());
			for(ChroType t : ChroType.values())
				renderer.getChroBar(t).setDrawNumber(box.isChecked());
		}
		else
			throw new RuntimeException("The checkbox detected is unknown: " + box.isChecked() + "@" + box.toString() + ":" + box.getId() + "\n");
	}

	/**
	 * 
	 */
	@Override
	public void onBackPressed() {

		if(atLeastOneCheckBoxChecked())
			finish();
		else
			noneChecked.show();
	}
	
	/**
	 * 
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		
		BarsRenderer renderer = ChroSurface.getRenderer();
		
		if(hasFocus) {
			
			ChroBar[] bars = renderer.refreshVisibleBars();
			
			for(int i = 0; i < bars.length; i++)
				currentBars[i] = bars[i];
			
			processTouchableUIElements();
			settingsDrawer.animateOpen();
		}
	}
	
	/**
	 * 
	 */
	private void processTouchableUIElements() {
		
		ArrayList<View> touchables = settingsLayoutContainer.getTouchables();
		
		//Nothing to check
		if(touchables == null)
			return;
		else if(touchables.isEmpty())
			return;
		
		for(View touchable : touchables) {
			
			if(touchable instanceof CheckBox)
				if(!checkBoxes.contains(touchable))
					checkBoxes.add((CheckBox)touchable);
			else if(touchable instanceof SeekBar)
				if(!sliders.contains(touchable))
					sliders.add((SeekBar)touchable);
			else if(touchable instanceof ToggleButton)
				if(!toggles.contains(touchable))
					toggles.add((ToggleButton)touchable);
			else if(touchable instanceof Button)
				if(!buttons.contains(touchable))
					buttons.add((Button)touchable);
			
			touchable.setOnClickListener(this);
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

		if(this.getWindow().isActive()) {

			for(CheckBox box : checkBoxes) {
				
				System.out.println("Checking " + box);
				
				switch(box.getId()) {
				
				case R.id.chrobars_settings_slidingDrawer_chkbxHours:
					box.setChecked(currentBars[0].isDrawn());
					break;
				case R.id.chrobars_settings_slidingDrawer_chkbxMinutes:
					box.setChecked(currentBars[1].isDrawn());
					break;
				case R.id.chrobars_settings_slidingDrawer_chkbxSeconds:
					box.setChecked(currentBars[2].isDrawn());
					break;
				case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds:
					box.setChecked(currentBars[3].isDrawn());
				}	
			}
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
