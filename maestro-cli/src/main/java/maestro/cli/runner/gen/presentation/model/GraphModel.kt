package maestro.cli.runner.gen.presentation.model

import com.google.gson.Gson
import maestro.cli.runner.gen.TestSuiteGeneratorLogger
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import org.slf4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.Serializable
import java.io.Writer
import java.lang.Appendable

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
    private val discoveredVertices = mutableSetOf<String>()
    private val executedNodes = mutableSetOf<String>()
    private val snapshotSequence = mutableListOf<Pair<Int, ModelSnapshot<T>>>()
    private var step = 1

    /**
     * Add to the graph a vertex with its given neighbors.
     */
    fun addNeighborsToVertex(
        vertex: Vertex<T>,
        neighbors: List<Vertex<T>>,
        addToSequence: Boolean = true
    ) {
        graph[vertex.id] = vertex.value to
                if (vertex.id in graph.keys) {
                    val previousNeighbors = graph[vertex.id]?.second ?: emptyList()
                    previousNeighbors + neighbors
                } else {
                    neighbors
                }
        if (addToSequence) {
            discoveredVertices.add(vertex.id)
            executedNodes.add(vertex.id)
            neighbors.forEach { discoveredVertices.add(it.id) }
            val snapshot = ModelSnapshot<T>(
                step = this.step,
                discoveredNodes = discoveredVertices.size,
                executedNodes = executedNodes.size,
                snapshot = null
            )
            snapshotSequence.add(step to snapshot)
            step += 1
        }
    }

    private fun outputSequence() {
        val fileWriter = FileWriter("$path/graphGenerationSequence.json")
        fileWriter.use {
            Gson().toJson(snapshotSequence, it)
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
                emptyList(),
                false
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
        outputSequence()
        completeGraphWithUnusedNodes()

        val graphFile = mutableListOf<String>()
        graphFile.add(
            "digraph {\n"
        )
        graph.forEach { (node, edges) ->
            val label =
                "${node.hashCode()} [style=filled, fillcolor=\"${colorProducer(node)}\", label=\"${
                    labelProducer(
                        edges.first
                    )
                }\", usages=\"${usagesCountProducer(node)}\"]\n"
            graphFile.add(label)
        }
        graph.forEach { (node, edges) ->
            edges.second.forEach { neighbor ->
                graphFile.add("${node.hashCode()} -> ${neighbor.id.hashCode()}\n")
            }
        }
        graphFile.add("}")
        writeDotFile(graphFile)
        // TODO: Uncomment this to actually generate the svg
        //  For the experiment phase we are not using it
        //  since it may take a lot of time to render a huge graph
        //  and even sometimes it's impossible
        // generateGraphInSVG()
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

data class ModelSnapshot<T>(
    val step: Int,
    val executedNodes: Int,
    val discoveredNodes: Int,
    val snapshot: Map<String, Pair<T, List<Vertex<T>>>>?
)
