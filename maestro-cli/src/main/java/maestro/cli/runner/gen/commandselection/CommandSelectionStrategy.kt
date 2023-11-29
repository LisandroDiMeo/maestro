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

    val onPreviousCommandUpdated: (CommandInformation) -> Unit

    object UnableToPickCommand : Exception()

    companion object {
        fun strategyFor(
            strategy: String,
            onPreviousCommandUpdated: (CommandInformation) -> Unit
        ): CommandSelectionStrategy {
            return when (strategy.lowercase()) {
                "random" -> RandomCommandSelection(onPreviousCommandUpdated)
                "viewranking" -> ViewRanking(onPreviousCommandUpdated)
                else -> RandomCommandSelection(onPreviousCommandUpdated)
            }
        }
    }
}

data class CommandInformation(
    val commandExecuted: MaestroCommand,
    val hash: String,
    val destinations: List<Pair<String, MaestroCommand>>
)
