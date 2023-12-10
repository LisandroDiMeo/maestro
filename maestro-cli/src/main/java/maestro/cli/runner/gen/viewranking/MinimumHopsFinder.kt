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
    ): Map<String, List<String>> {
        // We keep a modified model that only holds
        // the relationships between nodes, and add a final vertex
        // that will be linked from all the targets
        // so we can run BFS from each source to only one target
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
