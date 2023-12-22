package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand

class AndroidHierarchyAnalyzer(
    override val selectionStrategy: CommandSelectionStrategy,
    override val disambiguationRule: DisambiguationRule,
) : HierarchyAnalyzer(disambiguationRule, selectionStrategy) {

    override fun extractClickableActions(selectors: List<Pair<TreeNode, ElementSelector>>): List<Pair<Command, TreeNode?>> {
        val resultingCommands = mutableListOf<Pair<Command, TreeNode>>()
        selectors.forEach { (node, selector) ->
            node.clickable?.let { isClickable ->
                if (isClickable) {
                    val resourceAndPackage =
                        node.attributes["resource-id"] + "-" + node.attributes["packageName"]
                    if (ignoredResources.all { res -> !resourceAndPackage.contains(res) })
                        resultingCommands.add(TapOnElementCommand(selector) to node)
                }
            }
        }
        return resultingCommands.toList()
    }

    override fun isScrollable(nodes: List<TreeNode>): Boolean {
        nodes.any {
            it.attributes["className"]
                ?.lowercase()
                ?.contains("scroll") == true
        }.let {
            if (it) return true
        }
        return false
    }

    override fun isKeyboardOpen(nodes: List<TreeNode>): Boolean {
        return nodes.any {
            val resourceId = it.attributes["resource-id"] ?: ""
            resourceId.contains("com.google.android.inputmethod.latin")
        }
    }

    override fun isOutsideApp(
        hierarchy: ViewHierarchy,
        packageName: String
    ): Boolean {
        return hierarchy
            .aggregate()
            .none { it.attributes["packageName"] == packageName }
    }

    companion object {
        val ignoredResources = listOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin"
        )
    }
}
