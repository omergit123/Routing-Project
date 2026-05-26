# 🌐 Network Routing System (Dijkstra Implementation)

This project simulates a computer network and calculates the optimal routing path between routers using a modified **Dijkstra Algorithm**. Unlike standard implementations that only consider distance, this system accounts for **bandwidth capacity, current network load, and signal delay**.

## 🚀 Key Features
* **Dynamic Network Creation:** Add routers and connect them with communication lines via a CLI menu.
* **Smart Routing Algorithm:** implementation of Dijkstra that prioritizes paths based on a dynamic weight formula.
* **Congestion Control:** The system identifies bottlenecks and avoids paths where `Load > Capacity`.
* **Performance:** Utilizes `PriorityQueue` for efficient pathfinding ($O(E \log V)$).
* **Robust Testing:** Includes stress tests for high-load scenarios, disconnected graphs, and large grid networks.

## 🧠 How It Works
The core logic resides in `DijkstraAlgorithm.java`. The weight of each edge is calculated dynamically:

$$Weight = Delay + \frac{1}{Capacity - CurrentLoad}$$

* **Delay:** Physical distance / Speed.
* **Capacity:** Max bandwidth of the line.
* **Current Load:** How much data is currently flowing.

As the load approaches capacity, the cost shoots up, forcing the algorithm to find alternative (even if physically longer) paths—simulating real-world traffic balancing.

## 🛠️ Installation & Usage

### Prerequisites
* Java JDK 8 or higher.
* IntelliJ IDEA (Recommended).

### Steps
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/omergit123/Routing-Project.git](https://github.com/omergit123/Routing-Project.git)
    cd Routing-Project
    ```
2.  **Run the Simulation:**
    * Locate `Simulation.java` (contains the `main` method).
    * Run the file to start the interactive menu.

3.  **Interactive Menu:**
    ```text
    Main menu:
        1. Add router (ID, X, Y)
        2. Add line (Source ID, Dest ID, Capacity)
        3. Route (Calculate best path considering Load)
        4. Exit
    ```

## 📂 Project Structure
* `Simulation.java`: The main entry point (CLI).
* `DijkstraAlgorithm.java`: Pathfinding logic.
* `Network.java`: Manages the graph of routers.
* `Router.java`: Represents a node with coordinates (X, Y).
* `Line.java`: Represents an edge with Capacity, Delay, and Load.
* `PredecessorAndDistance.java`: Used for path finding in Dijkstra algorithm.
* `RoutingAlgorithm.java`: Interface for each routhing algorithm we will use now or in the future.
* `RoutingTests.java`: JUnit test suites.

## 🧪 Testing
The project includes comprehensive Unit Tests and Stress Tests covering:
* ✅ Simple direct paths.
* ✅ Complex multi-hop routes.
* ✅ **Saturation Tests:** Verifying behavior when lines are 99.9% full.
* ✅ **Grid Tests:** Routing in large-scale networks (100+ nodes).

To run tests, execute the JUnit classes located in the `src/test` directory.

## 👤 Author
**Omer Rahamim** - [GitHub Profile](https://github.com/omergit123)




