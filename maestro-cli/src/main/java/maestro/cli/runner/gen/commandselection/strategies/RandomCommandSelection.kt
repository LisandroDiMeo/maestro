package maestro.cli.runner.gen.commandselection.strategies

import maestro.TreeNode
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.cli.runner.gen.viewranking.actionhash.ActionHasher
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

/**
 * Strategy that randomly chooses given commands.
 */
class RandomCommandSelection(
    override val actionHasher: ActionHasher = TreeDirectionHasher(),
    private val random: Random = Random(1234),
    override val onPreviousCommandExecuted: (CommandInformation) -> Unit
) : CommandSelectionStrategy(actionHasher) {

    private val launchAppCommand = MaestroCommand(launchAppCommand = LaunchAppCommand(""))
    private val launchAppCommandHash = actionHasher.hashAction(TreeNode(), launchAppCommand, null)
    private var previousAction: String = ""
    private var previousActionCommand: MaestroCommand = launchAppCommand

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
        onPreviousCommandExecuted(
            CommandInformation(
                previousActionCommand,
                previousAction,
                hashedActions
            )
        )
        val actionToExecute = hashedActions.random(random)
        previousActionCommand = actionToExecute.second
        previousAction = actionToExecute.first
        return actionToExecute.second
    }


}
