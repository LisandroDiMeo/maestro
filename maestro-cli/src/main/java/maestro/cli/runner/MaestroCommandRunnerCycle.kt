package maestro.cli.runner

import maestro.MaestroException
import maestro.cli.report.CommandDebugMetadata
import maestro.cli.report.FlowDebugMetadata
import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import maestro.orchestra.runcycle.FlowFileRunCycle
import org.slf4j.Logger
import java.util.IdentityHashMap

class MaestroCommandRunnerCycle(
    private val logger: Logger,
    private val refreshUi: () -> Unit,
    private val onScreenshot: (CommandStatus) -> Unit,
    private val flowDebugMetadata: FlowDebugMetadata,
    private val commandStatuses: IdentityHashMap<MaestroCommand, CommandStatus>,
    private val commandMetadata: IdentityHashMap<MaestroCommand, Orchestra.CommandMetadata>
) : FlowFileRunCycle() {
    override fun onCommandStart(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} RUNNING")
        commandStatuses[command] = CommandStatus.RUNNING
        flowDebugMetadata.commands[command] = CommandDebugMetadata(
            timestamp = System.currentTimeMillis(), status = CommandStatus.RUNNING
        )

        refreshUi()
    }

    override fun onCommandComplete(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} COMPLETED")
        commandStatuses[command] = CommandStatus.COMPLETED
        flowDebugMetadata.commands[command]?.let {
            it.status = CommandStatus.COMPLETED
            it.calculateDuration()
        }
        refreshUi()
    }

    override fun onCommandFailed(commandId: Int, command: MaestroCommand, error: Throwable): Orchestra.ErrorResolution {
        flowDebugMetadata.commands[command]?.let {
            it.status = CommandStatus.FAILED
            it.calculateDuration()
            it.error = error
        }

        onScreenshot(CommandStatus.FAILED)

        if (error !is MaestroException) {
            throw error
        } else {
            flowDebugMetadata.exception = error
        }

        logger.info("${command.description()} FAILED")
        commandStatuses[command] = CommandStatus.FAILED
        refreshUi()

        return Orchestra.ErrorResolution.FAIL
    }

    override fun onCommandSkipped(commandId: Int, command: MaestroCommand) {
        logger.info("${command.description()} SKIPPED")
        commandStatuses[command] = CommandStatus.SKIPPED
        flowDebugMetadata.commands[command]?.let {
            it.status = CommandStatus.SKIPPED
        }
        refreshUi()
    }

    override fun onCommandReset(command: MaestroCommand) {
        logger.info("${command.description()} PENDING")
        commandStatuses[command] = CommandStatus.PENDING
        flowDebugMetadata.commands[command]?.let {
            it.status = CommandStatus.PENDING
        }
        refreshUi()
    }

    override fun onCommandMetadataUpdate(command: MaestroCommand, metadata: Orchestra.CommandMetadata) {
        logger.info("${command.description()} metadata $metadata")
        commandMetadata[command] = metadata
        refreshUi()
    }
}
