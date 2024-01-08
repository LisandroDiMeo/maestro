package maestro.cli.runner.gen.commandselection

import maestro.orchestra.MaestroCommand

/**
 * Executed command information used to build
 * the graphical model.
 * It resembles the representation of a vertex ([hash])
 * and its neighbors ([destinations]).
 */
data class CommandInformation(
    val commandExecuted: MaestroCommand,
    val hash: String,
    val destinations: List<Pair<String, MaestroCommand>>,
    val usages: Int = 0
)
