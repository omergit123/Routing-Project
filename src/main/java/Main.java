import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import config.*;

public class Main {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final Random RANDOM = new Random();

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key="
            + API_KEY;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("====== Network Simulation AI Generator ( Automated API ) ======");

        // Load the system instructions from the config file
        String systemInstructions = "";
        try {
            systemInstructions = Files.readString(Paths.get("config/system_prompt.txt"));
        } catch (IOException e) {
            System.err.println("❌ Critical Error: Could not load config/system_prompt.txt");
            System.err.println("Please ensure the prompt file exists in the config directory.");
            scanner.close();
            return;
        }

        System.out
                .print("Generate a new network with Gemini API or a local random generator? (api/local) [local]:\n> ");
        String generationMode = scanner.nextLine().trim().toLowerCase();
        boolean useApi = generationMode.equals("api");

        if (useApi && (API_KEY == null || API_KEY.isBlank())) {
            System.out.println("\nNo Gemini API key found. Falling back to local random generation.");
            useApi = false;
        }

        try {
            if (!useApi) {
                System.out.println("\nGenerating a new random network and streams locally...");
                int routerCount = promptForInt(scanner, "Enter router count [15]: ", 15);
                int streamCount = promptForInt(scanner, "Enter stream count [30]: ", 30);
                generateRandomNetworkAndTraffic(routerCount, streamCount);
            } else {
                System.out.print("Describe the network topology and streams you want to test:\n> ");
                String userPrompt = scanner.nextLine();
                System.out.println("\nSending request to Gemini AI via API...");
                String aiResponse = callGeminiAPI(userPrompt, systemInstructions);

                // Saving the AI response in json files.
                extractAndSaveJson(aiResponse);
                System.out.println("⚡ JSON Configuration files created successfully via API.");
            }
            System.out.println("🚀 Starting network simulation...\n");

            // =======================================================
            // Main simulation logic: Load the topology and traffic, run the routing
            // algorithm, and handle failures
            // =======================================================
            int failCounter = 0;
            List<String> failedStreams = new ArrayList<>();

            NetworkConfig netConfig = NetworkLoader.loadTopology("config/topology.json");
            TrafficConfig traffic = NetworkLoader.loadTraffic("config/streams.json");

            Network network = new Network(netConfig);
            RoutingAlgorithm algorithm = new DijkstraAlgorithm();

            for (TrafficConfig.Stream stream : traffic.streams) {
                if (failCounter >= 5) {
                    System.out.println("Too many failed streams. Stopping further processing.");
                    break;
                }

                Router source = network.getRouter(stream.source);
                Router target = network.getRouter(stream.destination);
                if (source == null || target == null) {
                    failCounter++;
                    failedStreams.add("Invalid stream route: " + stream.source + " -> " + stream.destination);
                    continue;
                }

                double load = stream.sizeMb;
                HashMap<String, PredecessorAndDistance> routingResult = algorithm.routing(network, source, load);
                PredecessorAndDistance targetResult = routingResult.get(target.getId());

                if (targetResult != null && targetResult.getDistance() < Double.POSITIVE_INFINITY) {
                    System.out.printf("Stream %s: route found from %s to %s (distance=%.5f)%n",
                            stream.streamId, source.getId(), target.getId(), targetResult.getDistance());
                    updateLoadOnPath(network, routingResult, target.getId(), load);
                } else {
                    failCounter++;
                    failedStreams.add("Stream from " + source.getId() + " to " + target.getId());
                }
            }

            if (!failedStreams.isEmpty()) {
                System.out.println("\nFailed streams:");
                failedStreams.forEach(System.out::println);
            } else {
                System.out.println("\nAll streams were routed successfully.");
            }

        } catch (Exception e) {
            System.err.println("\n❌ An error occurred during API call or simulation:");
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /*
     * Calls the Gemini API with the user prompt and system instructions, and
     * returns the AI's response text
     * It's a http POST request.
     */
    private static String callGeminiAPI(String userPrompt, String systemInstructions) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Build the combined prompt with clear delimiters for the AI to generate the
        // required JSON blocks
        ObjectMapper mapper = new ObjectMapper();

        com.fasterxml.jackson.databind.node.ObjectNode rootRequestBody = mapper.createObjectNode();

        // System instructions are added to the request body if they are not empty.
        if (systemInstructions != null && !systemInstructions.trim().isEmpty()) {
            com.fasterxml.jackson.databind.node.ObjectNode sysTextNode = mapper.createObjectNode().put("text",
                    systemInstructions);
            com.fasterxml.jackson.databind.node.ArrayNode sysPartsArray = mapper.createArrayNode().add(sysTextNode);
            com.fasterxml.jackson.databind.node.ObjectNode sysPartsNode = mapper.createObjectNode().set("parts",
                    sysPartsArray);

            rootRequestBody.set("system_instruction", sysPartsNode);
        }

        com.fasterxml.jackson.databind.node.ObjectNode userTextNode = mapper.createObjectNode().put("text", userPrompt);
        com.fasterxml.jackson.databind.node.ArrayNode userPartsArray = mapper.createArrayNode().add(userTextNode);
        com.fasterxml.jackson.databind.node.ObjectNode userContentNode = mapper.createObjectNode().set("parts",
                userPartsArray);
        com.fasterxml.jackson.databind.node.ArrayNode contentsArray = mapper.createArrayNode().add(userContentNode);

        rootRequestBody.set("contents", contentsArray);

        String jsonRequestBody = mapper.writeValueAsString(rootRequestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();

        // --- Exponential Backoff Logic ---
        int maxRetries = 3;
        int baseDelayMs = 2000;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Success - parse JSON and return
                JsonNode rootNode = mapper.readTree(response.body());
                return rootNode.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();

            } else if (response.statusCode() == 429 && attempt < maxRetries) {
                // Rate Limit hit - wait and retry
                int sleepTime = baseDelayMs * (int) Math.pow(2, attempt);
                System.out.println("API Error 429 (Rate Limit). Retrying in " + sleepTime + "ms...");
                Thread.sleep(sleepTime);

            } else {
                // Other errors (e.g., 400, 500) or ran out of retries
                throw new RuntimeException("API returned error: " + response.statusCode() + " - " + response.body());
            }
        }

