package com.travelsync.client;

import com.travelsync.model.Place;
import java.util.List;

public interface PlacesApiClient {
    List<Place> getNearbyPlaces(double latitude, double longitude, int radiusMeters);
}
