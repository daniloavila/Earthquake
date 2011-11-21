package com.example.android.earthquake.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ParseException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.earthquake.R;
import com.example.android.earthquake.bean.Quake;

public class EarthquakeActivity extends Activity {
	
	private List<Quake> quakes;
	private ArrayAdapter<Quake> aa;
	private ListView listView ;
	private Quake selectedQuake;
	
	private int minMag = 0;
	private boolean autoUpdate = false;
	private int updateFreq = 0;
	
	static final private int MENU_UPDATE = Menu.FIRST;
	static final private int PREFERENCES = Menu.FIRST + 1;
	
	static final private int SHOW_PREFERENCES = 1;
	static final private int QUAKE_DIALOG = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        quakes = new ArrayList<Quake>();
        aa = new ArrayAdapter<Quake>(this, android.R.layout.simple_list_item_1, quakes);
        
        listView = (ListView) findViewById(R.id.earthquakeListView);
        listView.setAdapter(aa);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView _av, View _v, int _index,long arg3) {
				selectedQuake = quakes.get(_index);
				showDialog(QUAKE_DIALOG);
			}
        });
		
		updateFromPreferences();
        refreshQuakes();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_update);
    	menu.add(0, PREFERENCES, Menu.NONE, R.string.menu_preferences);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	switch (item.getItemId()) {
		case MENU_UPDATE:
			refreshQuakes();
			return true;
		case PREFERENCES:
			Intent intent = new Intent(this, Preferences.class);
			startActivityForResult(intent, SHOW_PREFERENCES);
			return true;
		}
    	
    	return false;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if(id == QUAKE_DIALOG){
    		LayoutInflater li = LayoutInflater.from(this);
    		View view = li.inflate(R.layout.quakedetails, null);
    		
    		AlertDialog.Builder quakeDialog = new AlertDialog.Builder(this);
    		quakeDialog.setTitle("QuakeTIme");
    		quakeDialog.setView(view);
    		
    		return quakeDialog.create();
    	}
    	
    	return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	if(id == QUAKE_DIALOG){
    		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    		String dateString = sdf.format(selectedQuake.getDate());
			String quakeText = "Magnitude " + selectedQuake.getMagnitude() + "\n" + selectedQuake.getDetails() + "\n" + selectedQuake.getLink();
			AlertDialog quakeDialog = (AlertDialog) dialog;
			quakeDialog.setTitle(dateString);
			TextView tv = (TextView) quakeDialog.findViewById(R.id.earthquakeTextView);
			tv.setText(quakeText);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == SHOW_PREFERENCES){
    		if(resultCode == Activity.RESULT_OK){
    			updateFromPreferences();
    			refreshQuakes();
    		}
    	}
    }
    
    private void updateFromPreferences(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		autoUpdate = prefs.getBoolean(Preferences.PREFS_AUTO_UPDATE, false);
		
		int minMagIndex = prefs.getInt(Preferences.PREFS_MIN_MAG, 0);
		if(minMagIndex <0 ){
			minMagIndex = 0;
		}
		
		int freqIndex = prefs.getInt(Preferences.PREFS_UPDATE_FREQ, 0);
		if(freqIndex <0 ){
			freqIndex = 0;
		}
		
		int[] minMagValues = getResources().getIntArray(R.array.mag_values);
		int[] updateFreqValues = getResources().getIntArray(R.array.update_freq_values);
		
		minMag = minMagValues[minMagIndex];
		updateFreq = updateFreqValues[freqIndex];
	}
    
    private void refreshQuakes(){
    	
    	String feed = getString(R.string.feed);
    	try {
			URL url = new URL(feed);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			int response = httpConnection.getResponseCode();
			
			if(response == HttpURLConnection.HTTP_OK){
				InputStream in = httpConnection.getInputStream();
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				
				Document dom = db.parse(in);
				Element docEle = dom.getDocumentElement();
				
				quakes.clear();
				
				NodeList nl = docEle.getElementsByTagName("entry");
				
				if( nl != null && nl.getLength() > 0 ){
					for(int i = 0; i < nl.getLength(); i++){
						Element entry = (Element) nl.item(i);
						Element title = (Element) entry.getElementsByTagName("title").item(0);
						Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
						Element when = (Element) entry.getElementsByTagName("updated").item(0);
						Element link = (Element) entry.getElementsByTagName("link").item(0);
						
						String details = title.getFirstChild().getNodeValue();
						String hostname = "http://earthquake.usgs.gov";
						String linkString = hostname + link.getAttribute("href");
						
						String point = g.getFirstChild().getNodeValue();
						String dt = when.getFirstChild().getNodeValue();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
						Date qdate = new GregorianCalendar(0, 0, 0).getTime();
						
						try {
							qdate = sdf.parse(dt);
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (java.text.ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						String[] location = point.split(" ");
						Location l = new Location("dummyGPS");
						l.setLatitude(Double.parseDouble(location[0]));
						l.setLongitude(Double.parseDouble(location[1]));
						
						String magnitudeString = details.split(" ")[1];
						int end = magnitudeString.length() - 1;
						double magnitude = Double.parseDouble(magnitudeString.substring(0, end));
						details = details.split(",")[1].trim();
						Quake quake = new Quake(details, linkString, qdate, l, magnitude);
						// Process a newly found earthquake 
						addNewQuake(quake);
					}
				}
				
				
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
    }

	private void addNewQuake(Quake quake) {
		if(quake.getMagnitude() >= minMag){
			quakes.add(quake);
			aa.notifyDataSetChanged();
		}
		
	}
}