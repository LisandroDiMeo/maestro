package maestro.cli.runner.gen.hierarchyanalyzer

import hierarchy.XCUIElementDeserializer.Companion.ELEMENT_TYPES
import maestro.DeviceInfo
import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand

class IOSHierarchyAnalyzer(
    private val selectionStrategy: CommandSelectionStrategy,
    private val viewDisambiguator: ViewDisambiguator,
    private val deviceInfo: DeviceInfo
) : HierarchyAnalyzer(viewDisambiguator, deviceInfo) {
    override fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand {
        val availableWidgets = extractWidgets(hierarchy)
        val commands = mutableListOf<Command>()
        availableWidgets.forEach { (node, selector) ->
            node.clickable?.let {
                commands.add(TapOnElementCommand(selector))
            }
        }
        return selectionStrategy.pickFrom(commands.map { MaestroCommand(it) })
    }

    override fun extractWidgets(hierarchy: ViewHierarchy): List<Pair<TreeNode, ElementSelector>> {
        val flattenNodes = hierarchy.aggregate()
        val availableWidgets = flattenNodes
            .filter { !shouldBeIgnored(ELEMENT_TYPES[it.attributes["elementType"]] ?: "any") }
            .map { it to viewDisambiguator.disambiguate(hierarchy.root, it, flattenNodes) }
            .filter {
                viewDisambiguator.properlyDisambiguated(it.second)
            }
            return availableWidgets
    }

    private fun shouldBeIgnored(elementType: String): Boolean = when (elementType) {
        "application", "activityIndicator", "window", "ruler", "rulerMarker",
        "progressIndicator", "outline", "outlineRow", "layoutArea", "any", "other"
        -> true
        else -> false
    }
}
