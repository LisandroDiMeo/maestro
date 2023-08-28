package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.DeviceInfo
import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.SimpleAndroidViewDisambiguator
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.filterOutOfBounds
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand

abstract class HierarchyAnalyzer(
    private val viewDisambiguator: ViewDisambiguator,
    private val deviceInfo: DeviceInfo,
) {
    abstract fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand

    open fun extractWidgets(hierarchy: ViewHierarchy): List<Pair<TreeNode, ElementSelector>> {
        val root = hierarchy.root
        val filtered = root.filterOutOfBounds(
            width = deviceInfo.widthGrid,
            height = deviceInfo.heightGrid
        )
        val flattenNodes = filtered?.aggregate() ?: emptyList()
        val visibleNodes = flattenNodes.filter { hierarchy.isVisible(it) }

        val availableWidgets = visibleNodes
            .map { it to viewDisambiguator.disambiguate(root, it, flattenNodes) }
            .filter {
                viewDisambiguator.properlyDisambiguated(it.second)
            }
        return availableWidgets
    }

}
