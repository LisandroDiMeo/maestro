package maestro.cli.runner.gen.viewranking

import maestro.TreeNode
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.MaestroCommand

class ViewRanking : CommandSelectionStrategy {

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
        return bestRankedAction
    }

    private fun rank(
        actionHashed: String,
        command: MaestroCommand
    ): ActionRank {
        val priority = priority(command)
        val unused = if (model[actionHashed]!!.second) 1 else 0
        val hopsToUnusedActions = Int.MAX_VALUE
        return Triple(
            priority,
            unused,
            hopsToUnusedActions
        )
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

    fun edgesFor(command: MaestroCommand, node: TreeNode?, root: TreeNode): ActionInformation? {
        val hash = actionHasher.hashAction(
            root,
            command,
            node
        )
        return model[hash]
    }

}

typealias ActionInformation = Pair<List<String>, Boolean>
typealias ActionRank = Triple<Int, Int, Int>


