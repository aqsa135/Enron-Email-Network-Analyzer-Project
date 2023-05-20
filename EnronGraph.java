import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * This class models an email network graph from the Enron email dataset.
 * Each person is a node, and an edge represents a sent/received email between two individuals.
 */

public class EnronGraph {

    private Map<String, Set<String>> adjacencyList;
    private Map<String, Boolean> visited;
    private Map<String, Integer> dfsNum, back;
    private Map<String, List<String>> parentChildren;
    private int time;
    private Set<String> connectors;
    private Map<String, Set<String>> sentEmails;
    private Map<String, Set<String>> receivedEmails;


    /**
     * Constructor for the EnronGraph class.
     * Initializes all the maps and the set.
     */
    public EnronGraph() {
//        this.adjacencyList = new HashMap<>();
        this.visited = new HashMap<>();
        this.dfsNum = new HashMap<>();
        this.back = new HashMap<>();
        this.parentChildren = new HashMap<>();
        this.connectors = new HashSet<>();
        this.time = 0;
        this.adjacencyList = new HashMap<>();
        this.sentEmails = new HashMap<>();
        this.receivedEmails = new HashMap<>();
    }
    /**
     * Add an edge in the graph.
     *
     * @param from Sender email address.
     * @param to Receiver email address.
     */
    public void addEdge(String from, String to) {

        // Add 'from' and 'to' to each other's adjacency lists.

        adjacencyList.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        adjacencyList.computeIfAbsent(to, k -> new HashSet<>()).add(from);
        // Update sent and received emails maps.
        sentEmails.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        receivedEmails.computeIfAbsent(to, k -> new HashSet<>()).add(from);
    }

    /**
     * Loads emails from a directory and builds the graph.
     *
     * @param path The directory path.
     * @throws IOException If an I/O error occurs reading from the file
     */
    public void loadEmails(String path) throws IOException {
        File folder = new File(path);
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                loadEmails(file.getPath());
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                String from = null;
                List<String> toList = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    Matcher fromMatcher = Pattern.compile("From: ([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})").matcher(line);
                    Matcher toMatcher = Pattern.compile("To: (.*)").matcher(line);
                    Matcher newEmailMatcher = Pattern.compile("Message-ID: (.*)").matcher(line);

                    if (newEmailMatcher.find()) {
                        // Reset 'from' and 'toList' at the start of a new email
                        from = null;
                        toList.clear();
                    } else if (fromMatcher.find()) {
                        from = fromMatcher.group(1);
                    } else if (toMatcher.find()) {
                        String[] toEmails = toMatcher.group(1).split(",");
                        for(String email : toEmails) {
                            if(email.trim().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                                toList.add(email.trim());
                            }
                        }
                    }

                    if (from != null && !toList.isEmpty()) {
                        for (String to : toList) {
                            addEdge(from, to);
                        }
                        // Reset 'from' and 'toList' after adding edges
                        from = null;
                        toList.clear();
                    }
                }
                reader.close();
            }
        }
    }

    /**
     * A private method to perform DFS from a given node.
     *
     * @param node - The node from which to start the DFS.
     * @param parent - The parent of the node in the DFS tree.
     * @param isStartingPoint - True if the node is the starting point of the DFS, false otherwise.
     */
    public void DFS(String node, String parent, boolean isStartingPoint) {
        visited.put(node, true);
        dfsNum.put(node, time);
        back.put(node, time);
        time++;

        int children = 0;
        for (String neighbor : adjacencyList.get(node)) {
            if (!neighbor.equals(parent)) {
                if (visited.get(neighbor) == null || !visited.get(neighbor)) {
                    parentChildren.computeIfAbsent(node, k -> new ArrayList<>()).add(neighbor);
                    children++;
                    DFS(neighbor, node, false);

                    // Check if the current node is a connector
                    if ((parent != null && dfsNum.get(node) <= back.get(neighbor)) ||
                            (parent == null && children > 1) || (isStartingPoint && children > 0)) {
                        connectors.add(node);
                    }

                    back.put(node, Math.min(back.get(node), back.get(neighbor)));
                } else {
                    back.put(node, Math.min(back.get(node), dfsNum.get(neighbor)));
                }
            }
        }
    }



    public Set<String> findConnectors() {
        for (String node : adjacencyList.keySet()) {
            if (visited.get(node) == null || !visited.get(node)) {
                DFS(node, null,true);
            }
        }

        // Print the number of connectors
        System.out.println("Number of Connectors: " + connectors.size());

        // Print connectors
        for (String connector : connectors) {
            System.out.println("Connector: " + connector);
        }

        return connectors;
    }

    /**
     * Prints details about a specific email address, including the number of unique email addresses sent to, received from,
     * and the number of email addresses in the same team.
     *
     * @param email - The email address to be queried.
     */

    public void queryEmail(String email) {
        int sent = sentEmails.getOrDefault(email, new HashSet<>()).size();
        int received = receivedEmails.getOrDefault(email, new HashSet<>()).size();
        int team = adjacencyList.getOrDefault(email, new HashSet<>()).size();

        System.out.printf("Details for %s:\n", email);
        System.out.printf("Number of unique email addresses sent to: %d\n", sent);
        System.out.printf("Number of unique email addresses received from: %d\n", received);
        System.out.printf("Number of email addresses in the same team: %d\n", team);
    }

    public static void main(String[] args) {
        try {
            // Set up Scanner to read user input
            Scanner scanner = new Scanner(System.in);
            String input;

            EnronGraph enronGraph = new EnronGraph();
            enronGraph.loadEmails(args[0]);
            Set<String> connectors = enronGraph.findConnectors();
            // Write connectors to file
            if (args.length > 1) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {
                    for (String connector : connectors) {
                        writer.write(connector);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Interact with the user
            while (true) {
                System.out.println("Email address of the individual (or EXIT to quit): ");
                input = scanner.nextLine();

                if ("EXIT".equalsIgnoreCase(input)) {
                    System.out.println("Goodbye!");
                    break;
                }

                // Handle the email search
                if (enronGraph.adjacencyList.containsKey(input)) {
                    enronGraph.queryEmail(input);
                } else {
                    System.out.println("Email address (" + input + ") not found in the dataset.");
                }
            }

            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}