import React, { useEffect, useState } from 'react';
import { ArrowRight, Compass, ExternalLink, Hotel, MapPin, Navigation, Route, Search, Thermometer, Wind } from 'lucide-react';
import TravelApi from '../services/api';

const popularSearches = ['Manali', 'Goa', 'Tokyo', 'Paris', 'Bengaluru'];
const fallbackImage = 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1000&q=85';

export default function SearchPage({ initialQuery = '' }) {
  const [query, setQuery] = useState(initialQuery);
  const [searchedFor, setSearchedFor] = useState('');
  const [destinations, setDestinations] = useState([]);
  const [typoSuggestions, setTypoSuggestions] = useState([]);
  const [selectedDestination, setSelectedDestination] = useState(null);
  const [nearbyPlaces, setNearbyPlaces] = useState([]);
  const [originQuery, setOriginQuery] = useState('');
  const [routeOrigin, setRouteOrigin] = useState(null);
  const [originChoices, setOriginChoices] = useState([]);
  const [directions, setDirections] = useState(null);
  const [loading, setLoading] = useState(false);
  const [placesLoading, setPlacesLoading] = useState(false);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const [directionsLoading, setDirectionsLoading] = useState(false);
  const [error, setError] = useState('');
  const [suggestionsError, setSuggestionsError] = useState('');
  const [placesError, setPlacesError] = useState('');
  const [directionsError, setDirectionsError] = useState('');

  const searchDestinations = async (value) => {
    const destinationQuery = value.trim();
    if (!destinationQuery) {
      setError('Enter a city or destination to start exploring.');
      setDestinations([]);
      setSearchedFor('');
      return;
    }

    setQuery(destinationQuery);
    setSearchedFor(destinationQuery);
    setDestinations([]);
    setTypoSuggestions([]);
    setSelectedDestination(null);
    setNearbyPlaces([]);
    setError('');
    setSuggestionsError('');
    setPlacesError('');
    setLoading(true);

    try {
      const response = await TravelApi.searchDestinations(destinationQuery);
      const matches = response.data || [];
      setDestinations(matches);

      if (matches.length === 0) {
        setSuggestionsLoading(true);
        try {
          const suggestionResponse = await TravelApi.getTypoSuggestions(destinationQuery);
          setTypoSuggestions((suggestionResponse.data || []).slice(0, 3));
        } catch (suggestionRequestError) {
          setSuggestionsError(suggestionRequestError.message || 'Spelling suggestions are unavailable right now.');
        } finally {
          setSuggestionsLoading(false);
        }
      }
    } catch (searchError) {
      setDestinations([]);
      setError(searchError.message || 'We could not search destinations right now. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (initialQuery.trim()) {
      setQuery(initialQuery);
      searchDestinations(initialQuery);
    }
  }, [initialQuery]);

  const openDestination = async (destination) => {
    setSelectedDestination(destination);
    setPlacesLoading(true);
    setNearbyPlaces([]);
    setPlacesError('');
    setRouteOrigin(null);
    setOriginChoices([]);
    setDirections(null);
    setDirectionsError('');

    try {
      const response = await TravelApi.getDestinationPlaces(destination.latitude, destination.longitude);
      setNearbyPlaces(response.data || []);
    } catch (placesRequestError) {
      setPlacesError(placesRequestError.message || 'Nearby places are unavailable right now.');
    } finally {
      setPlacesLoading(false);
    }
  };

  const loadDirections = async (origin, destination = selectedDestination) => {
    if (!destination) return;

    setDirectionsLoading(true);
    setDirectionsError('');
    setOriginChoices([]);
    setRouteOrigin(origin);
    setDirections(null);

    try {
      const response = await TravelApi.getDrivingDirections(origin, destination);
      setDirections(response.data);
    } catch (routeError) {
      setDirectionsError(routeError.message || 'Driving directions are unavailable right now.');
    } finally {
      setDirectionsLoading(false);
    }
  };

  const handleFindDirections = async (event) => {
    event.preventDefault();
    const originName = originQuery.trim();
    if (!originName || !selectedDestination) return;

    setDirectionsLoading(true);
    setDirectionsError('');
    setOriginChoices([]);
    setDirections(null);
    setRouteOrigin(null);

    try {
      const response = await TravelApi.searchDestinations(originName);
      const origins = (response.data || []).filter(
        (origin) => origin.latitude !== selectedDestination.latitude
          || origin.longitude !== selectedDestination.longitude
      );

      if (origins.length === 0) {
        setDirectionsError(`We couldn't find a starting place called "${originName}". Check the spelling and try again.`);
        return;
      }

      if (origins.length > 1) {
        setOriginChoices(origins.slice(0, 5));
        setDirectionsLoading(false);
        return;
      }

      setDirectionsLoading(false);
      await loadDirections(origins[0]);
    } catch (routeError) {
      setDirectionsError(routeError.message || 'Could not look up your starting place. Please try again.');
    } finally {
      setDirectionsLoading(false);
    }
  };

  const getBookingSearchUrl = (placeName) => {
    const searchTerm = [placeName, selectedDestination?.name, selectedDestination?.country]
      .filter(Boolean)
      .join(', ');
    return `https://www.booking.com/searchresults.html?ss=${encodeURIComponent(searchTerm)}`;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    searchDestinations(query);
  };

  return (
    <div className="destination-search-page">
      <section className="destination-search-header">
        <div className="destination-search-heading">
          <span className="section-eyebrow"><Compass size={15} /> TRAVELSYNC DESTINATIONS</span>
          <h1>Where would you like to go?</h1>
          <p>Search a place and explore helpful details, local highlights, and nearby stays.</p>
        </div>

        <form className="destination-search-form" onSubmit={handleSubmit}>
          <MapPin size={20} aria-hidden="true" />
          <label className="sr-only" htmlFor="destination-results-search">City or destination</label>
          <input
            id="destination-results-search"
            type="search"
            autoComplete="off"
            placeholder="City, region, or destination"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <button type="submit" disabled={loading}>
            <Search size={17} />
            {loading ? 'Searching' : 'Search'}
          </button>
        </form>

        {!searchedFor && (
          <div className="destination-popular-searches">
            <span>Popular:</span>
            {popularSearches.map((destination) => (
              <button
                key={destination}
                type="button"
                onClick={() => searchDestinations(destination)}
                disabled={loading}
              >
                {destination}
              </button>
            ))}
          </div>
        )}
      </section>

      {error && <div className="destination-search-alert" role="alert">{error}</div>}

      {searchedFor && !error && (
        <section className="destination-results">
          <div className="destination-results-heading">
            <div>
              <span className="section-eyebrow">DESTINATION RESULTS</span>
              <h2>{loading ? 'Finding places…' : `Places matching “${searchedFor}”`}</h2>
              {!loading && destinations.length > 0 && (
                <p>{destinations.length} {destinations.length === 1 ? 'place' : 'places'} to explore</p>
              )}
            </div>
          </div>

          {loading ? (
            <div className="destination-search-loading" role="status">
              <span className="spinner" />
              <span>Searching destinations and gathering travel details…</span>
            </div>
          ) : destinations.length === 0 ? (
            <div className="destination-empty-state">
              <MapPin size={25} />
              <h3>No destinations found</h3>
              {suggestionsLoading ? (
                <p role="status">Checking for a spelling suggestion…</p>
              ) : typoSuggestions.length > 0 ? (
                <div className="destination-suggestions">
                  <p>Did you mean:</p>
                  {typoSuggestions.map((suggestion) => (
                    <button
                      key={suggestion.suggestedTarget}
                      type="button"
                      onClick={() => searchDestinations(suggestion.suggestedTarget)}
                    >
                      {suggestion.suggestedTarget}
                      <span>{Math.round(suggestion.similarity * 100)}% match</span>
                    </button>
                  ))}
                </div>
              ) : (
                <p>
                  {suggestionsError || 'Check the spelling or try a nearby city, region, or country.'}
                </p>
              )}
            </div>
          ) : (
            <div className="destination-results-grid">
              {destinations.map((destination, index) => (
                <article
                  className={`destination-result-card ${selectedDestination?.latitude === destination.latitude && selectedDestination?.longitude === destination.longitude ? 'selected' : ''}`}
                  key={`${destination.name}-${destination.latitude}-${destination.longitude}-${index}`}
                >
                  <div className="destination-result-image">
                    <img
                      src={destination.imageUrl || fallbackImage}
                      alt=""
                      loading="lazy"
                    />
                    {destination.weather && (
                      <span className="destination-weather-badge">
                        <Thermometer size={14} />
                        {Math.round(destination.weather.temperature)}°C
                      </span>
                    )}
                  </div>
                  <div className="destination-result-content">
                    <span className="destination-result-country">
                      {destination.region ? `${destination.region}, ` : ''}{destination.country}
                    </span>
                    <h3>{destination.name}</h3>
                    <p>
                      {destination.extract || destination.description || 'Discover local highlights and travel ideas for this destination.'}
                    </p>
                    {destination.categories?.length > 0 && (
                      <div className="destination-result-tags">
                        {destination.categories.slice(0, 3).map((category) => (
                          <span key={category}>{category}</span>
                        ))}
                      </div>
                    )}
                    <button
                      className="destination-result-link"
                      type="button"
                      onClick={() => openDestination(destination)}
                      aria-expanded={selectedDestination?.latitude === destination.latitude && selectedDestination?.longitude === destination.longitude}
                    >
                      Explore this place <ArrowRight size={16} />
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      )}

      {selectedDestination && (
        <section className="destination-detail-panel" aria-live="polite">
          <div className="destination-detail-cover">
            <img src={selectedDestination.imageUrl || fallbackImage} alt="" />
            <div className="destination-detail-cover-shade" />
            <div className="destination-detail-title">
              <span><MapPin size={16} /> {selectedDestination.region ? `${selectedDestination.region}, ` : ''}{selectedDestination.country}</span>
              <h2>{selectedDestination.name}</h2>
            </div>
          </div>
          <div className="destination-detail-content">
            <div className="destination-detail-main">
              <span className="section-eyebrow">A LITTLE ABOUT THIS PLACE</span>
              <p>
                {selectedDestination.extract || selectedDestination.description || `Explore ${selectedDestination.name}, discover nearby places, and start putting together a trip that suits you.`}
              </p>
              {selectedDestination.weather && (
                <div className="destination-detail-weather">
                  <span><Thermometer size={17} /> {Math.round(selectedDestination.weather.temperature)}°C</span>
                  <span><Wind size={17} /> {Math.round(selectedDestination.weather.windSpeed)} km/h wind</span>
                  <strong>{selectedDestination.weather.condition}</strong>
                </div>
              )}
              <form className="route-search-card" onSubmit={handleFindDirections}>
                <span className="section-eyebrow"><Route size={14} /> GETTING THERE</span>
                <h3>Driving directions</h3>
                <label htmlFor="route-origin">Where are you starting from?</label>
                <div className="route-origin-row">
                  <input
                    id="route-origin"
                    type="search"
                    value={originQuery}
                    onChange={(event) => setOriginQuery(event.target.value)}
                    placeholder="Enter a city or starting point"
                    required
                  />
                  <button type="submit" disabled={directionsLoading}>
                    <Navigation size={16} />
                    {directionsLoading ? 'Finding route…' : 'Get route'}
                  </button>
                </div>
                {directionsError && <p className="route-feedback route-error" role="alert">{directionsError}</p>}
                {originChoices.length > 0 && (
                  <div className="route-origin-choices">
                    <span>Choose your starting place:</span>
                    {originChoices.map((origin, index) => (
                      <button
                        key={`${origin.name}-${origin.latitude}-${origin.longitude}-${index}`}
                        type="button"
                        onClick={() => loadDirections(origin)}
                        disabled={directionsLoading}
                      >
                        {origin.name}, {origin.country}
                      </button>
                    ))}
                  </div>
                )}
                {directions && routeOrigin && (
                  <div className="route-result">
                    <div className="route-result-summary">
                      <span>{routeOrigin.name} <ArrowRight size={14} /> {selectedDestination.name}</span>
                      <strong>{directions.formattedDistance} · {directions.formattedDuration}</strong>
                    </div>
                    {directions.steps?.length > 0 && (
                      <ol className="route-steps">
                        {directions.steps.map((step, index) => (
                          <li key={`${step.instruction}-${index}`}>
                            <span>{index + 1}</span>
                            <div>
                              <strong>{step.instruction}</strong>
                              <small>{step.roadName || 'Road'} · {(step.distanceMeters >= 1000
                                ? `${(step.distanceMeters / 1000).toFixed(1)} km`
                                : `${Math.round(step.distanceMeters)} m`)}</small>
                            </div>
                          </li>
                        ))}
                      </ol>
                    )}
                    <a
                      className="route-map-link"
                      href={`https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${encodeURIComponent(`${routeOrigin.latitude},${routeOrigin.longitude};${selectedDestination.latitude},${selectedDestination.longitude}`)}`}
                      target="_blank"
                      rel="noreferrer"
                    >
                      Open route on map <ExternalLink size={14} />
                    </a>
                  </div>
                )}
              </form>
            </div>

            <div className="destination-nearby">
              <span className="section-eyebrow">AROUND {selectedDestination.name.toUpperCase()}</span>
              <h3>Things to see</h3>
              {placesLoading ? (
                <p className="destination-nearby-message">Finding places nearby…</p>
              ) : placesError ? (
                <p className="destination-nearby-message destination-nearby-error" role="alert">{placesError}</p>
              ) : nearbyPlaces.filter((place) => place.category !== 'Hotel').length > 0 ? (
                <ul>
                  {nearbyPlaces.filter((place) => place.category !== 'Hotel').slice(0, 6).map((place, index) => (
                    <li key={`${place.name}-${place.latitude}-${place.longitude}-${index}`}>
                      <span className="nearby-place-icon"><MapPin size={15} /></span>
                      <span>
                        <strong>{place.name}</strong>
                        <small>{place.category || 'Local place'}{place.distanceKm > 0 ? ` · ${place.distanceKm.toFixed(1)} km away` : ''}</small>
                      </span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="destination-nearby-message">No nearby places were found for this destination.</p>
              )}
            </div>
            <div className="destination-stays">
              <div className="destination-stays-heading">
                <div>
                  <span className="section-eyebrow"><Hotel size={14} /> PLACES TO STAY</span>
                  <h3>Nearby stays</h3>
                </div>
                <a href={getBookingSearchUrl()} target="_blank" rel="noreferrer">
                  Check availability <ExternalLink size={14} />
                </a>
              </div>
              <p className="destination-stays-note">
                Nearby accommodation listings from OpenStreetMap. Check current availability and prices with the booking provider.
              </p>
              {placesLoading ? (
                <p className="destination-nearby-message">Looking for nearby stays…</p>
              ) : nearbyPlaces.filter((place) => place.category === 'Hotel').length > 0 ? (
                <ul className="destination-stays-list">
                  {nearbyPlaces.filter((place) => place.category === 'Hotel').slice(0, 8).map((place, index) => (
                    <li key={`${place.name}-${place.latitude}-${place.longitude}-${index}`}>
                      <span className="nearby-place-icon"><Hotel size={15} /></span>
                      <span className="destination-stay-name">
                        <strong>{place.name}</strong>
                        <small>{place.distanceKm > 0 ? `${place.distanceKm.toFixed(1)} km from the destination` : 'Nearby accommodation'}</small>
                      </span>
                      <a
                        href={getBookingSearchUrl(place.name)}
                        target="_blank"
                        rel="noreferrer"
                        aria-label={`Search availability for ${place.name} on Booking.com`}
                      >
                        Book <ExternalLink size={13} />
                      </a>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="destination-nearby-message">
                  {placesError ? 'Nearby stays could not be loaded. You can still search all stays for this destination.' : 'No nearby stays found. Search accommodation for the destination instead.'}
                </p>
              )}
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
