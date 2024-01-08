package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand

data class CommandInformation(
    val commandExecuted: MaestroCommand,
    val hash: String,
    val destinations: List<Pair<String, MaestroCommand>>,
    val usages: Int = 0
)
