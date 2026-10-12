package com.travelsync.model;

import java.util.ArrayList;
import java.util.List;

public class Destination {
    private String name;
    private String country;
    private String countryCode;
    private String region;
    private double latitude;
    private double longitude;
    private String description;
    private String extract;
    private String imageUrl;
    private String timezone;
    private Long population;
    private Double elevation;
    private List<String> categories;
    private List<Place> places;
    private WeatherInfo weather;

    public Destination() {
        this.categories = new ArrayList<>();
        this.places = new ArrayList<>();
    }

    public Destination(String name, String country, double latitude, double longitude) {
        this();
        this.name = name;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public WeatherInfo getWeather() {
        return weather;
    }

    public void setWeather(WeatherInfo weather) {
        this.weather = weather;
    }

    public static class WeatherInfo {
        private double temperature;
        private double windSpeed;
        private int weatherCode;
        private String condition;

        public WeatherInfo() {}

        public WeatherInfo(double temperature, double windSpeed, int weatherCode, String condition) {
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.weatherCode = weatherCode;
            this.condition = condition;
        }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
    }
}
