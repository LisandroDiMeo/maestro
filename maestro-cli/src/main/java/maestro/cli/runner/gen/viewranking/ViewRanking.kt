package maestro.cli.runner.gen.viewranking

import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewranking.encoding.ActionEncoder
import maestro.cli.runner.gen.viewranking.encoding.ActionIdentifier
import maestro.cli.runner.gen.viewranking.encoding.ScreenEncoder
import maestro.cli.runner.gen.viewranking.encoding.ScreenIdentifier
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand
import kotlin.random.Random

class ViewRanking(
    private val screenEncoder: ScreenEncoder = ScreenEncoder(),
    private val actionEncoder: ActionEncoder = ActionEncoder(),
    private val random: Random = Random(1024L)
) : CommandSelectionStrategy {

    private val model: MutableMap<ScreenIdentifier, List<ActionIdentifier>> = mutableMapOf()
    private val ranking: MutableMap<ScreenIdentifier, MutableMap<ActionIdentifier, Double>> =
        mutableMapOf()
    private var lastActionExecuted: ActionIdentifier? = null
    private var lastScreenVisited: ScreenIdentifier? = null

    override fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand {
        val actionsIdentifier = availableCommands.map { actionEncoder.encode(it) }
        val screenIdentifier = screenEncoder.encode(availableCommands)
        var previouslySeenScreen: ScreenIdentifier? = null
        model.forEach { (screenId, actionsId) ->
            if (actionsId == actionsIdentifier) {
                previouslySeenScreen = screenId
            }
        }
        previouslySeenScreen?.let { screenId ->
            model[screenId] = actionsIdentifier
            val highest = highestRankAction(availableCommands, screenId)
            val actionIdentifier = actionEncoder.encode(highest)
            lastActionExecuted = actionIdentifier
            lastScreenVisited = screenIdentifier
            ranking[screenId]!![actionIdentifier] = rank(highest, screenId)
            return highest
        } ?: run {
            model[screenIdentifier] = actionsIdentifier
            ranking[screenIdentifier] = mutableMapOf()
            availableCommands.forEach {
                ranking[screenIdentifier]!![actionEncoder.encode(it)] = INITIAL_RANKING
            }
            val highest = highestRankAction(availableCommands, screenIdentifier)
            val actionIdentifier = actionEncoder.encode(highest)
            lastActionExecuted = actionIdentifier
            lastScreenVisited = screenIdentifier
            ranking[screenIdentifier]!![actionIdentifier] = rank(highest, screenIdentifier)
            return highest
        }
    }

    private fun highestRankAction(
        actions: List<MaestroCommand>,
        screenIdentifier: ScreenIdentifier
    ): MaestroCommand =
        actions.map { action -> action to rank(action, screenIdentifier) }
            .maxByOrNull { it.second }!!.first

    private fun rank(command: MaestroCommand, screenIdentifier: ScreenIdentifier): Double {
        val actionId = actionEncoder.encode(command)
        if (actionId in ranking[screenIdentifier]!!) {
            return (ranking[screenIdentifier]!![actionId] ?: 0.0) * 0.90
        }
        var rank = ranking[screenIdentifier]!![actionId]!!
        when {
            command.tapOnElement != null -> {
                val selector = command.tapOnElement!!.selector
                selector.idRegex?.let { rank *= 1.20 }
                selector.textRegex?.let { rank *= 1.15 }
                selector.containsChild?.let { rank *= 1.10 }
                selector.classNameRegex?.let { rank *= 1.05 }
                selector.packageNameRegex?.let { rank *= 1.05 }
            }

            command.backPressCommand != null -> {
                rank *= 1.10
            }

            command.inputTextCommand != null -> {
                rank *= 1.35
            }

            command.hideKeyboardCommand != null -> {
                rank *= 1.10
            }

            command.scrollCommand != null -> {
                rank *= 1.10
            }
        }
        return rank
    }

    fun printRanking() {
        ranking.forEach { (screenId, screenRank) ->
            println("===== Ranking for $screenId =====")
            screenRank.forEach { (actionId, actionRank) ->
                println("$actionId -> $actionRank")
            }
        }
    }

    companion object {
        private const val INITIAL_RANKING = 0.5
    }

}


fun main() {
    val viewRanking = ViewRanking()
    val initalCommands = listOf(
        MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(
                    idRegex = "id1"
                )
            )
        ),
        MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(
                    textRegex = "hola"
                )
            )
        ),
        MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(
                    classNameRegex = "className"
                )
            )
        ),
        MaestroCommand(backPressCommand = BackPressCommand()),
        MaestroCommand(scrollCommand = ScrollCommand()),
    )
    val selected = viewRanking.pickFrom(initalCommands)
    viewRanking.printRanking()
    println(selected)
    val otherCommands = listOf(
        MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(
                    idRegex = "id2"
                )
            )
        ),
        MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(
                    classNameRegex = "className"
                )
            )
        ),
        MaestroCommand(backPressCommand = BackPressCommand()),
        MaestroCommand(scrollCommand = ScrollCommand()),
    )
    val selected2 = viewRanking.pickFrom(otherCommands)
    viewRanking.printRanking()
    println(selected2)

}


