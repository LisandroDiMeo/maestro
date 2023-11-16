package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand

class AndroidHierarchyAnalyzer(
    private val selectionStrategy: CommandSelectionStrategy,
    override val viewDisambiguator: DisambiguationRule,
) : HierarchyAnalyzer(viewDisambiguator) {
    override fun fetchCommandFrom(
        hierarchy: ViewHierarchy,
        newTest: Boolean
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
            node.clickable?.let { isClickable ->
                if (isClickable) {
                    val resourceAndPackage =
                        node.attributes["resource-id"] + "-" + node.attributes["packageName"]
                    if (ignoredResources.all { res -> !resourceAndPackage.contains(res) })
                        commands.add(TapOnElementCommand(selector) to node)
                }
            }
        }
        val commandToExecute = selectionStrategy.pickFrom(
            commands.map { (command, node) -> MaestroCommand(command) to node },
            hierarchy.root,
            newTest
        )
        val nodeForCommand =
            availableWidgets.firstOrNull { (it.second == commandToExecute.tapOnElement?.selector) }?.first
                ?: TreeNode()
        if((previousAction != null && previousAction!!.first.inputRandomTextCommand == null) || previousAction == null) {
            previousAction = commandToExecute to nodeForCommand
        }
        return commandToExecute
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
