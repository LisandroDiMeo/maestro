package maestro.cli.runner.gen.commandselection.strategies

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.cli.runner.gen.commandselection.strategies.random.RandomCommandSelection
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

/**
 * This abstraction is used to define strategies of how to pick
 * commands fetched by (commonly) [maestro.cli.runner.gen.hierarchyanalyzer.HierarchyAnalyzer].
 * This allows a pluggable way to add more strategies to the generation of test suites.
 */
abstract class CommandSelectionStrategy(
    open val actionHasher: ActionHasher,
    open val executedCommandsObservable: ExecutedCommandsObservable
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

    abstract fun usagesForAction(actionHash: String): Int

    object UnableToPickCommand : Exception()

    protected fun hashActions(
        root: TreeNode,
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>
    ): List<Pair<String, MaestroCommand>> {
        return availableCommands.map { (command, node) ->
            actionHasher.hashAction(
                root,
                command,
                node
            ) to command
        }
    }

    fun hashForPreviousAction() = previousAction

    abstract fun updateUsagesForActionToExecute(actionToPerform: String)

    protected fun performUpdateForObservable(
        newTest: Boolean,
        hashedActions: List<Pair<String, MaestroCommand>>
    ) {
        when {
            newTest && previousAction != launchAppCommandHash -> {
                executedCommandsObservable.performUpdate(
                    CommandInformation(
                        previousActionCommand,
                        previousAction,
                        emptyList(),
                        usages = usagesForAction(previousAction)
                    )
                )
                previousAction = launchAppCommandHash
                previousActionCommand = launchAppCommand
            }

            newTest && previousAction == launchAppCommandHash -> {
                previousAction = launchAppCommandHash
                previousActionCommand = launchAppCommand
                executedCommandsObservable.performUpdate(
                    CommandInformation(
                        previousActionCommand,
                        previousAction,
                        hashedActions,
                        usages = usagesForAction(previousAction)
                    )
                )
            }

            else -> {
                executedCommandsObservable.performUpdate(
                    CommandInformation(
                        previousActionCommand,
                        previousAction,
                        hashedActions,
                        usages = usagesForAction(previousAction)
                    )
                )
            }
        }
    }

    companion object {
        fun strategyFor(
            strategy: String,
            executedCommandsObservable: ExecutedCommandsObservable,
            seed: Long,
        ): CommandSelectionStrategy {
            val randomness = Random(seed)
            return when (strategy.lowercase()) {
                "random" -> RandomCommandSelection(
                    executedCommandsObservable = executedCommandsObservable,
                    random = randomness
                )

                "viewranking" -> ViewRanking(
                    executedCommandsObservable = executedCommandsObservable,
                    random = randomness
                )

                else -> RandomCommandSelection(
                    executedCommandsObservable = executedCommandsObservable,
                    random = randomness
                )
            }
        }
    }
}

