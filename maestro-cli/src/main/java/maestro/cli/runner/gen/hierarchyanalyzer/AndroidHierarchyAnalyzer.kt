package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.DeviceInfo
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.SimpleAndroidViewDisambiguator
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.filterOutOfBounds
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
    val device: DeviceInfo
) : HierarchyAnalyzer(viewDisambiguator, device) {
    override fun fetchCommandFrom(hierarchy: ViewHierarchy): MaestroCommand {
        val root = hierarchy.root
        val allNodes = hierarchy.aggregate()
        val filtered = root.filterOutOfBounds(
            width = device.widthGrid,
            height = device.heightGrid
        )
        val flattenNodes = filtered?.aggregate() ?: emptyList()
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
        if (allNodes.size != flattenNodes.size) commands.add(ScrollCommand())

        // Generate Tap commands
        availableWidgets.forEach { (node, selector) ->
            node.clickable?.let {
                node.attributes["resource-id"]?.let {
                    if(ignoredResources.all { res -> !it.contains(res) })
                        commands.add(TapOnElementCommand(selector))
                } ?: commands.add(TapOnElementCommand(selector))
            }
        }

        return selectionStrategy.pickFrom(commands.map { MaestroCommand(it) })

    }

    companion object {
        val ignoredResources = listOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin"
        )
    }
}
