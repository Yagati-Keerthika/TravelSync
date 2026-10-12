import React, { useState } from 'react';
import { Check } from 'lucide-react';
import TravelApi from '../services/api';

export default function RecommendationsPage() {
  const allFeatures = [
    { id: 'beach', label: 'Beach & Coastal' },
    { id: 'historical', label: 'Heritage & Temples' },
    { id: 'adventure', label: 'Mountain & Trekking' },
    { id: 'nature', label: 'Forest & Wildlife' },
    { id: 'food', label: 'Cuisine & Dining' },
    { id: 'nightlife', label: 'Nightlife & Social' },
    { id: 'relaxation', label: 'Scenic & Relaxation' },
    { id: 'shopping', label: 'Markets & Shopping' },
  ];

  const [selectedPrefs, setSelectedPrefs] = useState(['beach', 'relaxation']);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const togglePref = (id) => {
    if (selectedPrefs.includes(id)) {
      setSelectedPrefs(selectedPrefs.filter((p) => p !== id));
    } else {
      setSelectedPrefs([...selectedPrefs, id]);
    }
  };

  const handleFetchRecommendations = async () => {
    setLoading(true);
    try {
      const res = await TravelApi.getPersonalizedRecommendations(selectedPrefs);
      setResult(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="recommendations-page">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.4rem' }}>
          Find a trip that feels like you
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Tell us what you enjoy and we’ll find destinations that match your travel style.
        </p>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>
          What do you love doing on a trip?
        </h2>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
          Choose as many interests as you like. You can change them any time.
        </p>

        {/* Feature Pills */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
          {allFeatures.map((feat) => {
            const isSelected = selectedPrefs.includes(feat.id);
            return (
              <button
                key={feat.id}
                type="button"
                onClick={() => togglePref(feat.id)}
                style={{
                  padding: '0.75rem 1rem',
                  borderRadius: 'var(--radius-sm)',
                  border: isSelected ? '2px solid var(--primary)' : '1px solid var(--border)',
                  background: isSelected ? 'var(--primary-light)' : 'white',
                  color: isSelected ? 'var(--primary)' : 'var(--text-main)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                }}
              >
                <span>{feat.label}</span>
                {isSelected && <Check size={17} aria-label="Selected" />}
              </button>
            );
          })}
        </div>

        <div style={{ background: '#f2f5ef', color: 'var(--text-main)', padding: '1rem 1.25rem', borderRadius: 'var(--radius-sm)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
          <div>
            <div style={{ fontSize: '0.9rem', fontWeight: 700 }}>Ready to find your next trip?</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{selectedPrefs.length} interests selected</div>
          </div>
          <button className="btn-accent" onClick={handleFetchRecommendations} disabled={loading}>
            {loading ? 'Finding your trip ideas...' : 'Show me places'}
          </button>
        </div>

        {/* Results */}
        {result && (
          <div>
            <div style={{ display: 'flex', gap: '1.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
              <div style={{ background: '#f8fafc', padding: '0.85rem 1.25rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Places matching your interests</div>
                <strong style={{ fontSize: '1.2rem', color: 'var(--primary)' }}>{result.totalSupersetMatches}</strong>
                <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Based on the interests you selected</div>
              </div>
            </div>

            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>
              Ranked Recommended Destinations ({result.recommendations.length})
            </h3>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
              {result.recommendations.map((item, idx) => (
                <div key={idx} className="card" style={{ padding: '0', overflow: 'hidden' }}>
                  <div style={{ height: '140px', position: 'relative' }}>
                    <img
                      src={item.destination.imageUrl || 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80'}
                      alt={item.destination.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                    <div style={{ position: 'absolute', top: '0.75rem', right: '0.75rem', background: 'rgba(0,0,0,0.75)', color: 'white', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700 }}>
                      {item.matchPercentage} Match
                    </div>
                  </div>

                  <div style={{ padding: '1rem' }}>
                    <h4 style={{ fontSize: '1.15rem', fontWeight: 800, marginBottom: '0.2rem' }}>
                      {item.destination.name}
                    </h4>
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '0.75rem' }}>
                      {item.destination.country}
                    </div>

                    <div style={{ fontSize: '0.82rem', color: '#334155', marginBottom: '0.75rem' }}>
                      Matching: <strong>{item.matchingFeatures.join(', ') || 'General Discovery'}</strong>
                    </div>

                    <p style={{ fontSize: '0.82rem', color: '#64748b', maxHeight: '60px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {item.destination.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
