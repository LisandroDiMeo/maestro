package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand

/**
 * This class is in charge of the mechanism of fetching a command using certain [selectionStrategy]
 * and providing nodes/selectors that were disambiguated by a [disambiguationRule].
 * Also, here we introduce a layer of device-specific abstraction, as both Android and iOS
 * has specific nodes that we'd like to ignore, different criteria of when an AUT is not visible, and so on.
 * As for our model, its the main collaborator for [maestro.cli.runner.gen.TestGenerationOrchestra].
 */
abstract class HierarchyAnalyzer(
    open val disambiguationRule: DisambiguationRule,
    open val selectionStrategy: CommandSelectionStrategy,
) {

    private var previousAction: Pair<MaestroCommand, TreeNode>? = null

    /**
     * Fetch a command using provided strategy for given [hierarchy].
     * @param hierarchy to be analyzed and from where the command will be generated
     * @param newTest strategies needs to know if we are selecting a command for a new test or an ongoing one
     * @param wasLastActionForTest strategies needs to know if the previous fetched command was the last action for that test
     */
    fun fetchCommandFrom(
        hierarchy: ViewHierarchy,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand {
        val flattenNodes = hierarchy.aggregate()
        val availableWidgets = extractWidgets(
            hierarchy,
            flattenNodes
        )
        val commands = mutableListOf<Pair<Command, TreeNode?>>()
        commands.addAll(keyboardOpenCommandsIfOpen(flattenNodes).map { it to hierarchy.root })
        backPressCommand()?.let {
            commands.add(it to hierarchy.root)
        }
        scrollCommandIfScrollable(flattenNodes)?.let { commands.add(it to hierarchy.root) }
        commands.addAll(extractClickableActions(availableWidgets))

        val commandToExecute = selectionStrategy.pickFrom(
            commands.map { (command, node) -> MaestroCommand(command) to node },
            hierarchy.root,
            newTest,
            wasLastActionForTest
        )
        val nodeForCommand =
            availableWidgets.firstOrNull { (it.second == commandToExecute.tapOnElement?.selector) }?.first
                ?: TreeNode()
        if ((previousAction != null && previousAction!!.first.inputRandomTextCommand == null) || previousAction == null) {
            previousAction = commandToExecute to nodeForCommand
        }
        return commandToExecute
    }

    /**
     * Filter out from [flattenNodes] which nodes are not useful for command generation
     */
    open fun removeIgnoredNodes(flattenNodes: List<TreeNode>): List<TreeNode> = flattenNodes

    /**
     * Provides how clickable actions will be made based on [selectors]
     */
    open fun extractClickableActions(selectors: List<Pair<TreeNode, ElementSelector>>): List<Pair<Command, TreeNode?>> {
        val resultingCommands = mutableListOf<Pair<Command, TreeNode?>>()
        selectors.forEach { (node, selector) ->
            node.clickable?.let {
                if (it) {
                    resultingCommands.add(TapOnElementCommand(selector) to node)
                }
            }
        }
        return resultingCommands.toList()
    }

    /**
     * Provides how widget extraction will be made.
     * This method is quite crucial since [TapOnElementCommand]'s relies mostly on useful [ElementSelector]'s.
     */
    open fun extractWidgets(
        hierarchy: ViewHierarchy,
        flattenNodes: List<TreeNode>
    ): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root
        val invalidSelectors = listOf(
            ElementSelector(),
            ElementSelector(textRegex = ""),
            ElementSelector(idRegex = ""),
            ElementSelector(
                idRegex = "",
                textRegex = ""
            )
        )
        // TODO: I made an important change here, so we will see if everything is ok...
        val allowedNodes = removeIgnoredNodes(flattenNodes)
        val preSelectionOfWidgets = allowedNodes
            .map {
                it to disambiguationRule.disambiguate(
                    root,
                    it,
                    flattenNodes
                )
            }.filter { it.second !in invalidSelectors }
        return preSelectionOfWidgets.filter { (_, selector) ->
            preSelectionOfWidgets.count { (_, otherSelector) ->
                otherSelector == selector
            } == 1
        }
    }

    open fun backPressCommand(): Command? = null

    open fun keyboardOpenCommands(): List<Command> {
        return listOf(
            InputRandomCommand(origin = previousAction),
            HideKeyboardCommand(origin = previousAction),
            EraseTextCommand(
                null,
                origin = previousAction
            )
        )
    }

    open fun keyboardOpenCommandsIfOpen(nodes: List<TreeNode>): List<Command> {
        return if (isKeyboardOpen(nodes)) keyboardOpenCommands() else emptyList()
    }

    open fun scrollCommandIfScrollable(nodes: List<TreeNode>): Command? {
        return if (isScrollable(nodes)) ScrollCommand() else null
    }

    abstract fun isScrollable(nodes: List<TreeNode>): Boolean

    abstract fun isKeyboardOpen(nodes: List<TreeNode>): Boolean

    /**
     * Determines if the [hierarchy] is outside the app identified by [packageName]
     */
    abstract fun isOutsideApp(
        hierarchy: ViewHierarchy,
        packageName: String
    ): Boolean

}
