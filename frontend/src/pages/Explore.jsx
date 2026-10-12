import React, { useState, useEffect } from 'react';
import { Search, MapPin, Wind, Thermometer, Compass, Sparkles, ExternalLink, Tag, Navigation, BookOpen } from 'lucide-react';
import TravelApi from '../services/api';

export default function Explore({ initialQuery }) {
  const [query, setQuery] = useState(initialQuery || 'Manali');
  const [loading, setLoading] = useState(false);
  const [destinations, setDestinations] = useState([]);
  const [selectedDest, setSelectedDest] = useState(null);
  const [places, setPlaces] = useState([]);
  const [placesTab, setPlacesTab] = useState('attractions');
  const [placesLoading, setPlacesLoading] = useState(false);
  const [error, setError] = useState(null);

  // In-destination algorithm search state
  const [keywordPattern, setKeywordPattern] = useState('scenic');
  const [algoType, setAlgoType] = useState('KMP');
  const [searchMatches, setSearchMatches] = useState(null);
  const [searchMeta, setSearchMeta] = useState(null);

  const handleSearch = async (searchTarget) => {
    const q = searchTarget || query;
    if (!q || !q.trim()) return;

    setLoading(true);
    setError(null);
    setSelectedDest(null);
    setPlaces([]);
    setSearchMatches(null);

    try {
      const res = await TravelApi.searchDestinations(q.trim());
      if (res.data && res.data.length > 0) {
        setDestinations(res.data);
        handleSelectDestination(res.data[0]);
      } else {
        setDestinations([]);
        setError('No destination found for "' + q + '". Try another city name.');
      }
    } catch (err) {
      setError(err.message || 'We couldn\'t retrieve travel information right now. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectDestination = async (dest) => {
    setSelectedDest(dest);
    setSearchMatches(null);
    setPlacesLoading(true);

    try {
      const placesRes = await TravelApi.getDestinationPlaces(dest.latitude, dest.longitude);
      setPlaces(placesRes.data || []);
    } catch (err) {
      console.warn('Could not fetch places:', err);
      setPlaces([]);
    } finally {
      setPlacesLoading(false);
    }
  };

  useEffect(() => {
    if (initialQuery) {
      setQuery(initialQuery);
      handleSearch(initialQuery);
    } else {
      handleSearch('Manali');
    }
  }, [initialQuery]);

  const runTextSearch = async () => {
    if (!selectedDest || !keywordPattern) return;
    const text = selectedDest.extract || selectedDest.description || '';

    try {
      let res;
      if (algoType === 'KMP') {
        res = await TravelApi.kmpSearch(text, keywordPattern);
      } else {
        res = await TravelApi.searchMultipleKeywords(text, [keywordPattern]);
      }

      if (algoType === 'KMP') {
        setSearchMatches(res.data || []);
        setSearchMeta(res.metadata);
      } else {
        const matches = (res.data && res.data[keywordPattern]) || [];
        setSearchMatches(matches);
        setSearchMeta(res.metadata);
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="explore-page">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.4rem' }}>
          Explore Real-World Destinations
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Queries Open-Meteo Geocoding, Wikipedia REST API, and OpenStreetMap in real-time. No mock data.
        </p>
      </div>

      {/* Search Header */}
      <form
        onSubmit={(e) => { e.preventDefault(); handleSearch(); }}
        style={{ display: 'flex', gap: '0.75rem', marginBottom: '2rem', maxWidth: '700px' }}
      >
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search any global destination (e.g. Manali, Tokyo, Mumbai, Paris, Vijayawada)..."
            style={{
              width: '100%',
              padding: '0.75rem 1rem 0.75rem 2.8rem',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border)',
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? <span className="spinner" style={{ width: '16px', height: '16px' }} /> : <Search size={16} />}
          <span>{loading ? 'Searching...' : 'Explore'}</span>
        </button>
      </form>

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', padding: '1rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {/* Main Grid: Left candidates + Right destination detail */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.75rem', alignItems: 'start' }}>
        {/* Candidate List */}
        <div>
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Matching Cities ({destinations.length})
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {destinations.map((dest, idx) => {
              const isSelected = selectedDest && selectedDest.name === dest.name && selectedDest.latitude === dest.latitude;
              return (
                <div
                  key={idx}
                  onClick={() => handleSelectDestination(dest)}
                  style={{
                    background: isSelected ? 'var(--primary-light)' : 'white',
                    border: isSelected ? '1px solid var(--primary)' : '1px solid var(--border)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '1rem',
                    cursor: 'pointer',
                    boxShadow: 'var(--shadow-sm)',
                    transition: 'all 0.2s ease',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                    <strong style={{ fontSize: '1.05rem', color: isSelected ? 'var(--primary)' : 'var(--text-main)' }}>
                      {dest.name}
                    </strong>
                    <span className="badge badge-gray">{dest.countryCode || dest.country}</span>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {dest.region ? `${dest.region}, ` : ''}{dest.country}
                  </div>
                  <div style={{ fontSize: '0.78rem', color: '#94a3b8', marginTop: '0.35rem' }}>
                    Coordinates: ({dest.latitude.toFixed(4)}, {dest.longitude.toFixed(4)})
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Destination Detail Card */}
        {selectedDest && (
          <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
            {/* Image Header */}
            <div style={{ position: 'relative', height: '260px', background: '#0f172a' }}>
              <img
                src={selectedDest.imageUrl || 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1200&q=80'}
                alt={selectedDest.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
              <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'linear-gradient(transparent, rgba(0,0,0,0.85))', padding: '1.5rem', color: 'white' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <MapPin size={18} color="var(--accent)" />
                  <h2 style={{ fontSize: '1.8rem', fontWeight: 800 }}>{selectedDest.name}</h2>
                  <span className="badge badge-orange" style={{ marginLeft: 'auto' }}>{selectedDest.country}</span>
                </div>
                <div style={{ fontSize: '0.85rem', color: '#cbd5e1' }}>
                  {selectedDest.region ? `${selectedDest.region} &bull; ` : ''}Lat: {selectedDest.latitude.toFixed(3)}, Lon: {selectedDest.longitude.toFixed(3)}
                </div>
              </div>
            </div>

            {/* Content Body */}
            <div style={{ padding: '1.5rem' }}>
              {/* Live Weather Widget */}
              {selectedDest.weather && (
                <div style={{ display: 'flex', gap: '1rem', background: '#f8fafc', padding: '0.85rem 1rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.25rem', border: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#0369a1' }}>
                    <Thermometer size={18} />
                    <strong style={{ fontSize: '1rem' }}>{selectedDest.weather.temperature}&deg;C</strong>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#475569' }}>
                    <Wind size={18} />
                    <span>{selectedDest.weather.windSpeed} km/h wind</span>
                  </div>
                  <div style={{ marginLeft: 'auto', fontWeight: 600, color: 'var(--accent)' }}>
                    {selectedDest.weather.condition}
                  </div>
                </div>
              )}

              {/* Categories */}
              {selectedDest.categories && selectedDest.categories.length > 0 && (
                <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', marginBottom: '1.25rem' }}>
                  {selectedDest.categories.map((c, i) => (
                    <span key={i} className="badge badge-blue">
                      <Tag size={12} />
                      <span>{c}</span>
                    </span>
                  ))}
                </div>
              )}

              {/* Encyclopedic Description */}
              <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <BookOpen size={16} color="var(--primary)" />
                  <span>Destination Overview (Wikipedia Live Extract)</span>
                </h4>
                <p style={{ color: '#334155', fontSize: '0.92rem', lineHeight: '1.7', background: '#fcfcfc', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid #f1f5f9' }}>
                  {selectedDest.extract || selectedDest.description}
                </p>
              </div>

              {/* Integrated String Algorithm Search Box */}
              <div style={{ background: '#f8fafc', padding: '1.25rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)', marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                  <Sparkles size={16} color="var(--accent)" />
                  <strong style={{ fontSize: '0.9rem' }}>Scan Destination Text via DSA Engines</strong>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <input
                    type="text"
                    value={keywordPattern}
                    onChange={(e) => setKeywordPattern(e.target.value)}
                    placeholder="Keyword to locate (e.g. scenic, temple, valley)..."
                    style={{ flex: 1, minWidth: '180px', padding: '0.45rem 0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
                  />
                  <select
                    value={algoType}
                    onChange={(e) => setAlgoType(e.target.value)}
                    style={{ padding: '0.45rem 0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)', background: 'white' }}
                  >
                    <option value="KMP">Knuth-Morris-Pratt (KMP)</option>
                    <option value="AC">Aho-Corasick</option>
                  </select>
                  <button type="button" className="btn-accent" onClick={runTextSearch} style={{ padding: '0.45rem 1rem', fontSize: '0.85rem' }}>
                    Run Algorithm
                  </button>
                </div>

                {searchMatches !== null && (
                  <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: '#1e293b' }}>
                    Found <strong>{searchMatches.length}</strong> occurrence(s) at text index/indices: [{searchMatches.join(', ')}]
                    {searchMeta && searchMeta.timeNanos && (
                      <span style={{ marginLeft: '0.5rem', color: 'var(--text-muted)' }}>
                        ({(searchMeta.timeNanos / 1000).toFixed(1)} &mu;s, Complexity: {searchMeta.complexity})
                      </span>
                    )}
                  </div>
                )}
              </div>

              {/* Nearby attractions and hotels */}
              <div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
                  <h4 style={{ fontSize: '0.95rem', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Navigation size={16} color="var(--primary)" />
                    <span>Nearby places</span>
                  </h4>
                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                    {[
                      { id: 'attractions', label: `Attractions (${places.filter((place) => place.category !== 'Hotel').length})` },
                      { id: 'hotels', label: `Hotels (${places.filter((place) => place.category === 'Hotel').length})` },
                    ].map((tab) => (
                      <button
                        key={tab.id}
                        type="button"
                        onClick={() => setPlacesTab(tab.id)}
                        className={placesTab === tab.id ? 'btn-primary' : 'btn-outline'}
                        style={{ padding: '0.4rem 0.7rem', fontSize: '0.8rem' }}
                      >
                        {tab.label}
                      </button>
                    ))}
                  </div>
                </div>
                {placesLoading ? (
                  <div className="loading-container">
                    <span className="spinner" />
                    <span>Loading nearby places and hotels...</span>
                  </div>
                ) : places.filter((place) => placesTab === 'hotels' ? place.category === 'Hotel' : place.category !== 'Hotel').length === 0 ? (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    No {placesTab === 'hotels' ? 'hotels' : 'attractions'} were found near this destination. Coverage depends on OpenStreetMap and Wikipedia data.
                  </p>
                ) : (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.75rem' }}>
                    {places
                      .filter((place) => placesTab === 'hotels' ? place.category === 'Hotel' : place.category !== 'Hotel')
                      .slice(0, 6)
                      .map((place, idx) => (
                      <div key={idx} style={{ background: '#ffffff', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', padding: '0.75rem' }}>
                        <strong style={{ fontSize: '0.88rem', display: 'block', marginBottom: '0.2rem' }}>{place.name}</strong>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                          <span className="badge badge-gray">{place.category}</span>
                          <span>{place.distanceKm.toFixed(1)} km from center</span>
                        </div>
                        <a
                          href={placesTab === 'hotels'
                            ? `https://www.google.com/travel/search?q=${encodeURIComponent(`${place.name} ${selectedDest.name}`)}`
                            : `https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}`}
                          target="_blank"
                          rel="noreferrer"
                          style={{ display: 'inline-block', marginTop: '0.55rem', fontSize: '0.8rem', color: 'var(--primary)', fontWeight: 600 }}
                        >
                          {placesTab === 'hotels' ? 'Check hotel availability' : 'Get directions'}
                        </a>
                      </div>
                    ))}
                  </div>
                )}
                <p style={{ marginTop: '0.75rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Distances shown are approximate straight-line distances from the destination center. Hotel availability and reservations open in Google Travel; bookings are not completed or paid for inside TravelSync.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