        throw new RuntimeException("Failed to call Gemini API after " + maxRetries + " retries.");
    }

    // Extracts the JSON content between specified tags and saves it to a file
    private static void extractAndSaveJson(String aiResponseText) throws IOException {
        // Cleaning tags if they exist in the response (in case the AI included them in
        // the output)
        if (aiResponseText.contains("```")) {
            aiResponseText = aiResponseText.replaceAll("```json|```", "").trim();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Parsing the entire AI response as JSON to extract the relevant sections for
        // topology and streams
        JsonNode simConfig = mapper.readTree(aiResponseText);

        // Ensure the config directory exists before writing files
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // topology file creating.
        JsonNode topologyNode = simConfig.get("topology");
        if (topologyNode == null || topologyNode.isMissingNode()) {
            throw new IllegalArgumentException("❌ AI response is missing the required 'topology' object.");
        }

        // wrapping the topology node in an object with a "topology" key to match the
        // expected structure of NetworkConfig
        String topologyJsonContent = "{\"topology\":" + topologyNode.toString() + "}";
        Files.writeString(Paths.get("config/topology.json"), topologyJsonContent);

        // streams file creating.
        JsonNode streamsNode = simConfig.get("streams");
        if (streamsNode == null || streamsNode.isMissingNode()) {
            throw new IllegalArgumentException("❌ AI response is missing the required 'streams' array.");
        }

        // wrapping the streams node in an object with a "streams" key to match the
        // expected structure of TrafficConfig
        String streamsJsonContent = "{\"streams\":" + streamsNode.toString() + "}";
        Files.writeString(Paths.get("config/streams.json"), streamsJsonContent);

        System.out.println("⚡ JSON Configuration files created successfully via Jackson!");
    }

    // Dynamically updates the load on the path from source to destination based on
    // the routing result
    public static void updateLoadOnPath(Network net, HashMap<String, PredecessorAndDistance> routingResult,
            String destinationId, double load) {
        String currentId = destinationId;
        while (currentId != null) {
            PredecessorAndDistance pd = routingResult.get(currentId);
            if (pd == null || pd.getPredecessor() == null) {
                break; // No path or reached the source
            }

            Router predecessor = pd.getPredecessor();
            Router current = net.getRouter(currentId);
            if (current == null) {
                break;
            }

            Line line = predecessor.getNeighbor(current.getId());
            if (line != null) {
                line.setCurrentLoad(line.getCurrentLoad() + load);
            }
            currentId = predecessor.getId();
        }
    }

    private static int promptForInt(Scanner scanner, String prompt, int defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number, using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static void generateRandomNetworkAndTraffic(int routerCount, int streamCount) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        List<Object> routers = new ArrayList<>();
        List<Object> links = new ArrayList<>();
        List<Object> streams = new ArrayList<>();

        for (int i = 1; i <= routerCount; i++) {
            routers.add(Map.of(
                    "id", "R" + i,
                    "x", RANDOM.nextDouble() * 100,
                    "y", RANDOM.nextDouble() * 100));
        }

        // Create a connected backbone first
        for (int i = 1; i < routerCount; i++) {
            links.add(Map.of(
                    "source", "R" + i,
                    "target", "R" + (i + 1),
                    "capacity_mbps", 100 + RANDOM.nextInt(201)));
        }

        // Add random extra medium-capacity links
        int extraLinks = Math.max(routerCount, routerCount / 2);
        for (int i = 0; i < extraLinks; i++) {
            int source = RANDOM.nextInt(routerCount) + 1;
            int target = RANDOM.nextInt(routerCount) + 1;
            if (source == target) {
                target = (source % routerCount) + 1;
            }
            links.add(Map.of(
                    "source", "R" + source,
                    "target", "R" + target,
                    "capacity_mbps", 120 + RANDOM.nextInt(181)));
        }

        for (int i = 1; i <= streamCount; i++) {
            int source = RANDOM.nextInt(routerCount) + 1;
            int destination = RANDOM.nextInt(routerCount) + 1;
            while (destination == source) {
                destination = RANDOM.nextInt(routerCount) + 1;
            }
            streams.add(Map.of(
                    "stream_id", "S" + i,
                    "source", "R" + source,
                    "destination", "R" + destination,
                    "size_mb", 1 + RANDOM.nextDouble() * 4));
        }

        Map<String, Object> topology = Map.of("topology", Map.of("routers", routers, "links", links));
        Map<String, Object> traffic = Map.of("streams", streams);

        Files.writeString(Paths.get("config/topology.json"), mapper.writeValueAsString(topology));
        Files.writeString(Paths.get("config/streams.json"), mapper.writeValueAsString(traffic));
        System.out.println("⚡ Random topology and streams created in config/topology.json and config/streams.json");
    }
}