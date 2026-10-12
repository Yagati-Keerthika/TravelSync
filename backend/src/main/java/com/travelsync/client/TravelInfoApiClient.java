package com.travelsync.client;

import com.travelsync.model.Destination;

public interface TravelInfoApiClient {
    Destination enrichDestination(Destination destination);
}
