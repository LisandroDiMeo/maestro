package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand

abstract class HierarchyAnalyzer(
    open val disambiguationRule: DisambiguationRule,
    open val selectionStrategy: CommandSelectionStrategy,
) {

    private var previousAction: Pair<MaestroCommand,TreeNode>? = null

    fun fetchCommandFrom(hierarchy: ViewHierarchy,
          newTest: Boolean,
          wasLastActionForTest: Boolean): MaestroCommand {
        val flattenNodes = removeIgnoredNodes(hierarchy.aggregate())
        val availableWidgets = extractWidgets(
            hierarchy,
            flattenNodes
        )
        val commands = mutableListOf<Pair<Command, TreeNode?>>()
        commands.addAll(keyboardOpenCommandsIfOpen(flattenNodes).map { it to hierarchy.root })
        commands.add(BackPressCommand() to hierarchy.root)
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
        if((previousAction != null && previousAction!!.first.inputRandomTextCommand == null) || previousAction == null) {
            previousAction = commandToExecute to nodeForCommand
        }
        return commandToExecute
    }

    open fun removeIgnoredNodes(flattenNodes: List<TreeNode>): List<TreeNode> = flattenNodes

    abstract fun extractClickableActions(selectors: List<Pair<TreeNode, ElementSelector>>): List<Pair<Command, TreeNode?>>

    open fun extractWidgets(
        hierarchy: ViewHierarchy,
        flattenNodes: List<TreeNode>
    ): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root
        val invalidSelectors = listOf(
            ElementSelector(),
            ElementSelector(textRegex = ""),
            ElementSelector(idRegex = ""),
            ElementSelector(idRegex = "", textRegex = "")
        )
        val preSelectionOfWidgets = flattenNodes
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
//        return flattenNodes
//            .map {
//                it to disambiguationRule.disambiguate(
//                    root,
//                    it,
//                    flattenNodes
//                )
//            }.filter { it.second !in invalidSelectors }
    }

    open fun keyboardOpenCommands(): List<Command> {
        return listOf(
            InputRandomCommand(origin = previousAction),
            HideKeyboardCommand(origin = previousAction),
            EraseTextCommand(null, origin = previousAction)
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

    abstract fun isOutsideApp(hierarchy: ViewHierarchy, packageName: String): Boolean

}
