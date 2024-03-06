package maestro.cli.runner.gen.commandselection.strategies.random

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

/**
 * Strategy that randomly chooses given commands.
 */
class RandomCommandSelection(
    override val actionHasher: ActionHasher = TreeDirectionHasher(),
    private val random: Random = Random(1234),
    override val executedCommandsObservable: ExecutedCommandsObservable = ExecutedCommandsObservable()
) : CommandSelectionStrategy(
    actionHasher,
    executedCommandsObservable
) {

    private val usagesForAction: MutableMap<String, Int> = mutableMapOf()

    init {
        previousAction = launchAppCommandHash
        usagesForAction[launchAppCommandHash] = 1
    }

    override fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand {
        if (availableCommands.isEmpty()) throw UnableToPickCommand
        val hashedActions = hashActions(root, availableCommands)
        performUpdateForObservable(newTest, hashedActions)

        if (wasLastActionForTest) return MaestroCommand()

        val actionToExecute = hashedActions.random(random)
        previousActionCommand = actionToExecute.second
        previousAction = actionToExecute.first
        updateUsagesForActionToExecute(actionToExecute.first)
        return actionToExecute.second
    }

    override fun updateUsagesForActionToExecute(actionToPerform: String) {
        if (actionToPerform in usagesForAction.keys) {
            usagesForAction[actionToPerform] = usagesForAction[actionToPerform]!! + 1
        } else {
            usagesForAction[actionToPerform] = 1
        }
    }

    override fun usagesForAction(actionHash: String): Int {
        return usagesForAction[actionHash] ?: 1
    }


}
