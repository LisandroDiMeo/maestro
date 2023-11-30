package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand

abstract class HierarchyAnalyzer(
    open val viewDisambiguator: DisambiguationRule,
    open val selectionStrategy: CommandSelectionStrategy,
) {

    protected var previousAction: Pair<MaestroCommand,TreeNode>? = null
    abstract fun fetchCommandFrom(
        hierarchy: ViewHierarchy,
        newTest: Boolean,
        wasLastActionForTest: Boolean
    ): MaestroCommand

    open fun extractWidgets(
        hierarchy: ViewHierarchy,
        flattenNodes: List<TreeNode>
    ): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root

        return flattenNodes
            .map {
                it to viewDisambiguator.disambiguate(
                    root,
                    it,
                    flattenNodes
                )
            }.filter { it.second != ElementSelector() }
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
