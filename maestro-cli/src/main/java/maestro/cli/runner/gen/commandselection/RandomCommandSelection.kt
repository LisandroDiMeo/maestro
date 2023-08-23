package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand

class RandomCommandSelection : CommandSelectionStrategy {
    override fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand {
        if (availableCommands.isEmpty()) throw CommandSelectionStrategy.UnableToPickCommand
        return availableCommands.random()
    }
}
