package maestro.cli.runner.gen.model

import java.io.FileOutputStream

class GraphModel<T> {

    private val graph: MutableMap<String, Pair<T, List<Vertex<T>>>> = mutableMapOf()

    fun addNeighborsToVertex(
        vertex: Vertex<T>,
        neighbors: List<Vertex<T>>
    ) {
        graph[vertex.id] = vertex.value to
                if (vertex.id in graph.keys) {
                    val previousNeighbors = graph[vertex.id]?.second ?: emptyList()
                    previousNeighbors + neighbors
                } else {
                    neighbors
                }

    }

    fun toDotFile(labelProducer: (T) -> String) {
        val graphFile = mutableListOf<String>()
        graphFile.add(
            "digraph {\n"
        )
        graph.forEach { (node, edges) ->
            val label = "${node.hashCode()} [label=\"${labelProducer(edges.first)}\"]\n"
            graphFile.add(label)
        }
        graph.forEach { (node, edges) ->
            edges.second.forEach { neighbor ->
                graphFile.add("${node.hashCode()} -> ${neighbor.id.hashCode()}\n")
            }
        }
        graphFile.add("}")
        FileOutputStream("graph.dot").use {
            graphFile.forEach { line ->
                it.write(line.toByteArray())
            }
        }
    }
}
