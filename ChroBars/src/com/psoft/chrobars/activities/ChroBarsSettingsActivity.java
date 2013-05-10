package com.psoft.chrobars.activities;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.psoft.chrobars.ChroBar;
import com.psoft.chrobars.R;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.settings.ChroBarsSettings;
import com.psoft.chrobars.settings.ColorPickerDialog;
import com.psoft.chrobars.settings.ColorPickerDialog.OnColorChangedListener;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * 
 * @author jhyry
 *
 */
@SuppressLint("ShowToast")
public class ChroBarsSettingsActivity extends Activity
									  implements OnClickListener,
									  OnItemSelectedListener, OnSeekBarChangeListener {
	
	private static OnColorChangedListener listening;
	
	/*
	 * These ArrayLists store the UI elements that the user interacts with.
	 */
	private ArrayList<CheckBox> 		checkBoxes 	= new ArrayList<CheckBox>();
	private ArrayList<Button> 			buttons 	= new ArrayList<Button>();
	private ArrayList<SeekBar>			sliders 	= new ArrayList<SeekBar>();
	private ArrayList<ToggleButton> 	toggles 	= new ArrayList<ToggleButton>();
	private ArrayList<Spinner>			spinners 	= new ArrayList<Spinner>();
	
	private static SlidingDrawer settingsDrawer;
	private static TableLayout settingsLayoutContainer;
	private static int lastLayout;
	
	private static Toast noneChecked, colorPickerInfo, forThreeD;
	private static AlertDialog resetConfirmDialog;
	
	private static ChroBarsSettings settings;
	private static BarsRenderer renderer;
	
	/**
	 * For this instance, get the currently drawn ChroBars.
	 */
	private static ChroBar[] currentBars = new ChroBar[ChroData._MAX_BARS_TO_DRAW];
	
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
		
		try {
			System.out.println("Requesting settings instance...");
			settings = ChroBarsActivity.requestSettingsObjectReference(this);
		}
		catch (Exception unknownEx) { ChroUtilities.printExDetails(unknownEx); }
		
		renderer = ChroSurface.getRenderer();
		
		if(settings == null)
			throw new NullPointerException("Critical: Failed to get settings instance.");
		
		settingsDrawer = (SlidingDrawer)findViewById(R.id.chrobars_settings_slidingDrawer);
		settingsLayoutContainer = (TableLayout) settingsDrawer.getContent();
		
		lastLayout = settings.getSettingsActivityLayout();
		getLayoutInflater().inflate(lastLayout, (ViewGroup) settingsLayoutContainer);

		currentBars = renderer.refreshVisibleBars();
		processTouchableUIElements();
		
		noneChecked = Toast.makeText(this, R.string.settings_bars_toastMessage_noneChecked, Toast.LENGTH_SHORT);
		colorPickerInfo = Toast.makeText(this, R.string.settings_bars_toastMessage_colorPickerInfo, Toast.LENGTH_LONG);
		forThreeD = Toast.makeText(this, R.string.settings_bars_toastMessage_for3D, Toast.LENGTH_LONG);
		
		AlertDialog.Builder resetDialogBuilder = new AlertDialog.Builder(this);
		
		resetDialogBuilder.setTitle(R.string.settings_general_reset_confirmTitle)
						  .setMessage(R.string.settings_general_reset_confirmMessage)
						  .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								settings.resetToDefaults();
								renderer.reloadSettings();
								processTouchableUIElements();
							}
						  })
						  .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						  });
		resetConfirmDialog = resetDialogBuilder.create();
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
	
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 
	 */
	@Override
	public void onClick(View v) {
		
		if(v instanceof CheckBox) {
			System.out.println("CheckBox " + v + " pressed.");
			setChroBarVisibility((CheckBox)v);
		}
		else if(v instanceof ToggleButton) {
			System.out.println("Toggle Button " + v + " pressed.");
			toggleSetting((ToggleButton)v);
		}
		else if(v instanceof Button) {
			System.out.println("Button " + v + " pressed.");
			pickColor((Button)v);
		}
	}
	
	/**
	 * 
	 */
	private void toggleSetting(ToggleButton tButton) {
		
		switch(tButton.getId()) {
		case R.id.chrobars_settings_general_tglToggle3D:
			tButton.setChecked(false);
			forThreeD.show();
			/* Only in Full/Pro versions
			settings.setPrefValue("threeD", tButton.isChecked());
			currentBars = renderer.refreshVisibleBars();
			for(ChroBar bar : currentBars)
				bar.updateEdgeColor(settings.getBarEdgeSetting());
			checkCheckBoxes();
			*/
			break;
		case R.id.chrobars_settings_general_tglToggleDynLighting:
			tButton.setChecked(false);
			forThreeD.show();
			/* Only in Full/Pro versions
			settings.setPrefValue("dynamicLighting", tButton.isChecked());
			*/
			break;
		case R.id.chrobars_settings_general_tglToggleTwelveHourTime:
			settings.setPrefValue("twelveHourTime", tButton.isChecked());
			break;
		case R.id.chrobars_settings_general_tglWireframe:
			settings.setPrefValue("wireframe", tButton.isChecked());
			for(ChroBar bar : currentBars)
				bar.setWireframe(tButton.isChecked());
			break;
		}
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
		default:
			setDefaults(button);
		}
	}

	/**
	 * 
	 * @param button
	 */
	private void setDefaults(Button button) {
		switch(button.getId()) {
		case R.id.chrobars_settings_drawer_general_resetButton:
			resetConfirmDialog.show();
		default:
			unknownViewWarning(button);
		}
	}

	/**
	 * 
	 * @param v
	 */
	private void unknownViewWarning(View v) {
		System.err.println("Error: " + v + " is unknown.");
	}

	/**
	 * 
	 */
	private void pickBackgroundColor() {
		
		colorPickerInfo.show();
		
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
		
		colorPickerInfo.show();

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
								
								ChroUtilities.barColorChosen(colorInt);
								ChroUtilities.changeChroBarColor(bar, colorInt);
								
								settingsRef.setPrefValue(ChroUtilities.getChroBarColorVarString(bar), colorInt);
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
		
		switch(box.getId()) {
		case R.id.chrobars_settings_slidingDrawer_chkbxHours_numbers:
			currentBars[0].setDrawNumber(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[0].getBarType(), true, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxMinutes_numbers:
			currentBars[1].setDrawNumber(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[1].getBarType(), true, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxSeconds_numbers:
			currentBars[2].setDrawNumber(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[2].getBarType(), true, box.isChecked());
			return;
		case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds_numbers:
			currentBars[3].setDrawNumber(box.isChecked());
			settings.setVisibilityPrefValue(currentBars[3].getBarType(), true, box.isChecked());
			return;
			
		}
		
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
		
		checkBoxes.clear(); toggles.clear();
		buttons.clear(); sliders.clear(); spinners.clear();
		
		for(View touchable : touchables) {
			
//			DEBUG
//			System.out.println("Examining " + touchable + "...");
			
			if(touchable instanceof CheckBox) {
//				DEBUG
//				System.out.println("Processing " + touchable + " as CheckBox.");
				checkBoxes.add((CheckBox)touchable);
			}
			else if(touchable instanceof ToggleButton) {
//				DEBUG
//				System.out.println("Processing " + touchable + " as ToggleButton.");
//				System.out.println("Setting toggle " + touchable);
				
				switch(touchable.getId()) {
				
				case R.id.chrobars_settings_general_tglToggle3D:
					((CompoundButton) touchable).setChecked(settings.isThreeD());
					break;
				case R.id.chrobars_settings_general_tglToggleDynLighting:
					((CompoundButton) touchable).setChecked(settings.usesDynamicLighting());
					break;
				case R.id.chrobars_settings_general_tglToggleTwelveHourTime:
					((ToggleButton) touchable).setChecked(settings.usesTwelveHourTime());
					break;
				case R.id.chrobars_settings_general_tglWireframe:
					((ToggleButton) touchable).setChecked(settings.wireframeEnabled());
					break;
				}
				toggles.add((ToggleButton)touchable);
			}
			else if(touchable instanceof Button) {
//				DEBUG
//				System.out.println("Processing " + touchable + " as Button.");
				buttons.add((Button)touchable);
			}
			else if(touchable instanceof Spinner) {
//				DEBUG
//				System.out.println("Processing " + touchable + " as Spinner.");
				Spinner spinner = (Spinner) touchable;
				switch(spinner.getId()) {
				case R.id.chrobars_settings_general_cmbEdges:
					ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.settings_bars_edges_options, android.R.layout.simple_spinner_item);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(adapter);
					spinner.setSelection(settings.getBarEdgeSetting());
					break;
				}
				spinners.add(spinner);
			}
			
			if(touchable instanceof AdapterView)
				((AdapterView<?>) touchable).setOnItemSelectedListener(this);
			else
				touchable.setOnClickListener(this);
		}
		
		//Only do this if we are in chrobars settings.
		if(lastLayout == R.layout.chrobars_settings) {
			sliderSearch(settingsLayoutContainer);
			if(!sliders.isEmpty()) {
				for(SeekBar slider : sliders) {
					
//					DEBUG
//					System.out.println("Processing seekbar " + slider + "...");
					
					slider.setOnSeekBarChangeListener(this);
					
					switch(slider.getId()) {
					case R.id.chrobars_settings_general_slider_motionPrecision:
//						DEBUG
//						System.out.println("Load precision slider " + slider + ", value " + slider.getProgress() + ".");
						slider.setProgress(settings.getPrecision());
						break;
					case R.id.chrobars_settings_general_slider_barMargin:
//						DEBUG
//						System.out.println("Load bar margin slider " + slider + ", value " + slider.getProgress() + ".");
						slider.setProgress(settings.getBarMarginMultiplier());
						break;
					case R.id.chrobars_settings_general_slider_edgeMargin:
//						DEBUG
//						System.out.println("Load edge margin slider " + slider + ", value " + slider.getProgress() + ".");
						slider.setProgress(settings.getEdgeMarginMultiplier());
						break;
					}
				}
			}
			else
				System.out.println("No sliders found :(");
		}
		
//		DEBUG
//		System.out.println("Touchables found:\n" + checkBoxes + "\n" + sliders + "\n" + toggles + "\n" + buttons);
		
		//Set onClick listener for layout switcher buttons
		Button generalHandle = (Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsGeneralHandleButton);
		generalHandle.setOnClickListener(this);
		buttons.add(generalHandle);
		Button chroHandle = (Button)findViewById(R.id.chrobars_settings_slidingDrawer_chrobarsHandleButton);
		chroHandle.setOnClickListener(this);
		buttons.add(chroHandle);
		
//		DEBUG
		System.out.println("Button general text: " + generalHandle.getText() + " Button bars text: " + chroHandle.getText());
		System.out.println("Buttons added to the button store: " + generalHandle + " = Gneral settings button; " + chroHandle + " = ChroSettings button.");
		
		checkCheckBoxes();
	}
	
	/**
	 * Searchs the View heirarchy recursively for SeekBar objects.
	 * 
	 * @param container Recurse into container to look for children.
	 */
	private void sliderSearch(ViewGroup container) {

		int containerChildCount = container.getChildCount();

//		DEBUG
//		System.out.println("Current container: " + container);
//		System.out.println("Current visual children count: " + containerChildCount);
//		System.out.println("Current visual children:");
		
		//Search through the children of the current container looking for other containers or specific object types.
		for(containerChildCount--; containerChildCount >=0; containerChildCount--) {
			//Get the next child
			View child = container.getChildAt(containerChildCount);

//			DEBUG
//			System.out.println("Current visual child: " + child);
			
			//If the child is a container, and has children, recurse into that container.
			if(ViewGroup.class.isAssignableFrom(child.getClass()))
				sliderSearch((ViewGroup) child);

			//If we are not dealing with a container, check to see if it is what we are looking for.
			// Slider, button, checkbox, etc.
			else {
				if(SeekBar.class.isAssignableFrom(child.getClass())) {
//					DEBUG
//					System.out.println("Found seekbar " + child);
					
					sliders.add((SeekBar)child);
				}
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
				
			case R.id.chrobars_settings_slidingDrawer_chkbxHours_numbers:
				box.setChecked(currentBars[0].isNumberDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxMinutes_numbers:
				box.setChecked(currentBars[1].isNumberDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxSeconds_numbers:
				box.setChecked(currentBars[2].isNumberDrawn());
				break;
			case R.id.chrobars_settings_slidingDrawer_chkbxMilliseconds_numbers:
				box.setChecked(currentBars[3].isNumberDrawn());
			}	
		}
	}
	
	/**
	 * Updates a slider value in the preferences file.
	 */
	private void saveSlider(SeekBar slider) {
		
		switch(slider.getId()) {
		case R.id.chrobars_settings_general_slider_motionPrecision:
			System.out.println("Save precision slider " + slider + ", value " + slider.getProgress() + ".");
			settings.setPrefValue("precision", slider.getProgress());
			break;
		case R.id.chrobars_settings_general_slider_barMargin:
			System.out.println("Save bar margin slider " + slider + ", value " + slider.getProgress() + ".");
			settings.setPrefValue("barMargin", slider.getProgress());
			break;
		case R.id.chrobars_settings_general_slider_edgeMargin:
			System.out.println("Save edge margin slider " + slider + ", value " + slider.getProgress() + ".");
			settings.setPrefValue("edgeMargin", slider.getProgress());
			break;
		}
	}

	/**
	 * Prevent possible divby0 errors.
	 * @return
	 */
	private boolean atLeastOneCheckBoxChecked() {
		
		//If there aren't any checkboxes we shouldn't worry about this.
		// False since there are no checkboxes.
		if(checkBoxes.isEmpty())
			return false;
		
		byte checkedBoxes = 0;
		
		for(CheckBox check : checkBoxes)
			if(check.isChecked())
				++checkedBoxes;
		
		return (checkedBoxes > 0);
	}

	/**
	 * 
	 */
	@Override
	public void onItemSelected(AdapterView<?> spinner, View itemSelectedView, int spinnerSelectionPosition, long itemSelectedId) {

		switch(spinner.getId()) {
		case R.id.chrobars_settings_general_cmbEdges:
			//If the setting didn't change, don't mess with it.
			if(settings.getBarEdgeSetting() == spinnerSelectionPosition)
				return;
			
//			DEBUG
			System.out.println("Spinner " + spinner + " set to " + spinner.getItemAtPosition(spinnerSelectionPosition) + " at index " + spinnerSelectionPosition + "\nSaving...");
			
			//Save the selection then update the edge color for each bar.
			settings.setPrefValue("barEdgeSetting", spinnerSelectionPosition);
			
			for(ChroBar bar : renderer.refreshVisibleBars())
				bar.updateEdgeColor(spinnerSelectionPosition);
			
			break;
		}
	}

	/**
	 * 
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		//Do nothing
	}

	/**
	 * 
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		//Do nothing
	}
	
	/**
	 * 
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		System.out.println("User currently adjusting motion precision...");
	}
	
	/**
	 * 
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		System.out.println("Slider " + seekBar + " set to " + seekBar.getProgress() + "\nSaving...");
		saveSlider(seekBar);
	}
}
