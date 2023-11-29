package maestro.cli.runner.gen.model

import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.orchestra.MaestroCommand

class SearchModel {
    private val graphModel = GraphModel<MaestroCommand>()
    private val labelProducer: (MaestroCommand) -> String = {
        it.description()
    }

    fun updateModel(
        commandInformation: CommandInformation
    ) {
        val vtx = Vertex(
            commandInformation.commandExecuted,
            commandInformation.hash
        )
        graphModel.addNeighborsToVertex(
            vtx,
            commandInformation.destinations.map { (hash, command) ->
                Vertex(
                    command,
                    hash
                )
            })
    }

    fun outputModel() = graphModel.toDotFile(labelProducer)
}

