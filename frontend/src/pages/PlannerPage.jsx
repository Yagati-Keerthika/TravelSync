import React, { useState } from 'react';
import { Route, MapPin, Calendar, CheckCircle2, ArrowRight, Clock, Gauge, DollarSign, Layers } from 'lucide-react';
import TravelApi from '../services/api';

export default function PlannerPage() {
  const [plannerTab, setPlannerTab] = useState('itinerary');

  // Bitmask DP Trip Planner state
  const [selectedCities, setSelectedCities] = useState(['Hyderabad', 'Chennai', 'Bengaluru', 'Goa']);
  const [newCityInput, setNewCityInput] = useState('');
  const [objective, setObjective] = useState('DISTANCE'); // DISTANCE or DURATION
  const [itineraryLoading, setItineraryLoading] = useState(false);
  const [itineraryResult, setItineraryResult] = useState(null);
  const [plannerStep, setPlannerStep] = useState(1);

  // Interval DP Activity state
  const defaultActivities = [
    { id: 'ACT-1', name: 'Sunrise Beach Walk', startMinutes: 0, endMinutes: 90, value: 45, category: 'Nature' },
    { id: 'ACT-2', name: 'Historic Temple & Heritage Tour', startMinutes: 60, endMinutes: 210, value: 85, category: 'Heritage' },
    { id: 'ACT-3', name: 'Culinary Street Food Walk', startMinutes: 220, endMinutes: 300, value: 60, category: 'Food' },
    { id: 'ACT-4', name: 'Art Gallery & Museum Visit', startMinutes: 180, endMinutes: 270, value: 50, category: 'Culture' },
    { id: 'ACT-5', name: 'Coastal Kayaking Adventure', startMinutes: 310, endMinutes: 420, value: 90, category: 'Adventure' },
    { id: 'ACT-6', name: 'Sunset Viewpoint Photography', startMinutes: 430, endMinutes: 520, value: 70, category: 'Scenery' },
  ];
  const [activities, setActivities] = useState(defaultActivities);
  const [scheduleResult, setScheduleResult] = useState(null);
  const [scheduleLoading, setScheduleLoading] = useState(false);

  // Tree DP Hierarchy state
  const [budget, setBudget] = useState(250);
  const [treeResult, setTreeResult] = useState(null);
  const [treeLoading, setTreeLoading] = useState(false);

  // Handlers for Bitmask DP Trip Planner
  const handleAddCity = () => {
    if (newCityInput.trim() && !selectedCities.includes(newCityInput.trim())) {
      setSelectedCities([...selectedCities, newCityInput.trim()]);
      setNewCityInput('');
    }
  };

  const handleRemoveCity = (city) => {
    setSelectedCities(selectedCities.filter((c) => c !== city));
  };

  const handleOptimizeTrip = async () => {
    if (selectedCities.length < 2) return;
    setItineraryLoading(true);
    setPlannerStep(3); // Fetching route information

    try {
      setPlannerStep(4); // Running Bitmask DP
      const res = await TravelApi.optimizeItinerary(selectedCities, objective);
      setItineraryResult(res.data);
      setPlannerStep(5); // Display result
    } catch (err) {
      console.error(err);
      alert('Optimization failed: ' + err.message);
      setPlannerStep(1);
    } finally {
      setItineraryLoading(false);
    }
  };

  // Handler for Interval DP
  const handleOptimizeActivities = async () => {
    setScheduleLoading(true);
    try {
      const res = await TravelApi.optimizeActivities(activities);
      setScheduleResult(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setScheduleLoading(false);
    }
  };

  // Handler for Tree DP
  const handleOptimizeTree = async () => {
    setTreeLoading(true);

    // Build hierarchical travel plan tree
    const tripTree = {
      id: 0,
      name: 'South India Exploration',
      type: 'TRIP',
      cost: 60,
      value: 40,
      children: [
        {
          id: 1,
          name: 'Day 1: Cultural City Tour',
          type: 'DAY',
          cost: 40,
          value: 30,
          children: [
            { id: 2, name: 'Heritage Palace Visit', type: 'ACTIVITY', cost: 25, value: 35, children: [] },
            { id: 3, name: 'Local Handicraft Bazaar', type: 'ACTIVITY', cost: 15, value: 20, children: [] },
          ],
        },
        {
          id: 4,
          name: 'Day 2: Coastal & Water Sports',
          type: 'DAY',
          cost: 50,
          value: 45,
          children: [
            { id: 5, name: 'Scuba Diving Session', type: 'ACTIVITY', cost: 45, value: 65, children: [] },
            { id: 6, name: 'Sunset Dolphin Cruise', type: 'ACTIVITY', cost: 20, value: 30, children: [] },
          ],
        },
        {
          id: 7,
          name: 'Day 3: Wildlife Sanctuary Safari',
          type: 'DAY',
          cost: 45,
          value: 35,
          children: [
            { id: 8, name: 'Jeep Jungle Safari', type: 'ACTIVITY', cost: 35, value: 50, children: [] },
          ],
        },
      ],
    };

    try {
      const res = await TravelApi.optimizeTravelPlanHierarchy({ plan: tripTree, budget: Number(budget) });
      setTreeResult(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setTreeLoading(false);
    }
  };

  return (
    <div className="planner-page">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.4rem' }}>
          Intelligent Trip Planner &amp; Dynamic Programming
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Organize your destinations, make time for the experiences you want, and keep your plans within budget.
        </p>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem', flexWrap: 'wrap', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' }}>
        <button
          className={`btn-outline ${plannerTab === 'itinerary' ? 'active' : ''}`}
          style={{ borderColor: plannerTab === 'itinerary' ? 'var(--primary)' : 'var(--border)', background: plannerTab === 'itinerary' ? 'var(--primary-light)' : 'white' }}
          onClick={() => setPlannerTab('itinerary')}
        >
          <Route size={16} color="var(--primary)" />
          <span>Route planner</span>
        </button>

        <button
          className={`btn-outline ${plannerTab === 'activities' ? 'active' : ''}`}
          style={{ borderColor: plannerTab === 'activities' ? 'var(--primary)' : 'var(--border)', background: plannerTab === 'activities' ? 'var(--primary-light)' : 'white' }}
          onClick={() => setPlannerTab('activities')}
        >
          <Calendar size={16} color="var(--primary)" />
          <span>Plan your day</span>
        </button>

        <button
          className={`btn-outline ${plannerTab === 'hierarchy' ? 'active' : ''}`}
          style={{ borderColor: plannerTab === 'hierarchy' ? 'var(--primary)' : 'var(--border)', background: plannerTab === 'hierarchy' ? 'var(--primary-light)' : 'white' }}
          onClick={() => setPlannerTab('hierarchy')}
        >
          <Layers size={16} color="var(--primary)" />
          <span>Trip budget</span>
        </button>
      </div>

      {/* Route planner */}
      {plannerTab === 'itinerary' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Plan a route through your destinations</h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Add your stops and choose whether distance or travel time matters most.
              </p>
            </div>
          </div>

          {/* 5-Step Progress Indicators */}
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.75rem', overflowX: 'auto', paddingBottom: '0.5rem' }}>
            {[
              { num: 1, title: 'Choose Cities' },
              { num: 2, title: 'Select Metric' },
              { num: 3, title: 'Get route details' },
              { num: 4, title: 'Arrange your stops' },
              { num: 5, title: 'View your route' },
            ].map((st) => (
              <div
                key={st.num}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  padding: '0.4rem 0.85rem',
                  borderRadius: '9999px',
                  fontSize: '0.82rem',
                  fontWeight: 600,
                  background: plannerStep >= st.num ? 'var(--primary-light)' : '#f1f5f9',
                  color: plannerStep >= st.num ? 'var(--primary)' : '#94a3b8',
                }}
              >
                <div style={{ width: '18px', height: '18px', borderRadius: '50%', background: plannerStep >= st.num ? 'var(--primary)' : '#cbd5e1', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.72rem' }}>
                  {st.num}
                </div>
                <span>{st.title}</span>
              </div>
            ))}
          </div>

          {/* Destination Selection Tags */}
          <div style={{ marginBottom: '1.25rem' }}>
            <label style={{ fontSize: '0.88rem', fontWeight: 600, display: 'block', marginBottom: '0.4rem' }}>
              Your stops ({selectedCities.length}):
            </label>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
              {selectedCities.map((city) => (
                <div
                  key={city}
                  style={{
                    background: '#e0f2fe',
                    color: '#0369a1',
                    padding: '0.35rem 0.85rem',
                    borderRadius: '9999px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.4rem',
                    fontWeight: 600,
                    fontSize: '0.88rem',
                  }}
                >
                  <MapPin size={14} />
                  <span>{city}</span>
                  <button
                    type="button"
                    onClick={() => handleRemoveCity(city)}
                    style={{ color: '#0284c7', fontWeight: 800, marginLeft: '0.2rem' }}
                  >
                    &times;
                  </button>
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', maxWidth: '420px' }}>
              <input
                type="text"
                value={newCityInput}
                onChange={(e) => setNewCityInput(e.target.value)}
                placeholder="Add another city (e.g. Mumbai, Delhi, Paris)..."
                style={{ flex: 1, padding: '0.55rem 0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
              />
              <button className="btn-outline" onClick={handleAddCity}>Add</button>
            </div>
          </div>

          {/* Objective Switcher */}
          <div style={{ display: 'flex', gap: '1.5rem', marginBottom: '1.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <label style={{ fontSize: '0.88rem', fontWeight: 600 }}>What matters most?</label>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', cursor: 'pointer', fontSize: '0.9rem' }}>
                <input
                  type="radio"
                  name="objective"
                  value="DISTANCE"
                  checked={objective === 'DISTANCE'}
                  onChange={() => setObjective('DISTANCE')}
                />
                <span>Shorter distance</span>
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', cursor: 'pointer', fontSize: '0.9rem' }}>
                <input
                  type="radio"
                  name="objective"
                  value="DURATION"
                  checked={objective === 'DURATION'}
                  onChange={() => setObjective('DURATION')}
                />
                <span>Less time travelling</span>
              </label>
            </div>
          </div>

          <button className="btn-accent" onClick={handleOptimizeTrip} disabled={itineraryLoading} style={{ padding: '0.75rem 2rem' }}>
            {itineraryLoading ? (
              <>
                <span className="spinner" style={{ width: '16px', height: '16px' }} />
                <span>Finding a good order for your stops...</span>
              </>
            ) : (
              <>
                <Route size={18} />
                <span>Plan my route</span>
              </>
            )}
          </button>

          {/* Itinerary Results Card */}
          {itineraryResult && (
            <div style={{ marginTop: '2rem', background: '#f8fafc', padding: '1.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--text-main)' }}>
                    Your suggested route
                  </h3>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    Based on live road distances and your selected preference.
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1.5rem' }}>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Distance</div>
                    <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--primary)' }}>
                      {itineraryResult.formattedTotalDistance}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Estimated Duration</div>
                    <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--accent)' }}>
                      {itineraryResult.formattedTotalDuration}
                    </div>
                  </div>
                </div>
              </div>

              {/* Visualized Ordered Permutation */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '1.5rem', background: 'white', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                {itineraryResult.orderedDestinations.map((dest, idx) => (
                  <React.Fragment key={idx}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700, color: idx === 0 ? 'var(--primary)' : 'var(--text-main)' }}>
                      <span style={{ width: '24px', height: '24px', borderRadius: '50%', background: idx === 0 ? 'var(--primary)' : '#475569', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.78rem' }}>
                        {idx + 1}
                      </span>
                      <span>{dest.name}</span>
                    </div>
                    {idx < itineraryResult.orderedDestinations.length - 1 && (
                      <ArrowRight size={16} color="#94a3b8" />
                    )}
                  </React.Fragment>
                ))}
              </div>

              {/* Legs Table */}
              <div>
                <h4 style={{ fontSize: '0.92rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Route Legs (Live Road Network Measurements)
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {itineraryResult.legs.map((leg, idx) => (
                    <div
                      key={idx}
                      style={{
                        background: 'white',
                        padding: '0.75rem 1rem',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '0.88rem',
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <strong>Leg {idx + 1}:</strong> {leg.origin} &rarr; {leg.destination}
                      </div>
                      <div style={{ display: 'flex', gap: '1rem', color: '#475569' }}>
                        <span>Distance: <strong>{leg.formattedDistance}</strong></span>
                        <span>Duration: <strong>{leg.formattedDuration}</strong></span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Daily activities */}
      {plannerTab === 'activities' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Make the most of your day</h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Find a comfortable order for the experiences on your list.
              </p>
            </div>
          </div>

          <p style={{ fontSize: '0.9rem', color: '#334155', marginBottom: '1.25rem' }}>
            Choose experiences that fit together, with enough time to enjoy each stop.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
            {activities.map((act) => (
              <div key={act.id} style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                  <strong style={{ fontSize: '0.92rem' }}>{act.name}</strong>
                  <span className="badge badge-gray">{act.category}</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Suggested time: {act.formatTimeRange ? act.formatTimeRange() : `${act.startMinutes}-${act.endMinutes} mins`}
                </div>
              </div>
            ))}
          </div>

          <button className="btn-primary" onClick={handleOptimizeActivities} disabled={scheduleLoading}>
            {scheduleLoading ? 'Putting your day together...' : 'Plan my day'}
          </button>

          {scheduleResult && (
            <div style={{ marginTop: '1.5rem', background: '#ecfdf5', padding: '1.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid #a7f3d0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <strong style={{ color: '#065f46', fontSize: '1.1rem' }}>
                  Your day plan ({scheduleResult.selectedActivities.length} activities)
                </strong>
                <span className="badge badge-green" style={{ fontSize: '0.85rem' }}>
                  Total enjoyment: {scheduleResult.totalValue}
                </span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {scheduleResult.selectedActivities.map((act, i) => (
                  <div key={i} style={{ background: 'white', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', border: '1px solid #d1fae5', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <CheckCircle2 size={16} color="#10b981" />
                      <strong>{act.name}</strong>
                    </div>
                    <span style={{ fontSize: '0.85rem', color: '#065f46', fontWeight: 600 }}>
                      {act.category}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Budget planning */}
      {plannerTab === 'hierarchy' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Plan a trip within your budget</h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Set a budget and explore a sample trip plan with costs in mind.
              </p>
            </div>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ fontSize: '0.88rem', fontWeight: 600, display: 'block', marginBottom: '0.4rem' }}>
              Trip budget ($): <strong>${budget}</strong>
            </label>
            <input
              type="range"
              min="100"
              max="350"
              step="10"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              style={{ width: '100%', maxWidth: '400px' }}
            />
          </div>

          <p style={{ margin: '0 0 1.5rem', color: 'var(--text-muted)', fontSize: '0.88rem', lineHeight: 1.65 }}>
            Your sample plan includes a cultural city tour, coastal activities, and a wildlife visit. Adjust the budget to see what fits.
          </p>

          <button className="btn-accent" onClick={handleOptimizeTree} disabled={treeLoading}>
            {treeLoading ? 'Finding a plan...' : `Build a plan for $${budget}`}
          </button>

          {treeResult && (
            <div style={{ marginTop: '1.5rem', background: '#fffbeb', padding: '1.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid #fde68a' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                <strong style={{ color: '#92400e', fontSize: '1.1rem' }}>
                  Suggested trip plan (enjoyment: {treeResult.maxValue})
                </strong>
                <span className="badge badge-orange">
                  Cost Spent: ${treeResult.totalCostUsed} / ${budget}
                </span>
              </div>
              <ul style={{ paddingLeft: '1.25rem', fontSize: '0.9rem', color: '#78350f' }}>
                {treeResult.selectedItems.map((item, idx) => (
                  <li key={idx} style={{ marginBottom: '0.35rem' }}>{item}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
