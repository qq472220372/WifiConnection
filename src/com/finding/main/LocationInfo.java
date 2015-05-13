package com.finding.main;

public class LocationInfo {

	private String Latitude;
	private String Longitude;
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

	public LocationInfo(String latitude, String longitude, String name,
			String homeAddress, String phoneNumber,double distance) {
		super();
		Latitude = latitude;
		Longitude = longitude;
		this.name = name;
		this.homeAddress = homeAddress;
		this.phoneNumber = phoneNumber;
		this.distance = distance;
	}

	public String getLatitude() {
		return Latitude;
	}

	public void setLatitude(String latitude) {
		Latitude = latitude;
	}

	public String getLongitude() {
		return Longitude;
	}

	public void setLongitude(String longitude) {
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
