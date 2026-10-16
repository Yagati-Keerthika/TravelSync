package com.travelsync.engine;

import java.util.*;

/**
 * Aho-Corasick Multi-Pattern String Matching Automaton
 *
 * Purpose: Searches for a dictionary of multiple keywords simultaneously in a
 *          single linear scan of the text using a Trie extended with BFS failure transitions.
 * Time Complexity: Construction O(sum(|P_i|)), Search O(N + Z) where Z is match count
 * Space Complexity: O(sum(|P_i|)) states
 * Usage: Simultaneous travel keyword scanning (e.g. "beach", "temple", "adventure", "nature")
 *        across live destination extracts.
 */
public class AhoCorasick {

    private static class Node {
        final Map<Character, Node> children = new HashMap<>();
        Node fail = null;
        final List<String> outputs = new ArrayList<>();
    }

    private final Node root;

    public AhoCorasick(Collection<String> patterns) {
        this.root = new Node();
        buildTrie(patterns);
        buildFailureLinks();
    }

    private void buildTrie(Collection<String> patterns) {
        if (patterns == null) return;
        Set<String> seen = new HashSet<>();
        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty() || !seen.add(pattern)) continue;
            Node current = root;
            for (char ch : pattern.toCharArray()) {
                current = current.children.computeIfAbsent(ch, c -> new Node());
            }
            current.outputs.add(pattern);
        }
    }

    private void buildFailureLinks() {
        Queue<Node> queue = new ArrayDeque<>();

        // Level 1 nodes fail to root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        // BFS traversal for deeper levels
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                Node fallback = current.fail;
                while (fallback != null && !fallback.children.containsKey(ch)) {
                    fallback = fallback.fail;
                }

                child.fail = (fallback != null) ? fallback.children.get(ch) : root;
                // Merge dictionary output links without duplicating pattern matches
                Set<String> mergedOutputs = new LinkedHashSet<>(child.outputs);
                mergedOutputs.addAll(child.fail.outputs);
                child.outputs.clear();
                child.outputs.addAll(mergedOutputs);
                queue.add(child);
            }
        }
    }

    /**
     * Finds all matches for all patterns in the text.
     *
     * @param text The input text
     * @return Map of pattern -> list of start indices
     */
    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        Node current = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            while (current != null && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            if (current == null) {
                current = root;
                continue;
            }

            current = current.children.get(ch);

            for (String pattern : current.outputs) {
                int start = i - pattern.length() + 1;
                result.computeIfAbsent(pattern, k -> new ArrayList<>()).add(start);
            }
        }

        return result;
    }

    /**
     * Static convenience helper.
     */
    public static Map<String, List<Integer>> searchAll(String text, Collection<String> patterns) {
        if (patterns == null || patterns.isEmpty() || text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }
        AhoCorasick automaton = new AhoCorasick(patterns);
        return automaton.search(text);
    }
}
