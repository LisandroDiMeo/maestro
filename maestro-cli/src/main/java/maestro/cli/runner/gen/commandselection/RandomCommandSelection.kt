package maestro.cli.runner.gen.commandselection

import maestro.TreeNode
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class RandomCommandSelection(
    override val onPreviousCommandUpdated: (CommandInformation) -> Unit,
    private val random: Random = Random(1234)
) : CommandSelectionStrategy {

    override fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean
    ): MaestroCommand {
        if (availableCommands.isEmpty()) throw CommandSelectionStrategy.UnableToPickCommand
        return availableCommands.map { it.first }.random(random)
    }


}
