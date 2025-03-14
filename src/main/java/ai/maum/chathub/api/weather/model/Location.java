package ai.maum.chathub.api.weather.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Location {
    private float longitude;
    private float latitude;

    public Location() {
    }

    public Location(float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
