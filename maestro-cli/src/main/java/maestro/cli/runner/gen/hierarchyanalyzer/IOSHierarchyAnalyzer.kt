package maestro.cli.runner.gen.hierarchyanalyzer

import hierarchy.AXElement.Companion.ELEMENT_TYPES
import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.TapOnElementCommand

class IOSHierarchyAnalyzer(
    override val selectionStrategy: CommandSelectionStrategy,
    override val disambiguationRule: DisambiguationRule,
) : HierarchyAnalyzer(
    disambiguationRule,
    selectionStrategy
) {
    override fun extractClickableActions(selectors: List<Pair<TreeNode, ElementSelector>>): List<Pair<Command, TreeNode?>> {
        val resultingCommands = mutableListOf<Pair<Command, TreeNode?>>()
        selectors.forEach { (node, selector) ->
            node.clickable?.let {
                resultingCommands.add(TapOnElementCommand(selector) to node)
            }
        }
        return resultingCommands.toList()
    }

    override fun isScrollable(nodes: List<TreeNode>): Boolean = true

    override fun isKeyboardOpen(nodes: List<TreeNode>): Boolean =
        nodes.any { ELEMENT_TYPES[it.attributes["elementType"]] == "key" }

    override fun isOutsideApp(
        hierarchy: ViewHierarchy,
        packageName: String
    ): Boolean {
        return hierarchy.aggregate().none { it.attributes.values.toString().contains(packageName) }
    }

    override fun removeIgnoredNodes(flattenNodes: List<TreeNode>): List<TreeNode> {
        val topContainerNodes = flattenNodes
            .first { it.attributes["resource-id"] == TOP_CONTAINER_RES_ID }
            .aggregate()
        val keyElementCode = ELEMENT_TYPES.keys.first { ELEMENT_TYPES[it] == "key" }
        val keyboardNodes = flattenNodes.firstOrNull { node ->
            node.children.any { childNode ->
                childNode.attributes["elementType"] == keyElementCode
            }
        }?.aggregate() ?: emptyList()
        val predictiveInput = flattenNodes.firstOrNull { node ->
            node.attributes["resource-id"] == "SystemInputAssistantView"
        }?.aggregate() ?: emptyList()
        return flattenNodes.filter {
                it !in (topContainerNodes + predictiveInput + keyboardNodes)
            }
    }

    companion object {
        private const val TOP_CONTAINER_RES_ID = "SBSwitcherWindow:Main"
    }

}
