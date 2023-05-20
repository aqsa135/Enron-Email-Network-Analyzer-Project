# Enron Email Network Analyzer

This Java program analyzes an email network graph built from the Enron email dataset. Each person is represented as a node, and an edge is created when an email is sent/received between two individuals.

key features include:
- Loading emails from a directory and building a graph of the network.
- Finding "connectors" in the network.
- Querying details about a specific email address, including the number of unique email addresses the person has sent to and received from, and the number of email addresses in their team.

## How to Run

```sh
java EnronGraph /path/to/email/directory /optional/path/to/output/file
```

The program will then load the emails, find the connectors, and enter an interactive mode. You can then query specific email addresses for information.

To exit the interactive mode, simply type "EXIT".

## Time Complexity

1. `loadEmails(String path)`: The time complexity of this method is O(N), where N is the number of lines across all files. Each line in each file is read exactly once.

2. `addEdge(String from, String to)`: This method runs in constant time, O(1), as HashMap and HashSet operations (put and add) are usually constant time.

3. `DFS(String node, String parent, boolean isStartingPoint)`: The time complexity of DFS is O(V + E), where V is the number of vertices and E is the number of edges in the graph.

4. `findConnectors()`: This method performs a DFS on each node, so its time complexity is also O(V + E).

5. `queryEmail(String email)`: This method has a time complexity of O(1) because it's merely retrieving and printing information stored in HashMaps and HashSets.

The overall time complexity of the program is largely determined by the DFS methods and the `loadEmails` method. It is proportional to the size of the email dataset.

#Conclusion 
Overall it was an interesting project and I was able to learn a lot over time. 
