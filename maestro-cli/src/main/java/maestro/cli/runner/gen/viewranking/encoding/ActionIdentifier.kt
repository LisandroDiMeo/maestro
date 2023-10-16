package maestro.cli.runner.gen.viewranking.encoding

import maestro.orchestra.MaestroCommand

data class ActionIdentifier(
    val identifier: String,
    val action: MaestroCommand
)
