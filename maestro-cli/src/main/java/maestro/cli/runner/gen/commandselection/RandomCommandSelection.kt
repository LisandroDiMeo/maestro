package maestro.cli.runner.gen.commandselection

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class RandomCommandSelection(
    override val onPreviousCommandUpdated: (CommandInformation) -> Unit,
    private val random: Random = Random(1234)
) : CommandSelectionStrategy {

    private val actionHasher = TreeDirectionHasher()

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
        if (availableCommands.isEmpty()) throw CommandSelectionStrategy.UnableToPickCommand
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
        onPreviousCommandUpdated(
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
