import React from 'react';
import { ArrowRight, Compass } from 'lucide-react';

export default function Footer({ setActiveTab }) {
  return (
    <footer className="footer">
      <div className="footer-inner travel-footer-inner">
        <div className="footer-top">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <Compass size={20} color="var(--primary)" />
              <strong style={{ fontSize: '1.1rem' }}>TravelSync</strong>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', maxWidth: '420px' }}>
              Find inspiring places, plan a trip that feels like yours, and enjoy a little more of the journey.
            </p>
          </div>
          <button className="footer-guide-link" type="button" onClick={() => setActiveTab('insights')}>
            How TravelSync works <ArrowRight size={16} />
          </button>
        </div>
        <div className="footer-bottom">
          <span>&copy; {new Date().getFullYear()} TravelSync. Go a little further.</span>
          <span>Made for the places you haven’t been yet.</span>
        </div>
      </div>
    </footer>
  );
}
