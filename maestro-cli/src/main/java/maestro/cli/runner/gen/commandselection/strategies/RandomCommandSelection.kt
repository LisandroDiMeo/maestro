package maestro.cli.runner.gen.commandselection.strategies

import maestro.TreeNode
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.cli.runner.gen.model.ExecutedCommandsObservable
import maestro.cli.runner.gen.viewranking.actionhash.ActionHasher
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

/**
 * Strategy that randomly chooses given commands.
 */
class RandomCommandSelection(
    override val actionHasher: ActionHasher = TreeDirectionHasher(),
    private val random: Random = Random(1234),
    val executedCommandsObservable: ExecutedCommandsObservable = ExecutedCommandsObservable()
) : CommandSelectionStrategy(
    actionHasher,
    executedCommandsObservable
) {

    private val usagesForAction: MutableMap<String, Int> = mutableMapOf()

    init {
        usagesForAction[launchAppCommandHash] = 1
    }

    override fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand {
        if (availableCommands.isEmpty()) throw UnableToPickCommand
        val hashedActions = availableCommands.map { (command, node) ->
            actionHasher.hashAction(
                root,
                command,
                node
            ) to command
        }
        if (newTest) {
            previousAction = launchAppCommandHash
            previousActionCommand = launchAppCommand
        }
        executedCommandsObservable.performUpdate(
            CommandInformation(
                previousActionCommand,
                previousAction,
                hashedActions,
                usages = usagesForAction[previousAction] ?: 1
            )
        )
        if (wasLastActionForTest) return MaestroCommand()
        val actionToExecute = hashedActions.random(random)
        previousActionCommand = actionToExecute.second
        previousAction = actionToExecute.first
        if (previousAction in usagesForAction.keys) {
            usagesForAction[previousAction] = usagesForAction[previousAction]!! + 1
        } else {
            usagesForAction[previousAction] = 1
        }
        return actionToExecute.second
    }


}
