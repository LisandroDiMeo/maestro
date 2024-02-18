package maestro.cli.runner.gen.presentation.runcycle

import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import org.slf4j.Logger

class TestSuiteGeneratorCycle(private val logger: Logger) : RunCycle() {

    override fun onCommandStart(
        commandId: Int,
        command: MaestroCommand
    ) {
        logger.info("Executing command ($commandId, ${command.description()}) ⏳")
    }

    override fun onCommandComplete(
        commandId: Int,
        command: MaestroCommand
    ) {
        logger.info("Command ($commandId,${command.description()}) completed successfully ✅")
    }

    override fun onCommandFailed(
        commandId: Int,
        command: MaestroCommand,
        error: Throwable
    ): Orchestra.ErrorResolution {
        logger.error("Command ($commandId,${command.description()}) failed ❌")
        logger.error("Reason: ${error.message}")
        return Orchestra.ErrorResolution.FAIL
    }

    override fun onCommandSkipped(
        commandId: Int,
        command: MaestroCommand
    ) {
        logger.warn("Command ($commandId,${command.description()}) was skipped ⏭️")
    }

    override fun onCommandReset(command: MaestroCommand) {
        logger.warn("Command ${command.description()} is pending 🖐")
    }

}
