import React, { useState } from 'react';
import { ArrowRight, CheckCircle2, GitBranch, Plus, RefreshCw, Trash2, Users } from 'lucide-react';
import TravelApi from '../services/api';

const sampleTravelers = [
  { id: 'traveler-asha', name: 'Asha', interests: ['nature', 'culture'] },
  { id: 'traveler-arjun', name: 'Arjun', interests: ['adventure', 'nature'] },
  { id: 'traveler-leah', name: 'Leah', interests: ['culture', 'food'] },
  { id: 'traveler-noah', name: 'Noah', interests: ['nature', 'adventure'] },
  { id: 'traveler-sofia', name: 'Sofia', interests: ['food', 'culture'] },
  { id: 'traveler-omar', name: 'Omar', interests: ['adventure', 'food'] },
];

const sampleActivities = [
  { id: 'activity-nature', name: 'Coastal nature walk', categories: ['nature'], capacity: 2 },
  { id: 'activity-museum', name: 'Local history museum', categories: ['culture'], capacity: 2 },
  { id: 'activity-food', name: 'Street-food tour', categories: ['food'], capacity: 1 },
  { id: 'activity-kayak', name: 'Guided kayaking', categories: ['adventure'], capacity: 1 },
];

function parseTags(value) {
  return value.split(',').map((tag) => tag.trim()).filter(Boolean);
}

