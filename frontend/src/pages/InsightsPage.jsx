import React from 'react';
import { Binary, Search, Route, GitCompare, Sparkles, Workflow } from 'lucide-react';

const engineGroups = [
  {
    title: 'String algorithms',
    icon: Search,
    summary: 'From finding one phrase in a travel guide to indexing and comparing whole descriptions.',
    items: [
      'The pattern-in-text problem; the naive O(nm) baseline and why better methods matter.',
      'Knuth–Morris–Pratt: failure-function preprocessing intuition and linear-time matching.',
      'The Z-function as a linear-time alternative for prefix matches.',
      'Rabin–Karp with polynomial rolling hashes, modular arithmetic, and double hashing.',
      'Aho–Corasick: a trie with failure links for multi-pattern search.',
      'Suffix arrays: definition, SA-IS at a conceptual O(n) level, and practical O(n log n) construction.',
      'Kasai’s algorithm for a linear-time longest-common-prefix (LCP) array.',
      'Suffix automata and suffix trees: a preview of what they enable beyond suffix arrays.',
    ],
  },
  {
    title: 'Advanced dynamic programming',
    icon: Route,
    summary: 'A toolkit for planning problems where subproblems overlap and local choices affect the best overall result.',
    items: [
      'The DP framework: overlapping subproblems and optimal substructure.',
      'Levenshtein, Damerau–Levenshtein, and weighted edit distance; the Wagner–Fischer algorithm.',
      'Sequence alignment: Needleman–Wunsch (global), Smith–Waterman (local), and gap penalties.',
      'Interval DP: matrix-chain multiplication and optimal binary search trees, including optimized variants.',
      'Bitmask DP for travelling salesperson in O(n² · 2ⁿ), Hamiltonian paths, and set-cover variants.',
      'Tree DP: subset sums, diameter, centroid problems, and rerooting.',
      'Sum-over-subsets (SOS) DP and its use in inclusion–exclusion.',
      'When DP is the wrong tool: no overlap, a sufficient greedy choice, or an uncompressed exponential state space.',
    ],
  },
  {
    title: 'Network flow',
    icon: Workflow,
    summary: 'Model capacities, assignments, and constrained choices as flow through a network.',
    items: [
      'Maximum flow: source, sink, capacity, conservation, and integral-capacity flows.',
      'Ford–Fulkerson augmenting paths in the residual graph and correctness via max-flow min-cut.',
      'Edmonds–Karp: BFS augmenting paths in O(VE²).',
      'Dinic’s algorithm: blocking flows on level graphs in O(V²E).',
      'Minimum cut as the dual of maximum flow; partitioning via flow.',
      'Bipartite matching, König’s theorem, perfect matching, and assignment problems.',
      'Minimum-cost maximum flow: successive shortest paths, cycle cancelling, and cost-aware assignment.',
      'Practical reductions: image segmentation, project selection, and deadline scheduling.',
    ],
  },
  {
    title: 'NP-completeness & approximation',
    icon: GitCompare,
    summary: 'Recognize hard problems early and understand what efficient approximation can—and cannot—promise.',
    items: [
      'Decision versus optimisation problems; polynomial-time reductions as a comparison tool.',
      'P, NP, and co-NP through verification and canonical witnesses.',
      'Cook–Levin at an intuition level and SAT as a canonical NP-complete problem.',
      'The reduction zoo: 3-SAT, CLIQUE, INDEPENDENT-SET, VERTEX-COVER, SUBSET-SUM, KNAPSACK, HAM-CYCLE, and TSP.',
      'An engineering checklist for spotting familiar NP-hard structures.',
      'Approximation ratios and the 2-approximation for vertex cover via maximal matching.',
      'PTAS, FPTAS, and APX; approximation limits and why some guarantees are unavailable.',
      'Parameterized complexity preview: FPT and kernelisation when the hard part is a small parameter.',
    ],
  },
  {
    title: 'Randomized & parallel algorithms',
    icon: Sparkles,
    summary: 'Use randomness for simple, effective algorithms and parallel work when a problem can be split safely.',
    items: [
      'Las Vegas versus Monte Carlo: always-correct results versus correctness with probability.',
      'Randomized quicksort and its expected-time analysis.',
      'Miller–Rabin: witnesses, Carmichael numbers, and practical probabilistic primality testing.',
      'Universal hash families and perfect hashing; why hashing is fundamentally probabilistic.',
      'Reservoir sampling for uniform samples from a stream, with the proof idea.',
      'Parallel work and span (T₁ and T∞), Brent’s theorem, and the speedup ceiling.',
      'Parallel reduce and Blelloch scan/prefix sum; parallel sorting at an intuition level.',
      'Where data-parallel and task-parallel approaches shine—and where they do not.',
    ],
  },
];

export default function InsightsPage() {
  return (
    <div className="insights-page">
      <div className="insights-header">
        <span className="section-eyebrow"><Binary size={15} /> THE IDEAS BEHIND THE JOURNEY</span>
        <h1>How TravelSync works</h1>
        <p>
          TravelSync is designed around real travel tasks—finding places, comparing ideas, and putting a trip together.
          This guide collects the algorithm concepts connected to those tasks, alongside the broader topics in the curriculum.
        </p>
      </div>

      <div className="insight-grid">
        {engineGroups.map(({ title, icon: Icon, summary, items }, index) => (
          <article className="insight-card" key={title}>
            <div className="insight-card-heading">
              <div className={`insight-card-icon insight-card-icon-${index}`}>
                <Icon size={20} />
              </div>
              <h2>{title}</h2>
            </div>
            <p className="insight-card-summary">{summary}</p>
            <ul className="insight-list">
              {items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
        ))}
      </div>
      <p className="insights-note">
        Some of these topics are implemented in TravelSync’s current travel tools; others are included here as study concepts and possible future applications.
      </p>
    </div>
  );
}
