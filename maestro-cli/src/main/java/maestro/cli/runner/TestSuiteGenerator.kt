package maestro.cli.runner

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import maestro.Maestro
import maestro.TreeNode
import maestro.cli.device.Device
import maestro.cli.device.Platform
import maestro.cli.runner.gen.TestGenerationOrchestra
import maestro.cli.runner.gen.commandselection.CommandSelectionStrategy
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.hierarchyanalyzer.IOSHierarchyAnalyzer
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import maestro.cli.runner.gen.viewranking.ViewRanking
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
    private val packageName: String,
    private val testSuiteSize: Int,
    private val testSize: Int,
    private val endTestIfOutsideApp: Boolean = false,
    private val strategy: String,
) {

    private val logger = LoggerFactory.getLogger(TestSuiteGenerator::class.java)

    init {
        LogConfig.switchLogbackConfiguration(
            "maestro-cli/src/main/resources/logback-generative.xml"
        )
    }

    data class ConfigHeader(val appId: String)

    fun generate() {
        val strategy = CommandSelectionStrategy.strategyFor(strategy)
        val disambiguationRule =
            SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether()
        val analyzer = when (device?.platform) {
            Platform.ANDROID -> AndroidHierarchyAnalyzer(
                strategy,
                disambiguationRule
            )

            Platform.IOS -> IOSHierarchyAnalyzer(
                strategy,
                disambiguationRule
            )

            else -> null
        }
        analyzer?.let {
            val testGenerator = TestGenerationOrchestra(
                maestro = maestro,
                packageName = packageName,
                runCycle = TestSuiteGeneratorCycle(logger),
                hierarchyAnalyzer = it,
                testSize = testSize,
                endTestIfOutsideApp = endTestIfOutsideApp
            )
            for (testId in 1..testSuiteSize) {
                testGenerator.startGeneration()
                generateFlowFile(
                    testGenerator.generatedCommands(),
                    testId
                )
            }
            if (strategy is ViewRanking) {
                outputModel(strategy.exploration)
            }

        }
    }

    private fun outputModel(model: Map<String, TreeNode>) {
        FileOutputStream("output.txt", true).use {
            model.forEach { t, u ->
                val s = "$t -> $u"
                s.toByteArray(charset = Charsets.UTF_8).apply {
                    it.write(this)
                }
            }
        }
    }


    private fun generateFlowFile(
        commands: List<MaestroCommand>,
        id: Int = 0
    ) {
        logger.info("Brewing up Flow File $id ☕️...")
        val yamlCommands = commands.map { YamlFluentCommand.fromCommand(it) }
        val config = ConfigHeader(packageName)
        val flowFileMapper = ObjectMapper(YAMLFactory())
        val configHeaderMapper =
            ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
        flowFileMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val tempGeneratedFlowFile = File("temp-generated-flow.yaml")
        flowFileMapper.writeValue(
            tempGeneratedFlowFile,
            yamlCommands
        )
        val configFile = File("config.yaml")
        configHeaderMapper.writeValue(
            configFile,
            config
        )
        val generatedFlowFile = File("generated-flows/generated-flow-$id.yaml")
        FileOutputStream(
            generatedFlowFile,
            true
        ).use { output ->
            configFile
                .forEachBlock { buffer, bytesRead ->
                    output.write(
                        buffer,
                        0,
                        bytesRead
                    )
                }
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
            "maestro-cli/src/main/resources/logback-generative.xml"
        )
    }

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




