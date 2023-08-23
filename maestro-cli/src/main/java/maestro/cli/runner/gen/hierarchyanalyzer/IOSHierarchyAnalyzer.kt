package maestro.cli.runner.gen.hierarchyanalyzer

import maestro.DeviceInfo
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.viewdisambiguator.ViewDisambiguator
import maestro.orchestra.Command
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
}
