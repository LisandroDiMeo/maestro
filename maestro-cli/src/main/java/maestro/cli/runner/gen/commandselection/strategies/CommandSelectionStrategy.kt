package maestro.cli.runner.gen.commandselection.strategies

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.commandselection.strategies.random.RandomCommandSelection
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand

/**
 * This abstraction is used to define strategies of how to pick
 * commands fetched by (commonly) [maestro.cli.runner.gen.hierarchyanalyzer.HierarchyAnalyzer].
 * This allows a pluggable way to add more strategies to the generation of test suites.
 */
abstract class CommandSelectionStrategy(
    open val actionHasher: ActionHasher,
    executedCommandsObservable: ExecutedCommandsObservable
) {

    protected val launchAppCommand = MaestroCommand(launchAppCommand = LaunchAppCommand(""))
    protected val launchAppCommandHash by lazy {
        actionHasher.hashAction(
            TreeNode(),
            launchAppCommand,
            null
        )
    }
    protected var previousAction: String = ""
    protected var previousActionCommand: MaestroCommand = launchAppCommand

    /**
     * @param availableCommands pool of pair <commands, node?> from which to choose
     * @param root usually is needed to know the root of the view tree from which the
     * commands where fetched to determine which command choose
     * @param newTest boolean to know if we are about to choose the first command for a test case
     * @param wasLastActionForTest boolean to know if we are done for current test case
     * @return [MaestroCommand] to execute.
     */
    abstract fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand

    object UnableToPickCommand : Exception()

    companion object {
        fun strategyFor(
            strategy: String,
            executedCommandsObservable: ExecutedCommandsObservable,
        ): CommandSelectionStrategy {
            return when (strategy.lowercase()) {
                "random" -> RandomCommandSelection(
                    executedCommandsObservable = executedCommandsObservable,
                )

                "viewranking" -> ViewRanking(
                    executedCommandsObservable = executedCommandsObservable,
                )

                else -> RandomCommandSelection(
                    executedCommandsObservable = executedCommandsObservable,
                )
            }
        }
    }
}

