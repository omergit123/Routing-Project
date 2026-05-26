# 🌐 Network Routing System (Dijkstra Implementation & Gemini AI Integration)

This project simulates a computer network and calculates the optimal routing path between routers using a modified **Dijkstra Algorithm**. Unlike standard implementations that only consider distance, this system accounts for **bandwidth capacity, current network load, and signal delay**. 

The simulation dynamically integrates with the **Gemini AI API** to generate realistic large-scale topology configurations and automated connection streams based on simple user prompts.

## 🚀 Key Features
* **AI-Powered Generation:** Generates complex network environments (routers and data streams) using Google Gemini 2.5-Flash via API.
* **Smart Routing Algorithm:** A customized implementation of Dijkstra that prioritizes paths based on a dynamic traffic and load formula.
* **Congestion Control:** The system identifies bottlenecks and avoids paths where `Load > Capacity`.
* **Containerized Environment:** Fully dockerized setup using Multi-stage builds, separate JRE runtimes, and Docker Compose orchestration.
* **Performance:** Utilizes `PriorityQueue` for efficient pathfinding ($O(E \log V)$).
* **Robust JSON Processing:** Utilizes Jackson (with automated filtering features) to ensure resilient schema binding against AI response anomalies.

## 🧠 How It Works
The core logic resides in `DijkstraAlgorithm.java`. The weight of each edge is calculated dynamically:

$$Weight = Delay + \frac{1}{Capacity - CurrentLoad}$$

* **Delay:** Physical distance / Speed (or static line delay).
* **Capacity:** Max bandwidth of the line.
* **Current Load:** How much data is currently flowing.

As the load approaches capacity, the cost shoots up, forcing the algorithm to find alternative (even if physically longer) paths—simulating real-world traffic balancing.

---

## 🛠️ Installation & Usage (Docker Setup)

The easiest and most reliable way to run the simulation is using **Docker** and **Docker Compose**, which eliminates the need for local Java or Maven installations.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.
* A Google Gemini API Key (Get one from [Google AI Studio](https://aistudio.google.com/)).

### 1. Setup Environment Credentials
Create a file named `.env` in the root directory of the project (right next to `docker-compose.yml`) and add your Gemini API Key:

```env
GEMINI_API_KEY=AIzaSyYourActualSecretKeyHere...
```
*Note: The `.env` file is included in `.gitignore` to prevent secret credentials from being leaked to GitHub.*

### 2. Build and Run the Container
Run the following commands in your terminal to build the Maven Fat-JAR image and start the interactive CLI:

```bash
# Build the application image using cache-optimized stages
docker compose build

# Run the simulation container interactively
docker compose run --rm network-simulator
```

### 3. Interactive CLI Example
Once started, input a descriptive prompt to let the AI build your network topology:
```text
> Enter prompt: create network with 30 routers and 15 streams
Sending request to Gemini AI via API...
⚡ JSON Configuration files created successfully via Jackson (Cleaned!)
🚀 Loading topology configuration...
[System Output] Simulating dynamic routing and Dijkstra steps...
```

## 👤 Author
**Omer Rahamim** - [GitHub Profile](https://github.com/omergit123)
