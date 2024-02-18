package maestro.cli.runner.gen.presentation.model

import maestro.cli.runner.gen.TestSuiteGeneratorLogger
import org.slf4j.Logger
import java.io.File
import java.io.FileOutputStream

/**
 * This class represents a graph where its vertices are of type [T].
 * This graph is tied to produce a graphical model using .dot format
 * and SVG images.
 */
class GraphModel<T>(
    val path: String = "",
    private val logger: Logger = TestSuiteGeneratorLogger.logger
) {

    private val graph: MutableMap<String, Pair<T, List<Vertex<T>>>> = mutableMapOf()

    /**
     * Add to the graph a vertex with its given neighbors.
     * Note that if the vertex existed before, it will override previous neighbors.
     */
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

    private fun completeGraphWithUnusedNodes() {
        val nodesToAdd = mutableListOf<Vertex<T>>()
        graph.values.forEach { (_, neighbours) ->
            neighbours.forEach { neighbour ->
                if (neighbour.id !in graph.keys) {
                    nodesToAdd.add(neighbour)
                }
            }
        }
        nodesToAdd.forEach {
            addNeighborsToVertex(
                it,
                emptyList()
            )
        }
    }

    /**
     * Produce the dot file and also the SVG for the model.
     */
    fun toDotFile(
        labelProducer: (T) -> String,
        colorProducer: (String) -> String,
        usagesCountProducer: (String) -> String
    ) {
        logger.info("Building visual graph model 🖼️")
        //        val prunedGraph = graph.mapValues { (_, value) ->
        //            val usedVertices = value.second.filter { it.id in graph.keys }
        //            value.first to usedVertices
        //        }
        completeGraphWithUnusedNodes()

        val graphFile = mutableListOf<String>()
        graphFile.add(
            "digraph {\n"
        )
        graph.forEach { (node, edges) ->
            val label =
                "${node.hashCode()} [style=filled, fillcolor=\"${colorProducer(node)}\", label=\"${labelProducer(edges.first)}\", usages=\"${usagesCountProducer(node)}\"]\n"
            graphFile.add(label)
        }
        graph.forEach { (node, edges) ->
            edges.second.forEach { neighbor ->
                graphFile.add("${node.hashCode()} -> ${neighbor.id.hashCode()}\n")
            }
        }
        graphFile.add("}")
        writeDotFile(graphFile)
        generateGraphInSVG()
    }

    private fun writeDotFile(graphFile: MutableList<String>) {
        FileOutputStream("$path/graph.dot").use {
            graphFile.toSet().forEach { line ->
                it.write(line.toByteArray())
            }
        }
    }

    private fun generateGraphInSVG() {
        val processBuilder = ProcessBuilder()
        val outputFile = File("$path/output.svg")
        processBuilder.command(
            listOf(
                "dot",
                "-Tsvg",
                "$path/graph.dot"
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

