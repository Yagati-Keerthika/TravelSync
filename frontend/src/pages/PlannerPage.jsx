import React, { useState } from 'react';
import { Route, MapPin, Calendar, CheckCircle2, ArrowRight, Layers, Plus, Trash2 } from 'lucide-react';
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
  const [activities, setActivities] = useState([]);
  const [scheduleResult, setScheduleResult] = useState(null);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [activityDraft, setActivityDraft] = useState({
    name: '', startTime: '08:00', endTime: '09:00', value: '50', category: 'Other',
  });
  const [activityError, setActivityError] = useState('');

  // Budget-based live nearby-place recommendations
  const [budget, setBudget] = useState('100');
  const [budgetCity, setBudgetCity] = useState('');
  const [budgetPlaces, setBudgetPlaces] = useState([]);
  const [budgetDestination, setBudgetDestination] = useState(null);
  const [budgetLoading, setBudgetLoading] = useState(false);
  const [budgetError, setBudgetError] = useState('');

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
    setActivityError('');
    if (activities.length === 0) {
      setActivityError('Add at least one activity before planning your day.');
      return;
    }
    setScheduleLoading(true);
    try {
      const res = await TravelApi.optimizeActivities(activities);
      setScheduleResult(res.data);
    } catch (err) {
      setActivityError(err.message || 'Could not plan these activities. Please try again.');
    } finally {
      setScheduleLoading(false);
    }
  };

  const handleAddActivity = (event) => {
    event.preventDefault();
    const name = activityDraft.name.trim();
    const [startHour, startMinute] = activityDraft.startTime.split(':').map(Number);
    const [endHour, endMinute] = activityDraft.endTime.split(':').map(Number);
    const startClockMinutes = startHour * 60 + startMinute;
    const endClockMinutes = endHour * 60 + endMinute;
    const value = Number(activityDraft.value);
    if (!name || !Number.isFinite(startClockMinutes) || !Number.isFinite(endClockMinutes)
      || endClockMinutes <= startClockMinutes
      || !Number.isFinite(value) || value < 0) {
      setActivityError('Enter an activity name, an end time later than the start time, and a non-negative priority.');
      return;
    }
    setActivities((current) => [...current, {
      id: `ACT-${Date.now()}`,
      name,
      startMinutes: startClockMinutes - 8 * 60,
      endMinutes: endClockMinutes - 8 * 60,
      value,
      category: activityDraft.category.trim() || 'Other',
    }]);
    setActivityDraft({ name: '', startTime: '08:00', endTime: '09:00', value: '50', category: 'Other' });
    setActivityError('');
    setScheduleResult(null);
  };

  const addSampleActivities = () => {
    setActivities((current) => current.length ? current : defaultActivities);
    setScheduleResult(null);
    setActivityError('');
  };

  const estimateVisitCost = (place) => {
    const category = `${place.category || ''} ${place.name || ''}`.toLowerCase();
    if (/beach|park|viewpoint|temple|church|monument|shrine/.test(category)) return 0;
    if (/museum|gallery|fort|palace/.test(category)) return 12;
    if (/zoo|aquarium|theme park|water park/.test(category)) return 25;
    return 8;
  };

  const handleBudgetSearch = async (event) => {
    event.preventDefault();
    const city = budgetCity.trim();
    if (!city) {
      setBudgetError('Enter a city to find nearby places that fit your budget.');
      return;
    }
    setBudgetLoading(true);
    setBudgetError('');
    setBudgetPlaces([]);
    setBudgetDestination(null);
    try {
      const search = await TravelApi.searchDestinations(city);
      const destination = (search.data || [])[0];
      if (!destination) {
        setBudgetError(`No destination found for "${city}". Try another city name.`);
        return;
      }
      setBudgetDestination(destination);
      const response = await TravelApi.getDestinationPlaces(destination.latitude, destination.longitude);
      const recommendations = (response.data || [])
        .filter((place) => place.category !== 'Hotel')
        .map((place) => ({ ...place, estimatedVisitCost: estimateVisitCost(place) }))
        .sort((a, b) => a.estimatedVisitCost - b.estimatedVisitCost);
      setBudgetPlaces(recommendations);
      if (recommendations.length === 0) {
        setBudgetError(`No nearby attractions were returned for ${destination.name}. Try another destination.`);
      }
    } catch (err) {
      setBudgetError(err.message || 'Could not find nearby places. Please try again.');
    } finally {
      setBudgetLoading(false);
    }
  };

  const budgetAmount = Number(budget) || 0;
  const budgetPlan = [];
  let remainingBudget = budgetAmount;
  budgetPlaces.forEach((place) => {
    if (place.estimatedVisitCost <= remainingBudget) {
      budgetPlan.push(place);
      remainingBudget -= place.estimatedVisitCost;
    }
  });
  const budgetPlanNames = new Set(budgetPlan.map((place) => place.name));
  const estimatedPlanSpend = budgetAmount - remainingBudget;
  const scheduledActivityIds = new Set((scheduleResult?.selectedActivities || []).map((activity) => activity.id));
  const skippedActivities = scheduleResult
    ? activities.filter((activity) => !scheduledActivityIds.has(activity.id))
    : [];

  const formatActivityTime = (minutesAfterEight) => {
    const minutesAfterMidnight = minutesAfterEight + 8 * 60;
    const hours = Math.floor(minutesAfterMidnight / 60);
    const minutes = minutesAfterMidnight % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
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
                Add multiple city stops and choose whether distance or travel time matters most. This arranges the order of your cities; use “Plan your day” to schedule activities within one day.
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
            Add your own places with clock start/end times, or load editable sample ideas. The planner uses weighted interval scheduling to choose the highest-priority set that does not overlap. Leave a gap between visits for travel time.
          </p>

          <form onSubmit={handleAddActivity} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(145px, 1fr))', gap: '0.65rem', alignItems: 'end', marginBottom: '1rem' }}>
            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              Activity name
              <input value={activityDraft.name} onChange={(e) => setActivityDraft({ ...activityDraft, name: e.target.value })} placeholder="e.g. Visit a museum" style={{ display: 'block', width: '100%', padding: '0.55rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.25rem' }} />
            </label>
            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              Start time
              <input type="time" value={activityDraft.startTime} onChange={(e) => setActivityDraft({ ...activityDraft, startTime: e.target.value })} required style={{ display: 'block', width: '100%', padding: '0.55rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.25rem' }} />
            </label>
            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              End time
              <input type="time" value={activityDraft.endTime} onChange={(e) => setActivityDraft({ ...activityDraft, endTime: e.target.value })} required style={{ display: 'block', width: '100%', padding: '0.55rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.25rem' }} />
            </label>
            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              Priority
              <input type="number" min="0" value={activityDraft.value} onChange={(e) => setActivityDraft({ ...activityDraft, value: e.target.value })} style={{ display: 'block', width: '100%', padding: '0.55rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.25rem' }} />
            </label>
            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              Category
              <input value={activityDraft.category} onChange={(e) => setActivityDraft({ ...activityDraft, category: e.target.value })} style={{ display: 'block', width: '100%', padding: '0.55rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.25rem' }} />
            </label>
            <button className="btn-outline" type="submit"><Plus size={16} /> Add activity</button>
          </form>
          {activityError && <p role="alert" style={{ color: '#b42318', margin: '0.5rem 0' }}>{activityError}</p>}
          <button className="btn-outline" type="button" onClick={addSampleActivities} style={{ marginBottom: '1rem' }}>
            Add sample ideas (editable)
          </button>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
            {activities.map((act) => (
              <div key={act.id} style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <strong style={{ fontSize: '0.92rem' }}>{act.name}</strong>
                  <span className="badge badge-gray">{act.category}</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  {formatActivityTime(act.startMinutes)}–{formatActivityTime(act.endMinutes)} · priority {act.value}
                </div>
                <button type="button" onClick={() => { setActivities(activities.filter((item) => item.id !== act.id)); setScheduleResult(null); }} aria-label={`Remove ${act.name}`} style={{ color: '#9b2c2c', fontSize: '0.78rem', marginTop: '0.5rem' }}>
                  <Trash2 size={13} /> Remove
                </button>
              </div>
            ))}
          </div>

          {activities.length === 0 && <p style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>Add your own activities or choose sample ideas, then plan your day.</p>}
          <button className="btn-primary" onClick={handleOptimizeActivities} disabled={scheduleLoading || activities.length === 0}>
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
                      <strong>{formatActivityTime(act.startMinutes)}–{formatActivityTime(act.endMinutes)} · {act.name}</strong>
                    </div>
                    <span style={{ fontSize: '0.85rem', color: '#065f46', fontWeight: 600 }}>
                      {act.category}
                    </span>
                  </div>
                ))}
              </div>
              {skippedActivities.length > 0 && (
                <div style={{ marginTop: '1rem', padding: '0.85rem 1rem', background: '#fff7ed', border: '1px solid #fed7aa', borderRadius: 'var(--radius-sm)', color: '#7c2d12' }}>
                  <strong>Not scheduled because of overlaps or lower combined priority:</strong>
                  <ul style={{ margin: '0.5rem 0 0', paddingLeft: '1.2rem' }}>
                    {skippedActivities.map((activity) => {
                      const conflicts = scheduleResult.selectedActivities.filter(
                        (selected) => activity.startMinutes < selected.endMinutes
                          && selected.startMinutes < activity.endMinutes,
                      );
                      return (
                        <li key={activity.id}>
                          {activity.name}
                          {conflicts.length > 0 && ` overlaps ${conflicts.map((conflict) => conflict.name).join(', ')}`}
                        </li>
                      );
                    })}
                  </ul>
                  <p style={{ margin: '0.5rem 0 0', fontSize: '0.82rem' }}>
                    If you want both activities, change their times so one ends before the next begins.
                  </p>
                </div>
              )}
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
                Choose any destination to see real nearby attractions, ranked by illustrative per-person visit estimates.
              </p>
            </div>
          </div>

          <form onSubmit={handleBudgetSearch} style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', alignItems: 'end', margin: '1.25rem 0' }}>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              Destination
              <input value={budgetCity} onChange={(e) => setBudgetCity(e.target.value)} placeholder="e.g. Goa, Manali, Paris" style={{ display: 'block', minWidth: '250px', padding: '0.65rem 0.8rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.3rem' }} />
            </label>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              Activity budget (USD)
              <input type="number" min="0" value={budget} onChange={(e) => setBudget(e.target.value)} style={{ display: 'block', width: '150px', padding: '0.65rem 0.8rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', marginTop: '0.3rem' }} />
            </label>
            <button className="btn-accent" type="submit" disabled={budgetLoading}>
              {budgetLoading ? 'Finding nearby places…' : 'Find places for my budget'}
            </button>
          </form>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', lineHeight: 1.6 }}>
            Places come from OpenStreetMap. Costs are rough per-person examples (free outdoor/heritage sites: $0; museums: $12; major attractions: $25; other places: $8), not live ticket prices. Stay and transport costs are not included.
          </p>
          {budgetError && <p role="alert" style={{ color: '#b42318' }}>{budgetError}</p>}
          {budgetDestination && budgetPlaces.length > 0 && (
            <section style={{ marginTop: '1.5rem' }}>
              <h3><MapPin size={17} /> Nearby places in {budgetDestination.name} ({budgetPlaces.length})</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Suggested budget-fit picks: {budgetPlan.length} places, about ${estimatedPlanSpend} total of your ${budgetAmount} budget. Estimates are illustrative; verify actual entry prices before travelling.
              </p>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '0.75rem' }}>
                {budgetPlaces.map((place, index) => (
                  <article key={`${place.name}-${place.latitude}-${index}`} style={{ padding: '0.9rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', background: place.estimatedVisitCost <= budgetAmount ? '#f0fdf4' : '#fff7ed' }}>
                    <strong>{place.name}</strong>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.82rem', marginTop: '0.35rem' }}>
                      {place.category || 'Local place'}{place.distanceKm > 0 ? ` · ${place.distanceKm.toFixed(1)} km away` : ''}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem', marginTop: '0.6rem', fontSize: '0.82rem' }}>
                      <span>Est. visit: {place.estimatedVisitCost === 0 ? 'Free' : `$${place.estimatedVisitCost}`}</span>
                      <strong>{budgetPlanNames.has(place.name) ? 'In budget-fit picks' : 'Other nearby option'}</strong>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          )}
        </div>
      )}
    </div>
  );
}
