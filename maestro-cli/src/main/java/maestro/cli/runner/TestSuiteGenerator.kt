package maestro.cli.runner

import maestro.Maestro
import maestro.cli.device.Device
import maestro.cli.report.TestSuiteReporter
import maestro.cli.runner.gen.TestGenerationOrchestra
import maestro.cli.runner.gen.commandselection.AndroidCommandSelection
import maestro.debuglog.LogConfig
import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import maestro.orchestra.runcycle.RunCycle
import org.slf4j.LoggerFactory

class TestSuiteGenerator(
    private val maestro: Maestro,
    private val device: Device? = null,
    private val reporter: TestSuiteReporter,
    private val packageName: String,
) {
    fun generate() {
        TestGenerationOrchestra(
            maestro = maestro,
            packageName = packageName,
            runCycle = TestSuiteGeneratorCycle(),
            commandSelectionStrategy = AndroidCommandSelection()
        ).startGeneration()
    }

}

class TestSuiteGeneratorCycle : RunCycle {

    private val logger = LoggerFactory.getLogger(TestSuiteGenerator::class.java)

    init {
        LogConfig.switchLogbackConfiguration(
            "/Users/lisandrodimeo/"
                + "Documents/Me/maestro/"
                + "maestro-cli/src/main/"
                + "resources/logback-generative.xml"
        )
    }

    override fun onCommandStart(commandId: Int, command: MaestroCommand) {
        logger.info("Executing command ($commandId, ${command.description()}) ⏳")
    }

    override fun onCommandComplete(commandId: Int, command: MaestroCommand) {
        logger.info("Command ($commandId,${command.description()}) completed successfully ✅")
    }

    override fun onCommandFailed(
        commandId: Int, command: MaestroCommand, error: Throwable
    ): Orchestra.ErrorResolution {
        logger.error("Command ($commandId,${command.description()}) failed ❌")
        logger.error("Reason: ${error.message}")
        return Orchestra.ErrorResolution.FAIL
    }

    override fun onCommandSkipped(commandId: Int, command: MaestroCommand) {
        logger.warn("Command ($commandId,${command.description()}) was skipped ⏭️")
    }

    override fun onCommandReset(command: MaestroCommand) {
        logger.warn("Command ${command.description()} is pending 🖐")
    }

}




