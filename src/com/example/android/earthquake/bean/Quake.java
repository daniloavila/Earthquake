package com.example.android.earthquake.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;

public class Quake {
	
	private String details;
	private String link;
	private Date date;
	private Location location;
	private double magnitude;

	public Quake(String details, String link, Date date, Location location, double magnitude) {
		this.details = details;
		this.link = link;
		this.date = date;
		this.location = location;
		this.magnitude = magnitude;
	}
	
	@Override
	public String toString(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date) + " : " + magnitude + " " + details;
	}

	public double getMagnitude() {
		return magnitude;
	}
	
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getDetails() {
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
}
