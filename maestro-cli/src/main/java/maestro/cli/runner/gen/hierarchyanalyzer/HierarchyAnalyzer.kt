package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand

abstract class HierarchyAnalyzer(
    private val viewDisambiguator: ViewDisambiguator,
) {

    protected var previousAction: Pair<MaestroCommand,TreeNode>? = null
    abstract fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand

    open fun extractWidgets(hierarchy: ViewHierarchy, flattenNodes: List<TreeNode>): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root
        val visibleNodes = flattenNodes.filter { hierarchy.isVisible(it) }

        val availableWidgets = visibleNodes
            .map { it to viewDisambiguator.disambiguate(root, it, flattenNodes) }
            .filter {
                viewDisambiguator.properlyDisambiguated(it.second)
            }
        return availableWidgets
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
