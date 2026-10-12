import React from 'react';
import { Compass, Search, MapPin, GitCompare, Calendar, Sparkles } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab }) {
  const navItems = [
    { id: 'explore', label: 'Explore', icon: MapPin },
    { id: 'compare', label: 'Compare', icon: GitCompare },
    { id: 'planner', label: 'Trip Planner', icon: Calendar },
    { id: 'recommendations', label: 'For you', icon: Sparkles },
  ];

  return (
    <header className="navbar travel-navbar">
      <div className="nav-inner">
        <button
          className="nav-brand"
          type="button"
          aria-label="TravelSync home"
          onClick={() => setActiveTab('explore')}
        >
          <div className="nav-brand-icon">
            <Compass size={22} />
          </div>
          <div>
            <span className="brand-travel">TRAVEL</span>
            <span className="brand-sync">SYNC</span>
          </div>
        </button>

        <nav aria-label="Main navigation">
          <ul className="nav-links">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <li key={item.id}>
                  <button
                    className={`nav-link-btn ${isActive ? 'active' : ''}`}
                    type="button"
                    onClick={() => setActiveTab(item.id)}
                    aria-current={isActive ? 'page' : undefined}
                  >
                    <Icon size={16} />
                    <span>{item.label}</span>
                  </button>
                </li>
              );
            })}
          </ul>
        </nav>

        <button className="nav-plan-button" type="button" onClick={() => setActiveTab('explore')}>
          <Search size={16} />
          <span>Search destinations</span>
        </button>
      </div>
    </header>
  );
}
