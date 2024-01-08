package maestro.cli.runner.gen.model

import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import okhttp3.internal.toHexString
import kotlin.math.roundToInt

class SearchModel {
    private val graphModel = GraphModel<MaestroCommand>()
    private val labelProducer: (MaestroCommand) -> String = {
        val description = it.description().replace(
            "\"",
            ""
        )
        description.split(".").last().take(30)
    }
    private val usagesCount = mutableMapOf<String, Int>()

    private val colorProducer: (String) -> String = { actionId ->
        val usageCountForAction = usagesCount[actionId] ?: 0
        val usagesPercentageForAction =
            usageCountForAction.toFloat() / usagesCount.values.maxOf { usages -> usages }.toFloat()
        val usagesPercentageForActionReversed = 100f - usagesPercentageForAction * 100f
        lightenRedColor(usagesPercentageForActionReversed.roundToInt())
    }

    fun updateModel(
        commandInformation: CommandInformation
    ) {
        usagesCount[commandInformation.hash] = commandInformation.usages
        val vtx = Vertex(
            commandInformation.commandExecuted,
            commandInformation.hash
        )
        graphModel.addNeighborsToVertex(vtx,
                                        commandInformation.destinations.map { (hash, command) ->
                                            Vertex(
                                                command,
                                                hash
                                            )
                                        })
    }

    fun outputModel() = graphModel.toDotFile(
        labelProducer,
        colorProducer
    )
}

private fun lightenRedColor(percentage: Int): String {
    val percentageToLighten = ((255f / 100f) * percentage).toInt()
    val padding = if (percentage < 8) {
        "0"
    } else ""
    val hexString =
        padding + percentageToLighten.toHexString() + padding + percentageToLighten.toHexString()

    return "#ff$hexString"
}

private fun generateMockModel() {
    val model = SearchModel()
    model.updateModel(
        CommandInformation(
            commandExecuted = MaestroCommand(
                tapOnElement = TapOnElementCommand(ElementSelector("hello"))
            ),
            hash = "A",
            destinations = listOf(
                "B" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello2"))
                ),
                "C" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello3"))
                ),
            ),
            usages = 5
        )
    )
    model.updateModel(
        CommandInformation(
            commandExecuted = MaestroCommand(
                tapOnElement = TapOnElementCommand(ElementSelector("hello2"))
            ),
            hash = "B",
            destinations = listOf(
                "A" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello"))
                ),
            ),
            usages = 3
        )
    )
    model.updateModel(
        CommandInformation(
            commandExecuted = MaestroCommand(
                tapOnElement = TapOnElementCommand(ElementSelector("hello"))
            ),
            hash = "A",
            destinations = listOf(
                "B" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello2"))
                ),
                "C" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello3"))
                ),
            ),
            usages = 6
        )
    )
    model.updateModel(
        CommandInformation(
            commandExecuted = MaestroCommand(
                tapOnElement = TapOnElementCommand(ElementSelector("hello3"))
            ),
            hash = "C",
            destinations = listOf(
                "A" to MaestroCommand(
                    tapOnElement = TapOnElementCommand(ElementSelector("hello"))
                ),
            ),
            usages = 1
        )
    )
    model.outputModel()
}
