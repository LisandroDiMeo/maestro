package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand

abstract class HierarchyAnalyzer(
    private val viewDisambiguator: ViewDisambiguator,
) {
    abstract fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand

    open fun extractWidgets(hierarchy: ViewHierarchy): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root
        val flattenNodes = root.aggregate()
        val visibleNodes = flattenNodes.filter { hierarchy.isVisible(it) }

        val availableWidgets = visibleNodes
            .map { it to viewDisambiguator.disambiguate(root, it, flattenNodes) }
            .filter {
                viewDisambiguator.properlyDisambiguated(it.second)
            }
        return availableWidgets
    }

    abstract fun isOutsideApp(hierarchy: ViewHierarchy, packageName: String): Boolean

}
