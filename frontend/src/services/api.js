const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

/**
 * Universal fetch wrapper for TravelSync REST endpoints
 */
async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json();

    if (!response.ok || data.success === false) {
      throw new Error(data.message || `HTTP ${response.status}: Request failed`);
    }

    return data;
  } catch (err) {
    if (err.name === 'TypeError' && err.message.includes('fetch')) {
      throw new Error('Could not connect to TravelSync backend server. Please verify it is running on port 8081.');
    }
    throw err;
  }
}

export const TravelApi = {
  // System Health
  getHello: () => request('/api/travel/hello'),

  // Destinations & Real Search
  searchDestinations: (query) => request(`/api/destinations/search?query=${encodeURIComponent(query)}`),
  getDestinationByName: (name) => request(`/api/destinations/${encodeURIComponent(name)}`),
  getDestinationPlaces: (latitude, longitude) => request(`/api/destinations/nearby-places?latitude=${encodeURIComponent(latitude)}&longitude=${encodeURIComponent(longitude)}`),
  getDrivingDirections: (origin, destination) => request(`/api/routes/directions?originLatitude=${encodeURIComponent(origin.latitude)}&originLongitude=${encodeURIComponent(origin.longitude)}&destinationLatitude=${encodeURIComponent(destination.latitude)}&destinationLongitude=${encodeURIComponent(destination.longitude)}`),

  // String Search Algorithms
  kmpSearch: (text, pattern) => request(`/api/search/kmp?text=${encodeURIComponent(text)}&pattern=${encodeURIComponent(pattern)}`),
  searchMultipleKeywords: (text, keywords) => request('/api/search/multiple', {
    method: 'POST',
    body: JSON.stringify({ text, keywords }),
  }),
  getTypoSuggestions: (query) => request(`/api/search/suggest?query=${encodeURIComponent(query)}`),
  searchBookingCode: (code) => request(`/api/bookings/search?code=${encodeURIComponent(code)}`),

  // Destination Comparisons (Suffix Array, LCP, Needleman-Wunsch, Smith-Waterman)
  compareDestinations: (dest1, dest2) => request('/api/compare/destinations', {
    method: 'POST',
    body: JSON.stringify({ destination1: dest1, destination2: dest2 }),
  }),
  compareDescriptions: (text1, text2) => request('/api/compare/descriptions', {
    method: 'POST',
    body: JSON.stringify({ text1, text2 }),
  }),

  // Advanced DP Optimizations
  optimizeItinerary: (destinations, objective = 'DISTANCE') => request('/api/itinerary/optimize', {
    method: 'POST',
    body: JSON.stringify({ destinations, objective }),
  }),
  optimizeActivities: (activities, maxSlots = 4) => request('/api/activities/optimize', {
    method: 'POST',
    body: JSON.stringify({ activities, maxSlots }),
  }),
  allocateActivities: (travelers, activities) => request('/api/activities/allocate', {
    method: 'POST',
    body: JSON.stringify({ travelers, activities }),
  }),
  optimizeTravelPlanHierarchy: (plan) => request('/api/travel-plan/optimize', {
    method: 'POST',
    body: JSON.stringify(plan),
  }),
  getPersonalizedRecommendations: (preferences) => request('/api/recommendations', {
    method: 'POST',
    body: JSON.stringify({ preferences }),
  }),
};

export default TravelApi;
