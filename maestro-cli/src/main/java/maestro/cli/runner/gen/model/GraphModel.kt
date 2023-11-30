package maestro.cli.runner.gen.model

import maestro.cli.runner.gen.TestSuiteGeneratorLogger
import java.io.File
import java.io.FileOutputStream

class GraphModel<T> {

    private val graph: MutableMap<String, Pair<T, List<Vertex<T>>>> = mutableMapOf()
    private val logger = TestSuiteGeneratorLogger.logger

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
        logger.info("Building visual graph model 🖼️")
        val prunedGraph = graph.mapValues { (_, value) ->
            val usedVertices = value.second.filter { it.id in graph.keys }
            value.first to usedVertices
        }

        val graphFile = mutableListOf<String>()
        graphFile.add(
            "digraph {\n"
        )
        prunedGraph.forEach { (node, edges) ->
            val label = "${node.hashCode()} [label=\"${labelProducer(edges.first)}\"]\n"
            graphFile.add(label)
        }
        prunedGraph.forEach { (node, edges) ->
            edges.second.forEach { neighbor ->
                graphFile.add("${node.hashCode()} -> ${neighbor.id.hashCode()}\n")
            }
        }
        graphFile.add("}")
        writeDotFile(graphFile)
        generateGraphInSVG()
    }

    private fun writeDotFile(graphFile: MutableList<String>) {
        FileOutputStream("graph.dot").use {
            graphFile.forEach { line ->
                it.write(line.toByteArray())
            }
        }
    }

    private fun generateGraphInSVG() {
        val processBuilder = ProcessBuilder()
        val outputFile = File("output.svg")
        processBuilder.command(
            listOf(
                "dot",
                "-Tsvg",
                "graph.dot"
            )
        )
        processBuilder.redirectOutput(outputFile)
        val startedProcess = processBuilder.start()
        startedProcess.waitFor()
        if (startedProcess.exitValue() == 0) {
            logger.info("Successfully generated visual graph model ✅")
        }
    }
}