export default function ActivityAllocationPage() {
  const [travelers, setTravelers] = useState(sampleTravelers);
  const [activities, setActivities] = useState(sampleActivities);
  const [travelerDraft, setTravelerDraft] = useState({ name: '', interests: '' });
  const [activityDraft, setActivityDraft] = useState({ name: '', categories: '', capacity: '1' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const addTraveler = (event) => {
    event.preventDefault();
    const name = travelerDraft.name.trim();
    const interests = parseTags(travelerDraft.interests);
    if (!name || interests.length === 0) {
      setError('Enter a traveler name and at least one interest.');
      return;
    }
    setTravelers((current) => [...current, {
      id: `traveler-${Date.now()}`,
      name,
      interests,
    }]);
    setTravelerDraft({ name: '', interests: '' });
    setResult(null);
    setError('');
  };

  const addActivity = (event) => {
    event.preventDefault();
    const name = activityDraft.name.trim();
    const categories = parseTags(activityDraft.categories);
    const capacity = Number(activityDraft.capacity);
    if (!name || categories.length === 0 || !Number.isInteger(capacity) || capacity < 1 || capacity > 100) {
      setError('Enter an activity name, at least one category, and a capacity from 1 to 100.');
      return;
    }
    setActivities((current) => [...current, {
      id: `activity-${Date.now()}`,
      name,
      categories,
      capacity,
    }]);
    setActivityDraft({ name: '', categories: '', capacity: '1' });
    setResult(null);
    setError('');
  };

  const runAllocation = async () => {
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const response = await TravelApi.allocateActivities(travelers, activities);
      setResult(response.data);
    } catch (requestError) {
      setError(requestError.message || 'Could not allocate travelers. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const resetSamples = () => {
    setTravelers(sampleTravelers);
    setActivities(sampleActivities);
    setResult(null);
    setError('');
  };

  return (
    <div className="flow-allocation">
      <section className="card flow-intro">
        <div className="flow-title-row">
          <span className="flow-title-icon"><GitBranch size={22} /></span>
          <div>
            <span className="section-eyebrow">NETWORK FLOW · EDMONDS–KARP</span>
            <h2>Match travelers to activity spaces</h2>
          </div>
        </div>
        <p>
          Assign each traveler to at most one activity they are interested in, without exceeding
          activity capacity. The flow network finds the maximum number of travelers that can be
          accommodated, including when a flexible traveler must be rerouted to make room for
          someone with fewer choices.
        </p>
        <div className="flow-network-summary" aria-label="Flow network structure">
          <span><Users size={17} /> Source</span>
          <ArrowRight size={17} />
          <span>Traveler nodes</span>
          <ArrowRight size={17} />
          <span>Eligible activities</span>
          <ArrowRight size={17} />
          <span>Capacity-limited sink</span>
        </div>
      </section>

      <div className="flow-editor-grid">
        <section className="card">
          <div className="flow-section-heading">
            <div>
              <h3>Travelers ({travelers.length})</h3>
              <p>Interest tags determine which activities each traveler can join.</p>
            </div>
          </div>
          <form className="flow-add-form" onSubmit={addTraveler}>
            <label>
              Name
              <input
                value={travelerDraft.name}
                onChange={(event) => setTravelerDraft({ ...travelerDraft, name: event.target.value })}
                placeholder="e.g. Maya"
              />
            </label>
            <label>
              Interests (comma-separated)
              <input
                value={travelerDraft.interests}
                onChange={(event) => setTravelerDraft({ ...travelerDraft, interests: event.target.value })}
                placeholder="e.g. nature, food"
              />
            </label>
            <button className="btn-outline" type="submit"><Plus size={15} /> Add</button>
          </form>
          <ul className="flow-entity-list">
            {travelers.map((traveler) => (
              <li key={traveler.id}>
                <span><strong>{traveler.name}</strong><small>{traveler.interests.join(', ')}</small></span>
                <button
                  className="flow-remove"
                  type="button"
                  aria-label={`Remove ${traveler.name}`}
                  onClick={() => {
                    setTravelers((current) => current.filter((item) => item.id !== traveler.id));
                    setResult(null);
                  }}
                >
                  <Trash2 size={15} />
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="card">
          <div className="flow-section-heading">
            <div>
              <h3>Activities ({activities.length})</h3>
              <p>Category tags define eligibility; capacity limits assigned travelers.</p>
            </div>
          </div>
          <form className="flow-add-form flow-activity-form" onSubmit={addActivity}>
            <label>
              Activity
              <input
                value={activityDraft.name}
                onChange={(event) => setActivityDraft({ ...activityDraft, name: event.target.value })}
                placeholder="e.g. City walking tour"
              />
            </label>
            <label>
              Categories
              <input
                value={activityDraft.categories}
                onChange={(event) => setActivityDraft({ ...activityDraft, categories: event.target.value })}
                placeholder="e.g. culture, nature"
              />
            </label>
            <label>
              Capacity
              <input
                type="number"
                min="1"
                max="100"
                value={activityDraft.capacity}
                onChange={(event) => setActivityDraft({ ...activityDraft, capacity: event.target.value })}
              />
            </label>
            <button className="btn-outline" type="submit"><Plus size={15} /> Add</button>
          </form>
          <ul className="flow-entity-list">
            {activities.map((activity) => (
              <li key={activity.id}>
                <span>
                  <strong>{activity.name}</strong>
                  <small>{activity.categories.join(', ')} · capacity {activity.capacity}</small>
                </span>
                <button
                  className="flow-remove"
                  type="button"
                  aria-label={`Remove ${activity.name}`}
                  onClick={() => {
                    setActivities((current) => current.filter((item) => item.id !== activity.id));
                    setResult(null);
                  }}
                >
                  <Trash2 size={15} />
                </button>
              </li>
            ))}
          </ul>
        </section>
      </div>

      {error && <p className="flow-error" role="alert">{error}</p>}

      <div className="flow-actions">
        <button
          className="btn-primary"
          type="button"
          onClick={runAllocation}
          disabled={loading || travelers.length === 0 || activities.length === 0}
        >
          {loading ? 'Finding the maximum allocation…' : 'Allocate travelers'}
        </button>
        <button className="btn-outline" type="button" onClick={resetSamples}>
          <RefreshCw size={15} /> Reset sample
        </button>
      </div>

      {result && (
        <section className="card flow-result" aria-live="polite">
          <div className="flow-result-header">
            <div>
              <span className="section-eyebrow">MAX-FLOW RESULT</span>
              <h3>{result.assignedTravelers} of {result.totalTravelers} travelers allocated</h3>
            </div>
            <span className="flow-complexity">{result.algorithm} · {result.complexity}</span>
          </div>
          <div className="flow-assignments">
            {result.assignments.map((assignment) => (
              <div className="flow-assignment" key={assignment.travelerId}>
                <span><CheckCircle2 size={17} /> <strong>{assignment.travelerName}</strong></span>
                <ArrowRight size={16} />
                <span>{assignment.activityName}</span>
                <small>Matched: {assignment.matchingInterests.join(', ')}</small>
              </div>
            ))}
          </div>
          {result.unassignedTravelers.length > 0 && (
            <p className="flow-unassigned">
              Not allocated: {result.unassignedTravelers.map((traveler) => traveler.name).join(', ')}
              {' '}— no compatible activity space remained.
            </p>
          )}
          <p className="flow-graph-stats">
            Residual graph: {result.graphVertices} vertices, {result.graphEdges} edges,
            {' '}{result.augmentations} augmenting paths.
          </p>
        </section>
      )}
    </div>
  );
}
