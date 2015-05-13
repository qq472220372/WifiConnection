package com.finding.main;

public class LocationInfo {

	private double Latitude;
	private double Longitude;
	private String name;
	private String homeAddress;
	private String phoneNumber;
	private double distance;
	
	public LocationInfo(){
		
	}

	public LocationInfo(String name, String homeAddress, String phoneNumber,double distance) {
		super();
		this.name = name;
		this.homeAddress = homeAddress;
		this.phoneNumber = phoneNumber;
		this.distance = distance;
	}

	public LocationInfo(double latitude, double longitude, String name,
			String homeAddress, String phoneNumber,double distance) {
		super();
		Latitude = latitude;
		Longitude = longitude;
		this.name = name;
		this.homeAddress = homeAddress;
		this.phoneNumber = phoneNumber;
		this.distance = distance;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double latitude) {
		Latitude = latitude;
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	
}
