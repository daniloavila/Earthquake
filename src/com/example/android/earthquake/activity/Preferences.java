package com.example.android.earthquake.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.example.android.earthquake.R;

public class Preferences extends Activity {
	
	public static final String PREFS_AUTO_UPDATE = "PREFS_AUTO_UPDATE";
	public static final String PREFS_MIN_MAG = "PREFS_MIN_MAG";
	public static final String PREFS_UPDATE_FREQ = "PREFS_UPDATE_FREQ";

	private CheckBox autoUpdateCheckbox;
	private Spinner magSpinner;
	private Spinner updateFreqSpinner;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.preferences);
		
		autoUpdateCheckbox = (CheckBox) findViewById(R.id.checkbox_auto_update);
		magSpinner = (Spinner) findViewById(R.id.spinner_mag);
		updateFreqSpinner= (Spinner) findViewById(R.id.spinner_update_freq);
		
		populateSpinners();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		updateUIFromPrefs();
		
		Button okbButton = (Button) findViewById(R.id.button_ok);
		okbButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePreferences();
				Preferences.this.setResult(RESULT_OK);
				finish();
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.this.setResult(RESULT_CANCELED);
				finish();
			}
		});
		
	}
	
	protected void savePreferences() {
		Editor editor = prefs.edit();
		editor.putBoolean(PREFS_AUTO_UPDATE, autoUpdateCheckbox.isChecked());
		editor.putInt(PREFS_MIN_MAG, magSpinner.getSelectedItemPosition());
		editor.putInt(PREFS_UPDATE_FREQ, updateFreqSpinner.getSelectedItemPosition());
		editor.commit();
	}

	private void updateUIFromPrefs() {
		autoUpdateCheckbox.setChecked(prefs.getBoolean(PREFS_AUTO_UPDATE, false));
		magSpinner.setSelection(prefs.getInt(PREFS_MIN_MAG, 0));
		updateFreqSpinner.setSelection(prefs.getInt(PREFS_UPDATE_FREQ, 0));
	}

	private void populateSpinners() {
		ArrayAdapter<CharSequence> fadapter;
		fadapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options, android.R.layout.simple_spinner_item);
		fadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateFreqSpinner.setAdapter(fadapter);
		
		ArrayAdapter<CharSequence> magapter;
		magapter = ArrayAdapter.createFromResource(this, R.array.mag_options, android.R.layout.simple_spinner_item);
		magapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		magSpinner.setAdapter(magapter);
	}
	
}
