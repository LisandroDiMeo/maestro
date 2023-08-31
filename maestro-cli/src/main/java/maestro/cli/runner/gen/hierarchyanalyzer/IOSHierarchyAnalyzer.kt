package maestro.cli.runner.gen.hierarchyanalyzer

import hierarchy.XCUIElementDeserializer.Companion.ELEMENT_TYPES
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
    private val viewDisambiguator: ViewDisambiguator
) : HierarchyAnalyzer(viewDisambiguator) {
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

        val bannedChildren = flattenNodes.first {
            val childrenAttributes =
                it.children
                    .map { attrs -> attrs.attributes.values.toString() }
                    .toString()
            val isStatusBar =
                childrenAttributes.contains(batteryIndicatorRegex) && (childrenAttributes.contains(
                    absoluteHourIndicatorRegex
                ) || childrenAttributes.contains(relativeHourIndicatorRegex))
            isStatusBar
        }.children

        val availableWidgets = flattenNodes.filter {
                !shouldBeIgnored(
                    ELEMENT_TYPES[it.attributes["elementType"]] ?: "any"
                ) || it in bannedChildren
            }
            .map { it to viewDisambiguator.disambiguate(hierarchy.root, it, flattenNodes) }
            .filter {
                viewDisambiguator.properlyDisambiguated(it.second)
            }
        return availableWidgets
    }

    override fun isOutsideApp(hierarchy: ViewHierarchy, packageName: String): Boolean {
        return !hierarchy.aggregate().any { it.attributes.values.toString().contains(packageName) }
    }

    private fun shouldBeIgnored(elementType: String): Boolean = when (elementType) {
        "application", "activityIndicator", "window", "ruler", "rulerMarker", "progressIndicator", "outline", "outlineRow", "layoutArea", "any", "other" -> true

        else -> false
    }

    companion object {
        private val batteryIndicatorRegex = Regex("(100|[0-9]{1,2})% battery power")
        private val relativeHourIndicatorRegex =
            Regex("((1[0-2]|[1-9]):([0-5][0-9]) ([AaPp][Mm]))")
        private val absoluteHourIndicatorRegex = Regex("([0-1][0-9]|2[0-3]):([0-5][0-9])")
    }

}
