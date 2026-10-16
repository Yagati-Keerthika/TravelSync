# TravelSync

TravelSync is a web-based travel planning and information platform that combines destination discovery with data structures and algorithms (DSA). It provides a React frontend and a Java Spring Boot REST API.

## Features

- Search for destinations and explore destination information.
- View nearby places, weather, and travel information from external services.
- Compare destinations and analyze similarities between their descriptions.
- Search travel text and booking codes, including typo suggestions and multiple-keyword search.
- Optimize multi-destination trip order using road-distance or travel-duration data.
- Create optimized activity schedules and hierarchical travel plans.
- Get destination recommendations based on travel preferences.
- Explore the algorithms and concepts behind the application in the Insights section.

## Technology

- **Frontend:** React 18, Vite, JavaScript
- **Backend:** Java 21, Spring Boot 3
- **External services:** Open-Meteo, Wikipedia, OSRM, and OpenStreetMap Overpass
- **Optional integrations:** Geoapify and OpenTripMap API keys

## Requirements

- Java 21
- Maven 3.9 or later
- Node.js and npm

## Run locally

Start the backend in one terminal:

```powershell
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8081`. Check that it is up at:

```text
http://localhost:8081/api/travel/hello
```

Start the frontend in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser. The frontend uses `http://localhost:8081` as its default API URL. To override it, set `VITE_API_BASE_URL` in the frontend environment before starting Vite.

## Tests

Run the backend tests from the `backend` directory:

```powershell
mvn test
```

## Algorithms

The backend implements the following algorithms:

- **String search:** Naive Search, Knuth–Morris–Pratt (KMP), Z-Function, Rabin–Karp, and Aho–Corasick
- **Text comparison:** Edit Distance, Suffix Array, Longest Common Prefix (LCP), Needleman–Wunsch, and Smith–Waterman
- **Dynamic programming:** Bitmask DP, Interval DP, Tree DP, and Sum Over Subsets (SOS) DP

These algorithms support features such as text search, booking-code lookup, typo suggestions, description comparison, itinerary ordering, activity scheduling, hierarchical plan optimization, and preference-based recommendations.

## Data and integrations

The application retrieves travel data from external services, so some features depend on network access and those services being available. Booking search currently uses sample in-memory records; the project does not currently include persistent database storage.

Optional integration keys can be configured as environment variables:

- `GEOAPIFY_API_KEY`
- `OPENTRIPMAP_API_KEY`

See [.env.example](./.env.example) for the configuration template.

## Project layout

```text
backend/    Spring Boot API, services, models, DSA engines, and tests
frontend/   React application
docs/       Project presentation and supporting documentation
```

## Project presentation

The DSA Review-1 presentation is available at [docs/TravelSync_DSA_Review1.pptx](./docs/TravelSync_DSA_Review1.pptx).
