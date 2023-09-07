package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand

class AndroidHierarchyAnalyzer(
    private val selectionStrategy: CommandSelectionStrategy,
    private val viewDisambiguator: ViewDisambiguator,
) : HierarchyAnalyzer(viewDisambiguator) {
    override fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand {
        val flattenNodes = hierarchy.aggregate()
        val availableWidgets = extractWidgets(hierarchy, flattenNodes)
        val commands = mutableListOf<Command>()
        commands.addAll(keyboardOpenCommandsIfOpen(flattenNodes))
        commands.add(BackPressCommand())
        scrollCommandIfScrollable(flattenNodes)?.let { commands.add(it) }

        // Generate Tap commands
        availableWidgets.forEach { (node, selector) ->
            node.clickable?.let { isClickable ->
                if (isClickable) {
                    val resourceAndPackage =
                        node.attributes["resource-id"] + "-" + node.attributes["packageName"]
                    if (ignoredResources.all { res -> !resourceAndPackage.contains(res) })
                        commands.add(TapOnElementCommand(selector))
                }
            }
        }
        return selectionStrategy.pickFrom(commands.map { MaestroCommand(it) })
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

    override fun isOutsideApp(hierarchy: ViewHierarchy, packageName: String): Boolean {
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
