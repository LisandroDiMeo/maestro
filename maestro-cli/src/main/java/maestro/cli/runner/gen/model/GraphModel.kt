package maestro.cli.runner.gen.model

import maestro.orchestra.MaestroCommand
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

    fun toDOT() {
        val graphFile = mutableListOf<String>()
        graphFile.add(
            "digraph {\n"
        )
        graph.forEach { (node, edges) ->
            // TODO: FIX THIS TO NOT USE AS MAESTRO COMMAND !
            val label = "${node.hashCode()} [label=\"${(edges.first as MaestroCommand).description()}\"]\n"
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
