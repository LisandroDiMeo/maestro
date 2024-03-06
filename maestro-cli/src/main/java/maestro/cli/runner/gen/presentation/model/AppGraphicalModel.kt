package maestro.cli.runner.gen.presentation.model

import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import java.lang.Integer.toHexString
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * This class allows you to build a graphical model of the explored app
 * (a.k.a. the application from which the test cases are generated).
 * @see GraphModel
 */
class AppGraphicalModel(val path: String) {
    private val graphModel = GraphModel<MaestroCommand>(path = path)
    private val labelProducer: (MaestroCommand) -> String = {
        val description = it.description().replace(
            "\"",
            ""
        )
        val contentLabel =
            if (description.split(".").size >= 3)
                "Tap on ${description.split(".").last().take(30)}"
            else
                description.take(30)
        if ("Tap" in description) description else contentLabel
    }
    private val usagesCount = mutableMapOf<String, Int>()
    private val usagesCountProducer: (String) -> String = { actionId ->
        (usagesCount[actionId] ?: 0).toString()
    }

    private val colorProducer: (String) -> String = { actionId ->
        val usageCountForAction = (usagesCount[actionId] ?: 0).toFloat()
        if (usageCountForAction == 0f) {
            "#ffffff"
        } else {
            if(usagesCount.values.maxOrNull() == 1){
                lightenRedColor(100)
            } else {
                val divider = max(usagesCount.values.maxOf { usages -> usages }, 1).toFloat()
                val usagesPercentageForAction =
                    usageCountForAction / divider
                val usagesPercentageForActionReversed = 100f - usagesPercentageForAction * 100f
                lightenRedColor(usagesPercentageForActionReversed.roundToInt())
            }
        }
    }

    fun updateModel(
        commandInformation: CommandInformation
    ) {
        usagesCount[commandInformation.hash] = commandInformation.usages
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
            }
        )
    }

    /**
     * Output the graphical model using the .dot and SVG format.
     */
    fun outputModel() = graphModel.toDotFile(
        labelProducer,
        colorProducer,
        usagesCountProducer
    )
}


private fun lightenRedColor(percentage: Int, red: Int = 201): String {
    val othersColorsPercentage = percentage + 36
    val hexString = toHexString(othersColorsPercentage)
    val padding = if (hexString.length == 1) "0" else ""
    return "#${toHexString(red)}$padding$hexString$padding$hexString"
}


private fun generateMockModel() {
    val model = AppGraphicalModel("/Users/lisandrodimeo/Documents/Me/maestro")
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
