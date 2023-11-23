package maestro.cli.runner.gen.model

import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.orchestra.MaestroCommand

class SearchModel {
    private val graphModel = GraphModel<MaestroCommand>()

    fun updateModel(
        action: CommandInformation
    ) {
        val vtx = Vertex(
            action.commandExecuted,
            action.hash
        )
        graphModel.addNeighborsToVertex(
            vtx,
            action.destinations.map { (hash, command) ->
                Vertex(
                    command,
                    hash
                )
            })
    }

    fun outputModel() = graphModel.toDOT()
}

