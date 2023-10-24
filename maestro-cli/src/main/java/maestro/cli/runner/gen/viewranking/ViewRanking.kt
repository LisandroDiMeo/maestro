package maestro.cli.runner.gen.viewranking

import maestro.TreeNode
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class ViewRanking(
    private val random: Random = Random(1024L)
) : CommandSelectionStrategy {

    private val model: MutableMap<String, ActionInformation> = mutableMapOf()

    private val actionHasher = TreeDirectionHasher()

    private var previousAction: String = ""

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
        addEdgesToPreviousAction(hashedActions)
        updateModelWithIncomingActions(hashedActions)
        val actionToExecute = hashedActions
            .map { (hash, command) -> hash to (priority(command) to command) }
            .sortedBy { (hash, priorityAndCommandPair) ->
                val (priority, _) = priorityAndCommandPair
                if (model[hash]?.second == true) {
                    Int.MAX_VALUE
                } else priority
            }
            .map { (hash, priorityAndCommandPair) -> hash to priorityAndCommandPair.second }
            .first()
        previousAction = actionToExecute.first
        return actionToExecute.second
    }

    private fun addEdgesToPreviousAction(hashedActions: List<Pair<String, MaestroCommand>>) {
        if (!isEmpty()) {
            model[previousAction] = hashedActions.map { (hash, _) -> hash } to true
        }
    }

    private fun updateModelWithIncomingActions(hashedActions: List<Pair<String, MaestroCommand>>) {
        hashedActions.forEach { (hash, _) ->
            if (hash !in model.keys) model[hash] = emptyList<String>() to false
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

}

typealias ActionInformation = Pair<List<String>, Boolean>


