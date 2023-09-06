package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class RandomCommandSelection(private val random: Random = Random(1234)) : CommandSelectionStrategy {

    override fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand {
        if (availableCommands.isEmpty()) throw CommandSelectionStrategy.UnableToPickCommand
        return availableCommands.random(random)
    }
}
