# Snake and Ladder — BFS Optimal Path Solver

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Swing](https://img.shields.io/badge/GUI-Swing-orange)
![License](https://img.shields.io/badge/license-MIT-green)

An interactive **Snake and Ladder** game built in Java with Swing, featuring a **Breadth-First Search (BFS) solver** that computes the minimum number of dice throws from cell 1 to cell 100.

## Features

- **Full graphical board** — 10×10 grid with snakes, ladders, and animated player token
- **BFS optimal path solver** — finds the shortest route with step-by-step breakdown
- **Path highlighting** — optimal cells highlighted in gold on the board
- **Die animation** — rolling animation with visual dot pattern
- **Snake/ladder transitions** — animated slides and climbs with 600 ms pause
- **Keyboard shortcuts** — `Ctrl+R` roll, `Ctrl+N` new game, `F1` help
- **Game log** — scrollable event log panel
- **Win detection** — victory dialog with move count vs optimal comparison

## Quick Start

```bash
# Compile
javac SnakeLadder.java

# Run
java SnakeLadder
```

Requires **Java 17+**.

## Controls

| Action | Button | Shortcut |
|--------|--------|----------|
| Roll Dice | Roll Dice | `Ctrl+R` |
| New Game | New Game | `Ctrl+N` |
| BFS Solver | BFS Solver | — |
| Show Help | — | `F1` |

## BFS Solver

The board is modeled as a directed graph with 100 nodes. Each die face (1–6) is an outgoing edge. Snakes and ladders are applied as immediate transitions. BFS guarantees the shortest path because all edges have unit weight.

Click **BFS Solver** to see:
- Minimum number of throws
- Full cell path with roll values
- Per-step breakdown showing snakes/ladders encountered
- "Highlight on Board" to visualize the optimal route

### Algorithm Complexity

| Metric | Value |
|--------|-------|
| Time | O(V + E) = O(600) |
| Space | O(V) |
| Nodes (V) | 100 |
| Edges (E) | ≤ 600 |

## Board Layout

The board uses a **boustrophedon** (alternating direction) layout — rows alternate left-to-right and right-to-left, matching the physical game.

### Snakes (head → tail)

| Head | Tail | Drop |
|------|------|------|
| 16 | 6 | 10 |
| 46 | 25 | 21 |
| 49 | 11 | 38 |
| 62 | 19 | 43 |
| 64 | 60 | 4 |
| 74 | 53 | 21 |
| 89 | 68 | 21 |
| 92 | 88 | 4 |
| 95 | 75 | 20 |
| 99 | 80 | 19 |

### Ladders (bottom → top)

| Bottom | Top | Gain |
|--------|-----|------|
| 2 | 38 | 36 |
| 7 | 14 | 7 |
| 8 | 31 | 23 |
| 15 | 26 | 11 |
| 21 | 42 | 21 |
| 28 | 84 | 56 |
| 36 | 44 | 8 |
| 51 | 67 | 16 |
| 71 | 91 | 20 |
| 78 | 98 | 20 |
| 87 | 94 | 7 |

## Project Structure

```
├── SnakeLadder.java          # Main source (678 lines)
├── Snake_Ladder_Project_Report.docx  # Detailed project report with screenshots
├── .gitignore
└── README.md
```

The source contains 6 classes:

| Class | Type | Responsibility |
|-------|------|----------------|
| `SnakeLadder` | `JFrame` | Main window, game loop, UI events |
| `Board` | static inner | Data model: snakes, ladders, coordinates |
| `BFSSolver` | static inner | BFS algorithm and path reconstruction |
| `BoardPanel` | inner `JPanel` | Board rendering (grid, snakes, ladders, player) |
| `DicePanel` | static inner `JPanel` | Die face dot-pattern renderer |
| `Result` | static inner | Data container for BFS output |

## Report

A comprehensive **project report** (`Snake_Ladder_Project_Report.docx`) is included with:
- Algorithm explanation and pseudocode
- Full BFS trace walkthrough
- GUI architecture and rendering deep-dive
- 4 embedded screenshots
- Complete source code listing
- Complexity analysis and future enhancements

## License

MIT
