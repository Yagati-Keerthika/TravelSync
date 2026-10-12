import React, { useState } from 'react';
import { ArrowRight, Compass, MapPin, Search, Sparkles } from 'lucide-react';

const destinations = [
  {
    name: 'Manali',
    country: 'India',
    description: 'Mountain air and slow mornings',
    image: 'photo-1626621341517-bbf3d9990a23',
  },
  {
    name: 'Kyoto',
    country: 'Japan',
    description: 'Quiet streets, timeless traditions',
    image: 'photo-1493976040374-85c8e12f0c0e',
  },
  {
    name: 'Paris',
    country: 'France',
    description: 'A little magic around every corner',
    image: 'photo-1502602898657-3e91760cbb34',
  },
  {
    name: 'Bali',
    country: 'Indonesia',
    description: 'Green escapes and ocean sunsets',
    image: 'photo-1537996194471-e657df975ab4',
  },
];

const travelTools = [
  {
    eyebrow: 'FIND YOUR NEXT FAVOURITE',
    title: 'Explore somewhere new',
    description: 'Get to know a destination with local places, useful details, and a little inspiration.',
    action: 'Explore destinations',
    tab: 'explore',
    image: 'photo-1470770841072-f978cf4d019e',
  },
  {
    eyebrow: 'MAKE EVERY DAY COUNT',
    title: 'Plan a trip that flows',
    description: 'Bring your stops together and shape a thoughtful route around the places you love.',
    action: 'Plan your trip',
    tab: 'planner',
    image: 'photo-1464822759023-fed622ff2c3b',
  },
  {
    eyebrow: 'MADE AROUND YOU',
    title: 'Find your kind of getaway',
    description: 'Discover destination ideas inspired by the experiences you want from your next trip.',
    action: 'See trip ideas',
    tab: 'recommendations',
    image: 'photo-1518509562904-e7ef99cdcc86',
  },
];

export default function Home({ onSearch, setActiveTab }) {
  const [query, setQuery] = useState('');

  const searchForDestination = (destination) => {
    const trimmedQuery = destination.trim();
    if (!trimmedQuery) return;
    onSearch(trimmedQuery);
    setActiveTab('explore');
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    searchForDestination(query);
  };

  return (
    <div className="home-page">
      <section className="travel-hero">
        <div className="travel-hero-copy">
          <span className="travel-eyebrow">
            <Compass size={15} />
            A little closer to your next adventure
          </span>
          <h1>
            The world is wide.
            <br />
            <span>Let’s find your place in it.</span>
          </h1>
          <p>
            Find the places you’ll love, plan days worth remembering, and make
            your next trip feel like your own.
          </p>
          <form className="travel-search" onSubmit={handleSubmit}>
            <MapPin size={20} aria-hidden="true" />
            <label className="sr-only" htmlFor="destination-search">Where would you like to go?</label>
            <input
              id="destination-search"
              type="search"
              placeholder="Where would you like to go?"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <button type="submit" aria-label="Search destinations">
              <Search size={18} />
              <span>Explore</span>
            </button>
          </form>
          <div className="travel-search-hint">
            <span>Try</span>
            {['Manali', 'Tokyo', 'Paris'].map((city) => (
              <button key={city} type="button" onClick={() => searchForDestination(city)}>
                {city}
              </button>
            ))}
          </div>
        </div>
        <div className="travel-hero-visual" role="img" aria-label="A peaceful mountain lake surrounded by green hills">
          <div className="hero-photo-caption">
            <span className="hero-caption-icon"><Sparkles size={16} /></span>
            <span>
              <strong>Your next story starts somewhere.</strong>
              <small>Find a destination that feels like you.</small>
            </span>
          </div>
        </div>
      </section>

      <section className="destination-section">
        <div className="travel-section-heading">
          <div>
            <span className="section-eyebrow">A GOOD PLACE TO BEGIN</span>
            <h2>Places to put on your list</h2>
            <p>A few much-loved corners of the world to get you dreaming.</p>
          </div>
          <button className="text-link" type="button" onClick={() => setActiveTab('explore')}>
            See all destinations <ArrowRight size={17} />
          </button>
        </div>
        <div className="destination-grid">
          {destinations.map((destination) => (
            <button
              className="destination-card"
              key={destination.name}
              type="button"
              onClick={() => searchForDestination(destination.name)}
              aria-label={`Explore ${destination.name}, ${destination.country}`}
            >
              <img
                src={`https://images.unsplash.com/${destination.image}?auto=format&fit=crop&w=720&q=85`}
                alt=""
                loading="lazy"
              />
              <span className="destination-card-shade" />
              <span className="destination-card-copy">
                <span>{destination.country}</span>
                <strong>{destination.name}</strong>
                <small>{destination.description}</small>
              </span>
              <span className="destination-card-arrow"><ArrowRight size={17} /></span>
            </button>
          ))}
        </div>
      </section>

      <section className="travel-tools-section">
        <div className="travel-section-heading">
          <div>
            <span className="section-eyebrow">YOUR TRIP, YOUR WAY</span>
            <h2>Everything for the journey</h2>
            <p>From the first spark of an idea to the little details along the way.</p>
          </div>
        </div>
        <div className="travel-tools-grid">
          {travelTools.map((tool) => (
            <article className="travel-tool-card" key={tool.title}>
              <div
                className="travel-tool-image"
                style={{ backgroundImage: `url(https://images.unsplash.com/${tool.image}?auto=format&fit=crop&w=900&q=85)` }}
                role="img"
                aria-label={tool.title}
              />
              <div className="travel-tool-copy">
                <span className="section-eyebrow">{tool.eyebrow}</span>
                <h3>{tool.title}</h3>
                <p>{tool.description}</p>
                <button className="text-link" type="button" onClick={() => setActiveTab(tool.tab)}>
                  {tool.action} <ArrowRight size={17} />
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="travel-note">
        <div className="travel-note-icon"><Compass size={25} /></div>
        <div>
          <span className="section-eyebrow">TRAVEL, THOUGHTFULLY</span>
          <h2>More than a place to go. A better way to get there.</h2>
          <p>TravelSync brings destination discovery, thoughtful itineraries, and personal trip ideas together in one place.</p>
        </div>
        <button className="travel-note-button" type="button" onClick={() => setActiveTab('planner')}>
          Start planning <ArrowRight size={17} />
        </button>
      </section>
    </div>
  );
}
