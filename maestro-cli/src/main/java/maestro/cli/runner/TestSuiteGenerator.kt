package maestro.cli.runner

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import maestro.Maestro
import maestro.cli.device.Device
import maestro.cli.device.Platform
import maestro.cli.report.TestSuiteReporter
import maestro.cli.runner.gen.TestGenerationOrchestra
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.commandselection.RandomCommandSelection
import maestro.cli.runner.gen.hierarchyanalyzer.IOSHierarchyAnalyzer
import maestro.cli.runner.gen.viewdisambiguator.SimpleAndroidViewDisambiguator
import maestro.debuglog.LogConfig
import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import maestro.orchestra.runcycle.RunCycle
import maestro.orchestra.yaml.YamlFluentCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream

class TestSuiteGenerator(
    private val maestro: Maestro,
    private val device: Device? = null,
    private val reporter: TestSuiteReporter,
    private val packageName: String,
) {

    private val logger = LoggerFactory.getLogger(TestSuiteGenerator::class.java)

    init {
        LogConfig.switchLogbackConfiguration(
            "/Users/lisandrodimeo/"
                + "Documents/Me/maestro/"
                + "maestro-cli/src/main/"
                + "resources/logback-generative.xml"
        )
    }

    data class ConfigHeader(val appId: String)

    fun generate() {
        val strategy = RandomCommandSelection()
        val analyzer = when(device?.platform) {
            Platform.ANDROID -> AndroidHierarchyAnalyzer(
                strategy,
                SimpleAndroidViewDisambiguator(maestro.viewHierarchy().root),
                maestro.deviceInfo()
            )
            Platform.IOS -> IOSHierarchyAnalyzer(
                strategy,
                SimpleAndroidViewDisambiguator(maestro.viewHierarchy().root),
                maestro.deviceInfo()
            )
            else -> null
        }
        analyzer?.let {
            val testGenerator = TestGenerationOrchestra(
                maestro = maestro,
                packageName = packageName,
                runCycle = TestSuiteGeneratorCycle(logger),
                hierarchyAnalyzer = it
            )
            testGenerator.startGeneration()
            generateFlowFile(testGenerator.generatedCommands())
        }
    }


    private fun generateFlowFile(commands: List<MaestroCommand>) {
        logger.info("Brewing up Flow File ☕️...")
        val yamlCommands = commands.map { YamlFluentCommand.fromCommand(it) }
        val config = ConfigHeader(packageName)
        val flowFileMapper = ObjectMapper(YAMLFactory())
        val configHeaderMapper =
            ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
        flowFileMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val tempGeneratedFlowFile = File("temp-generated-flow.yaml")
        flowFileMapper.writeValue(tempGeneratedFlowFile, yamlCommands)
        val configFile = File("config.yaml")
        configHeaderMapper.writeValue(configFile, config)
        val generatedFlowFile = File("generated-flow.yaml")
        FileOutputStream(generatedFlowFile, true).use { output ->
            configFile
                .forEachBlock { buffer, bytesRead -> output.write(buffer, 0, bytesRead) }
            tempGeneratedFlowFile
                .forEachBlock { buffer, bytesRead ->
                    output.write(
                        buffer,
                        0,
                        bytesRead
                    )
                }
        }
        tempGeneratedFlowFile.delete()
        configFile.delete()
    }

}

class TestSuiteGeneratorCycle(private val logger: Logger) : RunCycle() {

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




