package maestro.cli.runner.gen.commandselection.strategies.viewranking

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.orchestra.MaestroCommand
import kotlin.math.min
import kotlin.random.Random

/**
 * Strategy that attempts to pick unused actions or actions that can
 * lead to screens with unused actions. It's strongly inspired by
 * ISSTA 2020 Talk by Nadia Alshahwan.
 * @see <a href="https://www.youtube.com/watch?v=BM89PFDwZuU">ISSTA 2020 Talk</a>
 */
class ViewRanking(
    private val random: Random = Random(4242),
    override val actionHasher: ActionHasher = TreeDirectionHasher(),
    override val executedCommandsObservable: ExecutedCommandsObservable = ExecutedCommandsObservable(),
) : CommandSelectionStrategy(
    actionHasher,
    executedCommandsObservable
) {

    private val model: MutableMap<String, ActionInformation> = mutableMapOf()
    private val actionComparator = compareBy<Pair<String, Pair<MaestroCommand, ActionRank>>>(
        { it.second.second.second },
        { it.second.second.third },
        { it.second.second.first }
    )

    // This map is for debug purposes
    private val actionHashToCommand = mutableListOf<Pair<String, MaestroCommand>>()

    init {
        previousAction = launchAppCommandHash
        model[launchAppCommandHash] = ActionInformation(
            emptyList(),
            1
        )
    }

    override fun pickFrom(
        availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
        root: TreeNode,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand {
        val hashedActions = hashActions(root, availableCommands)
        actionHashToCommand.addAll(hashedActions)
        performUpdateForObservable(newTest, hashedActions)
        addEdgesToPreviousAction(hashedActions)
        updateModelWithIncomingActions(hashedActions)

        if (wasLastActionForTest) return MaestroCommand()

        val shortestPathMap = minimumHopsToUnusedActions(hashedActions.map { it.first })
        val (bestRankedActionHash, bestRankedAction) = pairBestRankedHashAction(
            hashedActions,
            shortestPathMap,
            actionComparator
        )

        previousAction = bestRankedActionHash
        updateUsagesForActionToExecute(previousAction)
        previousActionCommand = bestRankedAction
        return bestRankedAction
    }

    override fun usagesForAction(actionHash: String): Int {
        return model[actionHash]?.second ?: 1
    }

    private fun pairBestRankedHashAction(
        hashedActions: List<Pair<String, MaestroCommand>>,
        shortestPathMap: Map<String, List<String>>,
        comparator: Comparator<Pair<String, Pair<MaestroCommand, ActionRank>>>
    ) = hashedActions.map { (hash, command) ->
        val rank = rank(
            hash,
            command,
            shortestPathMap
        )
        hash to (command to rank)
    }.run {
        minimumValuesBy(
            comparator,
            this
        )
    }
        .map { it.first to it.second.first }
        .random(random)

    override fun updateUsagesForActionToExecute(actionToPerform: String) {
        // We override previous action destinations
        // since it can change over time
        if (actionToPerform in model.keys) {
            model[actionToPerform] = ActionInformation(
                model[actionToPerform]!!.first,
                model[actionToPerform]!!.second + 1
            )
        } else {
            model[actionToPerform] = ActionInformation(
                emptyList(),
                1
            )
        }
    }

    private fun rank(
        actionHashed: String,
        command: MaestroCommand,
        minimumHopsMap: Map<String, List<String>>
    ): ActionRank {
        val priority = priority(command)
        val usages = min(model[actionHashed]!!.second, 1)
        val hopsToUnusedActions = minimumHopsMap[actionHashed]?.size ?: Int.MAX_VALUE
        return Triple(
            priority,
            usages,
            hopsToUnusedActions
        )
    }

    private fun minimumHopsToUnusedActions(from: List<String>): Map<String, List<String>> {
        val to = model.filter { (_, value) ->
            value.second == 0
        }.keys.toList()
        return MinimumHopsFinder.minimumHopsForSources(
            model,
            from,
            to
        )
    }

    private fun addEdgesToPreviousAction(hashedActions: List<Pair<String, MaestroCommand>>) {
        if (!isEmpty()) {
            val usages = model[previousAction]?.second ?: 1
            val previousDestinations = model[previousAction]?.first ?: emptyList()
            model[previousAction] =
                (hashedActions.map { (hash, _) -> hash } + previousDestinations).toSet().toList() to usages
        }
    }

    private fun updateModelWithIncomingActions(hashedActions: List<Pair<String, MaestroCommand>>) {
        hashedActions.forEach { (hash, _) ->
            if (hash !in model.keys) model[hash] = emptyList<String>() to 0
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

    fun edgesFor(
        command: MaestroCommand,
        node: TreeNode?,
        root: TreeNode
    ): ActionInformation? {
        val hash = actionHasher.hashAction(
            root,
            command,
            node
        )
        return model[hash]
    }

}
