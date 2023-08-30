package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.BackPressCommand
import maestro.orchestra.Command
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand

class AndroidHierarchyAnalyzer(
    private val selectionStrategy: CommandSelectionStrategy,
    private val viewDisambiguator: ViewDisambiguator,
) : HierarchyAnalyzer(viewDisambiguator) {
    override fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand {
        val flattenNodes = hierarchy.aggregate()
        val availableWidgets = extractWidgets(hierarchy)

        // Generate indirect commands (BackPress, InputText, Go to recent tasks)

        val commands = mutableListOf<Command>()
        val isKeyboardOpen = flattenNodes.any {
            val resourceId = it.attributes["resource-id"] ?: ""
            resourceId.contains("com.google.android.inputmethod.latin")
        }
        if (isKeyboardOpen) {
            commands.add(InputRandomCommand())
            commands.add(HideKeyboardCommand())
            commands.add(EraseTextCommand(null)) // Maybe replace with rand?
        }
        commands.add(BackPressCommand())
        flattenNodes.any {
            it.attributes["className"]
                ?.lowercase()
                ?.contains("scroll") == true
        }.let {
            if (it) commands.add(ScrollCommand())
        }

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

    override fun isOutsideApp(hierarchy: ViewHierarchy, packageName: String): Boolean {
        return hierarchy
            .aggregate()
            .any { it.attributes["packageName"] == packageName }
    }

    companion object {
        val ignoredResources = listOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin"
        )
    }
}
