package maestro.cli.runner.gen.hierarchyanalyzer

import hierarchy.AXElement.Companion.ELEMENT_TYPES
import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand

class IOSHierarchyAnalyzer(
    override val selectionStrategy: CommandSelectionStrategy,
    override val viewDisambiguator: DisambiguationRule,
) : HierarchyAnalyzer(viewDisambiguator, selectionStrategy) {
    override fun fetchCommandFrom(
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
        commands.add(BackPressCommand() to hierarchy.root)
        scrollCommandIfScrollable(flattenNodes)?.let { commands.add(it to hierarchy.root) }

        // Generate Tap commands
        availableWidgets.forEach { (node, selector) ->
            node.clickable?.let {
                commands.add(TapOnElementCommand(selector) to node)
            }
        }
        val commandToExecute = selectionStrategy.pickFrom(
            commands.map { (command, node) -> MaestroCommand(command) to node },
            hierarchy.root,
            newTest,
            wasLastActionForTest
        )
        val u =
            availableWidgets.firstOrNull { (it.second == commandToExecute.tapOnElement!!.selector) }?.first
                ?: TreeNode()
        previousAction = commandToExecute to u
        return commandToExecute
    }

    override fun isScrollable(nodes: List<TreeNode>): Boolean = false

    override fun isKeyboardOpen(nodes: List<TreeNode>): Boolean =
        nodes.any { ELEMENT_TYPES[it.attributes["elementType"]] == "key" }

    override fun extractWidgets(
        hierarchy: ViewHierarchy,
        flattenNodes: List<TreeNode>
    ): List<Pair<TreeNode, ElementSelector>> {

        val bannedChildren = flattenNodes.first { treeNode ->
            val childrenAttributes =
                treeNode.children.map { attrs -> attrs.attributes.values.toString() }.toString()
            val isStatusBar =
                childrenAttributes.contains(batteryIndicatorRegex) && (childrenAttributes.contains(
                    absoluteHourIndicatorRegex
                ) || childrenAttributes.contains(relativeHourIndicatorRegex))
            isStatusBar
        }.children

        val availableWidgets = flattenNodes.filter { treeNode ->
            !shouldBeIgnored(
                ELEMENT_TYPES[treeNode.attributes["elementType"]] ?: "any"
            ) || treeNode in bannedChildren
        }.map {
            it to viewDisambiguator.disambiguate(
                hierarchy.root,
                it,
                flattenNodes
            )
        }
        return availableWidgets
    }

    override fun isOutsideApp(
        hierarchy: ViewHierarchy,
        packageName: String
    ): Boolean {
        return hierarchy.aggregate().none { it.attributes.values.toString().contains(packageName) }
    }

    private fun shouldBeIgnored(elementType: String): Boolean = when (elementType) {
        "application", "activityIndicator", "window", "ruler", "rulerMarker", "progressIndicator", "outline", "outlineRow", "layoutArea", "any", "other" -> true

        else -> false
    }

    companion object {
        private val batteryIndicatorRegex = Regex("(100|[0-9]{1,2})% battery power")
        private val relativeHourIndicatorRegex = Regex("((1[0-2]|[1-9]):([0-5][0-9]) ([AaPp][Mm]))")
        private val absoluteHourIndicatorRegex = Regex("([0-1][0-9]|2[0-3]):([0-5][0-9])")
    }

}
