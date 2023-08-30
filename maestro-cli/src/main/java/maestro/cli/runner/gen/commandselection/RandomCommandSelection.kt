package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class RandomCommandSelection : CommandSelectionStrategy {

    private val random = Random(1234)

    override fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand {
        if (availableCommands.isEmpty()) throw CommandSelectionStrategy.UnableToPickCommand
        return availableCommands.random(random)
    }
}

class PriorityCommandSelection : CommandSelectionStrategy {
    override fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand {
        val inputCommand = availableCommands.firstOrNull {
            it.inputRandomTextCommand != null || it.inputTextCommand != null }
        return if (inputCommand != null) {
            if (Random.nextBoolean() && Random.nextBoolean()) {
                availableCommands.random()
            } else {
                inputCommand
            }
        } else {
            availableCommands.random()
        }
    }
}
