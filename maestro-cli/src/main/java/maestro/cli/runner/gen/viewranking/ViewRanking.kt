package maestro.cli.runner.gen.viewranking

import maestro.TreeNode
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand

class ViewRanking(override val onPreviousCommandUpdated: (CommandInformation) -> Unit) : CommandSelectionStrategy {

    private val model: MutableMap<String, ActionInformation> = mutableMapOf()

    private val actionHasher = TreeDirectionHasher()

    private var previousAction: String = ""
    private var previousActionCommand: MaestroCommand = MaestroCommand(launchAppCommand = LaunchAppCommand(""))

    override fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean
    ): MaestroCommand {
        val hashedActions = availableCommands.map { (command, node) ->
            actionHasher.hashAction(
                root,
                command,
                node
            ) to command
        }
        if (!newTest) addEdgesToPreviousAction(hashedActions)
        updateModelWithIncomingActions(hashedActions)

        val (bestRankedActionHash, bestRankedAction) = hashedActions.map { (hash, command) ->
            val rank = rank(
                hash,
                command
            )
            hash to (command to rank)
        }.sortedWith(
            compareBy(
                { it.second.second.second },
                { it.second.second.first },
                { it.second.second.third }
            )
        ).map { it.first to it.second.first }.first()

        previousAction = bestRankedActionHash
        if (previousAction in model.keys) {
            model[previousAction] = ActionInformation(
                emptyList(),
                model[previousAction]!!.second + 1
            )
        } else {
            model[previousAction] = ActionInformation(
                emptyList(),
                1
            )
        }
        previousActionCommand = bestRankedAction
        return bestRankedAction
    }

    private fun rank(
        actionHashed: String,
        command: MaestroCommand
    ): ActionRank {
        val priority = priority(command)
        val usages = model[actionHashed]!!.second
        val hopsToUnusedActions = Int.MAX_VALUE
        return Triple(
            priority,
            usages,
            hopsToUnusedActions
        )
    }

    private fun addEdgesToPreviousAction(hashedActions: List<Pair<String, MaestroCommand>>) {
        if (!isEmpty()) {
            onPreviousCommandUpdated(
                CommandInformation(
                    previousActionCommand,
                    previousAction,
                    hashedActions
                )
            )
            val usages = model[previousAction]?.second ?: 1
            model[previousAction] =
                hashedActions.map { (hash, _) -> hash } to usages
        }
    }

    private fun updateModelWithIncomingActions(hashedActions: List<Pair<String, MaestroCommand>>) {
        hashedActions.forEach { (hash, _) ->
            if (hash !in model.keys) model[hash] = emptyList<String>() to 0
        }
    }

    private fun priority(maestroCommand: MaestroCommand): Int {
        return when {
            maestroCommand.tapOnElement != null -> 0
            maestroCommand.inputRandomTextCommand != null -> 1
            maestroCommand.hideKeyboardCommand != null -> 2
            maestroCommand.eraseTextCommand != null -> 2
            maestroCommand.backPressCommand != null -> 3
            else -> 4
        }
    }

    fun isEmpty(): Boolean = model.isEmpty()

    fun edgesFor(
        command: MaestroCommand,
        node: TreeNode?,
        root: TreeNode
    ): ActionInformation? {
        val hash = actionHasher.hashAction(
            root,
            command,
            node
        )
        return model[hash]
    }

}

typealias ActionInformation = Pair<List<String>, Int>
typealias ActionRank = Triple<Int, Int, Int>
