import React, { useState } from 'react';
import { GitCompare, MapPin, Sparkles } from 'lucide-react';
import TravelApi from '../services/api';

export default function ComparePage() {
  const [dest1, setDest1] = useState('Manali');
  const [dest2, setDest2] = useState('Shimla');
  const [loading, setLoading] = useState(false);
  const [comparison, setComparison] = useState(null);

  const handleCompareDestinations = async () => {
    if (!dest1.trim() || !dest2.trim()) return;
    setLoading(true);
    try {
      const res = await TravelApi.compareDestinations(dest1.trim(), dest2.trim());
      setComparison(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="compare-page">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.4rem' }}>
          Compare destinations
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Compare two places side by side, see what they have in common, and choose the trip that suits you.
        </p>
      </div>

      {/* Destination Comparison Section */}
      <div className="card" style={{ marginBottom: '2.5rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <GitCompare size={18} color="var(--primary)" />
          <span>Choose two places</span>
        </h2>
        <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
          Compare each place’s overview, travel styles and distance. The shared phrase is a text match from the descriptions, not a judgement that the places are alike.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
          <div>
            <label style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: '0.3rem' }}>
              Destination 1:
            </label>
            <input
              type="text"
              value={dest1}
              onChange={(e) => setDest1(e.target.value)}
              placeholder="e.g. Manali, Tokyo, Paris..."
              style={{ width: '100%', padding: '0.65rem 1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
            />
          </div>

          <div>
            <label style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: '0.3rem' }}>
              Destination 2:
            </label>
            <input
              type="text"
              value={dest2}
              onChange={(e) => setDest2(e.target.value)}
              placeholder="e.g. Shimla, Kyoto, Rome..."
              style={{ width: '100%', padding: '0.65rem 1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
            />
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Quick pairs:</span>
          {[
            ['Manali', 'Shimla'],
            ['Tokyo', 'Kyoto'],
            ['Paris', 'Rome'],
            ['Mumbai', 'Goa'],
          ].map(([c1, c2], idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => { setDest1(c1); setDest2(c2); }}
              style={{ fontSize: '0.78rem', background: '#f1f5f9', padding: '0.2rem 0.6rem', borderRadius: '9999px', border: '1px solid #e2e8f0' }}
            >
              {c1} vs {c2}
            </button>
          ))}
        </div>

        <button className="btn-primary" onClick={handleCompareDestinations} disabled={loading} style={{ marginBottom: '1.5rem' }}>
          {loading ? <span className="spinner" style={{ width: '16px', height: '16px' }} /> : <GitCompare size={16} />}
          <span>{loading ? 'Comparing places...' : 'Compare destinations'}</span>
        </button>

        {comparison && (
          <div style={{ background: '#f8fafc', padding: '1.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
            <div style={{ marginBottom: '1.25rem', paddingBottom: '1rem', borderBottom: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.4rem' }}>
                <Sparkles size={16} color="var(--accent)" />
                <strong style={{ fontSize: '1.05rem' }}>Longest shared phrase in their descriptions</strong>
              </div>
              {comparison.longestCommonSubstring.length > 0 ? (
                <div>
                  <div style={{ background: '#e0f2fe', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', border: '1px solid #bae6fd', color: '#0369a1', fontWeight: 600, fontSize: '0.95rem', marginBottom: '0.5rem' }}>
                    "{comparison.longestCommonSubstring.longestCommonSubstring}"
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Found with suffix-array + LCP text matching: <strong>{comparison.longestCommonSubstring.length} characters</strong>
                  </div>
                </div>
              ) : (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>No shared description phrase was found. Compare the travel styles and distance below instead.</div>
              )}
            </div>

            {/* Side-by-side cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem', marginBottom: '1.25rem' }}>
              <div style={{ background: 'white', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <h4 style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '0.3rem' }}>
                  {comparison.destination1.name}
                </h4>
                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                  {comparison.destination1.country}
                </div>
                <p style={{ fontSize: '0.85rem', color: '#475569', maxHeight: '120px', overflowY: 'auto' }}>
                  {comparison.destination1.extract || comparison.destination1.description}
                </p>
              </div>

              <div style={{ background: 'white', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <h4 style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--accent)', marginBottom: '0.3rem' }}>
                  {comparison.destination2.name}
                </h4>
                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                  {comparison.destination2.country}
                </div>
                <p style={{ fontSize: '0.85rem', color: '#475569', maxHeight: '120px', overflowY: 'auto' }}>
                  {comparison.destination2.extract || comparison.destination2.description}
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', color: '#475569', flexWrap: 'wrap', gap: '0.5rem' }}>
              <span>Distance between destinations: <strong>{comparison.geographicDistanceKm} km</strong></span>
              <span>Shared travel styles: <strong>{comparison.sharedCategories.join(', ') || 'No shared categories'}</strong></span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
