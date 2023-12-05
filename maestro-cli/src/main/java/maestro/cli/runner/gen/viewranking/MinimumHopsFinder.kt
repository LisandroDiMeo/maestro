package maestro.cli.runner.gen.viewranking

/**
 * Method Object to find the paths from a set of sources to a set
 * of targets on a graph model. If no path exists for a given source,
 * it will be skipped.
 * TLDR, it's the shortest path for a directed graph multiple sources
 * to multiple targets.
 */
object MinimumHopsFinder {
    fun minimumHopsForSources(
        model: Map<String, ActionInformation>,
        sources: List<String>,
        targets: List<String>
    ): MutableMap<String, List<String>> {
        // We keep a modified model that only holds
        // the relationships between nodes, and add a final vertex
        // that will be linked from all the targets
        // so we can run Dijkstra from each source to only one target
        val adjacencyList = mutableMapOf<String, List<String>>()
        adjacencyList["target"] = emptyList()
        model.forEach { (action, actionInformation) ->
            if (action in targets) {
                adjacencyList[action] = actionInformation.first + listOf("target")
            } else adjacencyList[action] = actionInformation.first
        }
        val pathsToTarget = mutableMapOf<String, List<String>>()
        val queue = mutableListOf<String>()
        sources.forEach { source ->
            queue.add(source)
            val visitedNodes: MutableMap<String, Boolean> = mutableMapOf()
            visitedNodes[source] = true
            val path = mutableMapOf<String, String>()
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                val neighbors = adjacencyList[node]
                neighbors?.forEach { neighbor ->
                    if (neighbor !in visitedNodes.keys) {
                        queue.add(neighbor)
                        visitedNodes[neighbor] = true
                        path[neighbor] = node
                    }
                }
            }
            if ("target" in path.keys) {
                pathsToTarget[source] = buildPath(
                    path,
                    source
                )
            }
        }

        return pathsToTarget
    }

    private fun buildPath(
        pathMap: Map<String, String>,
        source: String
    ): List<String> {
        var current = pathMap["target"]!!
        val path = mutableListOf<String>()
        while (current != source) {
            path.add(current)
            current = pathMap[current]!!
        }
        path.add(source)
        path.reverse()
        return path.toList()
    }
}

fun main() {
    val m = mapOf(
        "1" to ActionInformation(
            listOf(
                "4",
                "5"
            ),
            1
        ),
        "2" to ActionInformation(
            listOf(
                "3",
                "4"
            ),
            1
        ),
        "9" to ActionInformation(
            listOf(),
            1
        ),
        "3" to ActionInformation(
            listOf("4"),
            1
        ),
        "4" to ActionInformation(
            listOf("5"),
            1
        ),
        "5" to ActionInformation(
            listOf("6"),
            1
        ),
        "6" to ActionInformation(
            listOf("7"),
            1
        ),
        "7" to ActionInformation(
            listOf(),
            1
        ),
    )

    val hops = MinimumHopsFinder.minimumHopsForSources(
        m,
        listOf(
            "1",
            "2",
            "3",
            "9"
        ),
        listOf(
            "6",
            "7"
        )
    )
    hops.forEach { (k, v) -> println("Path from $k is $v") }
}
