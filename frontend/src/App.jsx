import React, { useState } from 'react';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import SearchPage from './pages/SearchPage';
import ComparePage from './pages/ComparePage';
import PlannerPage from './pages/PlannerPage';
import RecommendationsPage from './pages/RecommendationsPage';
import InsightsPage from './pages/InsightsPage';

export default function App() {
  const [activeTab, setActiveTab] = useState('explore');

  return (
    <div className="app-container">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <main className="main-content">
        {(activeTab === 'explore' || activeTab === 'search') && (
          <SearchPage />
        )}
        {activeTab === 'compare' && <ComparePage />}
        {activeTab === 'planner' && <PlannerPage />}
        {activeTab === 'recommendations' && <RecommendationsPage />}
        {activeTab === 'insights' && <InsightsPage />}
      </main>

      <Footer setActiveTab={setActiveTab} />
    </div>
  );
}
