package maestro.cli.runner.gen.commandselection

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.ViewRanking
import maestro.orchestra.MaestroCommand

interface CommandSelectionStrategy {
    @kotlin.jvm.Throws(UnableToPickCommand::class)
    fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean
    ): MaestroCommand

    object UnableToPickCommand : Exception()

    companion object {
        fun strategyFor(strategy: String): CommandSelectionStrategy {
            return when (strategy.lowercase()) {
                "random" -> RandomCommandSelection()
                "viewranking" -> ViewRanking()
                else -> RandomCommandSelection()
            }
        }
    }
}
