package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command

class AndroidHierarchyAnalyzer(
    override val selectionStrategy: CommandSelectionStrategy,
    override val disambiguationRule: DisambiguationRule,
) : HierarchyAnalyzer(disambiguationRule, selectionStrategy) {

    override fun removeIgnoredNodes(flattenNodes: List<TreeNode>): List<TreeNode> {
        return flattenNodes.filter {
            val resourceAndPackage = "${it.attributes["resource-id"]}-${it.attributes["packageName"]}"
            ANDROID_IGNORED_RESOURCES.all { res -> !resourceAndPackage.contains(res) }
        }
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

    override fun keyboardOpenCommands(): List<Command> {
        return emptyList()
    }

    override fun backPressCommand(): Command? {
        return BackPressCommand()
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
        val ANDROID_IGNORED_RESOURCES = listOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin"
        )
    }
}
