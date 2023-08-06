package maestro.cli.runner

import maestro.MaestroException
import maestro.cli.report.CommandDebugMetadata
import maestro.cli.report.FlowDebugMetadata
import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import maestro.orchestra.runcycle.FlowFileRunCycle
import org.slf4j.Logger

class TestSuiteInteractorCycle(
    private val logger: Logger,
    private val debug: FlowDebugMetadata,
    private val onScreenshot: (CommandStatus) -> Unit
) : FlowFileRunCycle() {
    override fun onCommandStart(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} RUNNING ⏳")
        debug.commands[command] = CommandDebugMetadata(
            timestamp = System.currentTimeMillis(),
            status = CommandStatus.RUNNING
        )
    }

    override fun onCommandComplete(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} COMPLETED ✅")
        debug.commands[command]?.let {
            it.status = CommandStatus.COMPLETED
            it.calculateDuration()
        }
    }

    override fun onCommandFailed(commandId: Int, command: MaestroCommand, error: Throwable): Orchestra.ErrorResolution {
        logger.info("${command.description()} FAILED ❌")
        if (error is MaestroException) debug.exception = error
        debug.commands[command]?.let {
            it.status = CommandStatus.FAILED
            it.calculateDuration()
            it.error = error
        }

        onScreenshot(CommandStatus.FAILED)
        return Orchestra.ErrorResolution.FAIL
    }

    override fun onCommandSkipped(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} SKIPPED ⏭️")
        debug.commands[command]?.let {
            it.status = CommandStatus.SKIPPED
        }
    }

    override fun onCommandReset(command: MaestroCommand) {
        logger.info("${command.description()} PENDING 🖐️")
        debug.commands[command]?.let {
            it.status = CommandStatus.PENDING
        }
    }
}
