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
	
	/*
	 * These ArrayLists store the UI elements that the user interacts with.
	 */
	private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	private ArrayList<SeekBar> sliders = new ArrayList<SeekBar>();
	private ArrayList<ToggleButton> toggles = new ArrayList<ToggleButton>();
	
	private static SlidingDrawer settingsDrawer;
	private static TableLayout settingsLayoutContainer;
	private static int lastLayout;
	
	private static Toast noneChecked;
	
	private static ChroBarsSettings settings;
	private static BarsRenderer renderer;
	
	/**
	 * For this instance, get the currently drawn ChroBars.
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
		
		lastLayout = settings.getSettingsActivityLayout();
		getLayoutInflater().inflate(lastLayout, (ViewGroup) settingsLayoutContainer);

		currentBars = renderer.refreshVisibleBars();
		processTouchableUIElements();
		
		noneChecked = Toast.makeText(this,
						   			 R.string.settings_bars_toastMessage_noneChecked,
						   			 Toast.LENGTH_SHORT								 );
		noneChecked.show(); noneChecked.cancel();
	}
	
	/**
	 * 
	 */
	@Override
	public void onPause() {
		super.onPause();
		settingsDrawer.animateClose();
	}
	
	/**
	 * 
	 */
	@Override
	public void onResume() {
		super.onResume();
		settingsDrawer.animateOpen();
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
		currentBars = renderer.refreshVisibleBars();
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
			if(lastLayout == R.layout.chrobars_general_settings)
				break;
			if(atLeastOneCheckBoxChecked()) {
				settingsDrawer.close();
				settingsLayoutContainer.removeAllViews();
				lastLayout = R.layout.chrobars_general_settings;
				getLayoutInflater().inflate(lastLayout, (ViewGroup) settingsLayoutContainer);
				processTouchableUIElements();
				settingsDrawer.animateOpen();
			}
			else
				noneChecked.show();
			break;
		case R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton:
			if(lastLayout == R.layout.chrobars_settings)
				break;
			settingsDrawer.close();
			settingsLayoutContainer.removeAllViews();
			lastLayout = R.layout.chrobars_settings;
			getLayoutInflater().inflate(lastLayout, (ViewGroup) settingsLayoutContainer);
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
			//TODO make this per bar
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

		if(atLeastOneCheckBoxChecked() ||
			lastLayout == R.layout.chrobars_general_settings) {
			//save the layout to the settings file so we
			// know what settings screen the user last saw
			settings.setPrefValue("settingsActivityLayout", lastLayout);
			finish();
		}
		else
			noneChecked.show();
	}
	
	/**
	 * 
	 */
	private void processTouchableUIElements() {
		
		ArrayList<View> touchables = settingsLayoutContainer.getTouchables();
		
//		System.out.println("Processing UI elements...");
		
		//Nothing to check
		if(touchables == null)
			return;
		else if(touchables.isEmpty())
			return;
		
		checkBoxes.clear(); toggles.clear(); buttons.clear();
		
		for(View touchable : touchables) {
			
//			System.out.println("Examining " + touchable + "...");
			
			if(touchable instanceof CheckBox) {
//				System.out.println("Processing " + touchable + " as CheckBox.");
				checkBoxes.add((CheckBox)touchable);
			}
			else if(touchable instanceof ToggleButton) {
//				System.out.println("Processing " + touchable + " as ToggleButton.");
				toggles.add((ToggleButton)touchable);
			}
			else if(touchable instanceof Button) {
//				System.out.println("Processing " + touchable + " as Button.");
				buttons.add((Button)touchable);
			}
			
			touchable.setOnClickListener(this);
		}
		
		//Only do this if we are in general settings.
		if(lastLayout == R.layout.chrobars_general_settings) {
			
			sliders.clear();
			
			SeekBar precision = (SeekBar) findViewById(R.id.chrobars_settings_general_slider_motionPrecision);
			precision.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener (){
	
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
															 boolean fromUser) {
				}
	
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					System.out.println("User currently adjusting motion precision...");
				}
	
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					System.out.println("Slider " + seekBar + " set to " + seekBar.getProgress() + "\nSaving...");
					saveSliders();
				}
				
			});
			
			precision.setProgress(settings.getPrecision());
			
			sliders.add(precision);
		}
		
		//System.out.println("Touchables found:\n" + checkBoxes + "\n" + sliders + "\n" + toggles + "\n" + buttons);
		
		//Set onClick listener for layout switcher buttons
		{
			Button settingsLayoutSwitcher = (Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsGeneralHandleButton);
			settingsLayoutSwitcher.setOnClickListener(this);
			buttons.add(settingsLayoutSwitcher);
			settingsLayoutSwitcher = (Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton);
			settingsLayoutSwitcher.setOnClickListener(this);
			buttons.add(settingsLayoutSwitcher);
		}
		
		checkCheckBoxes();
		
		for(ToggleButton tB : toggles) {
			
			System.out.println("Setting toggle " + tB);
			
			switch(tB.getId()) {
			
			case R.id.chrobars_settings_general_tglToggle3D:
				tB.setChecked(settings.isThreeD());
			}
		}
	}
	
	/**
	 * 
	 */
	private void checkCheckBoxes() {
		
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
				break;
			case R.id.chrobars_settings_general_chkbxDispNumbers:
				box.setChecked(settings.isDisplayNumbers());
			}	
		}
	}
	
	/**
	 * Updates the slider values in the preferences file.
	 */
	private void saveSliders() {

		for(SeekBar slider : sliders) {
			
			switch(slider.getId()) {
			case R.id.chrobars_settings_general_slider_motionPrecision:
				System.out.println("Save precision slider " + slider + ", value " + slider.getProgress() + ".");
				settings.setPrefValue("precision", slider.getProgress());
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
