package com.travelsync.client;

import com.travelsync.model.Destination;
import java.util.List;

public interface GeocodingApiClient {
    List<Destination> searchDestinations(String query);
}
