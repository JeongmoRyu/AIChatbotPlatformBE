package ai.maum.chathub.api.weather.model;

import java.util.Map;

public class WeatherInfo {
    private Location location;
    private Map<String, String> weatherInfo;

    public WeatherInfo() {
    }

    public WeatherInfo(Location location, Map<String, String> weatherInfo) {
        this.location = location;
        this.weatherInfo = weatherInfo;
    }
}
