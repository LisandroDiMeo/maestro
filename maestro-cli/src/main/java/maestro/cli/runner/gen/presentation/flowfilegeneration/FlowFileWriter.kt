package maestro.cli.runner.gen.presentation.flowfilegeneration

import maestro.orchestra.MaestroCommand

interface FlowFileWriter {
    fun writeFlowFileFrom(
        commands: List<MaestroCommand>,
        id: Int
    )
}
