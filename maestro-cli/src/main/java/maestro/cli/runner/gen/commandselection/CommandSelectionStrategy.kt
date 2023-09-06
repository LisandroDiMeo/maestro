package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand

interface CommandSelectionStrategy {
    @kotlin.jvm.Throws(UnableToPickCommand::class)
    fun pickFrom(availableCommands: List<MaestroCommand>): MaestroCommand

    object UnableToPickCommand : Exception()

    companion object {
        fun strategyFor(strategy: String): CommandSelectionStrategy {
            return when(strategy.lowercase()) {
                "random" -> RandomCommandSelection()
                else -> RandomCommandSelection()
            }
        }
    }
}
