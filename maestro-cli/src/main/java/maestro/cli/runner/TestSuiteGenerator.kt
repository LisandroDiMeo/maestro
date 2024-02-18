package maestro.cli.runner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import maestro.Maestro
import maestro.cli.device.Device
import maestro.cli.device.Platform
import maestro.cli.runner.gen.TestGenerationOrchestra
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.hierarchyanalyzer.IOSHierarchyAnalyzer
import maestro.cli.runner.gen.presentation.flowfilegeneration.FlowFileWriter
import maestro.cli.runner.gen.presentation.model.AppGraphicalModel
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.cli.runner.gen.presentation.runcycle.TestSuiteGeneratorCycle
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import org.slf4j.Logger

class TestSuiteGenerator(
    private val maestro: Maestro,
    private val device: Device? = null,
    private val packageName: String,
    private val testSuiteSize: Int,
    private val testSize: Int,
    private val endTestIfOutsideApp: Boolean = false,
    private val strategy: String,
    private val logger: Logger,
    private val flowFileWriter: FlowFileWriter
) {

    fun generate() {
        val appGraphicalModel = AppGraphicalModel("generated-flows/${packageName}/$strategy")
        val executedCommandsObservable = ExecutedCommandsObservable()
        val strategy = CommandSelectionStrategy.strategyFor(
            strategy,
            executedCommandsObservable
        )
        val modelBuildingJob = CoroutineScope(Dispatchers.IO).launch {
            executedCommandsObservable.commandInformationState.collect {
                it?.let {
                    if (it.commandExecuted.stopAppCommand != null) cancel()
                    appGraphicalModel.updateModel(it)
                }
            }
        }
        val shouldUseFallbackMechanism = device?.platform == Platform.IOS
        val disambiguationRule =
            SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether(
                shouldUseFallbackMechanism
            )
        val analyzer = hierarchyAnalyzer(
            strategy,
            disambiguationRule
        )
        analyzer?.let {
            val testGenerator = TestGenerationOrchestra(
                maestro = maestro,
                packageName = packageName,
                runCycle = TestSuiteGeneratorCycle(logger),
                hierarchyAnalyzer = it,
                testSize = testSize,
                logger = logger,
                endTestIfOutsideApp = endTestIfOutsideApp
            )
            for (testId in 1..testSuiteSize) {
                testGenerator.startGeneration()
                logger.info("Brewing up Flow File $testId ☕️...")
                flowFileWriter.writeFlowFileFrom(
                    testGenerator.generatedCommands(),
                    testId
                )
            }
            executedCommandsObservable.close()
        }
        runBlocking { modelBuildingJob.join() }
        appGraphicalModel.outputModel()
    }

    private fun hierarchyAnalyzer(
        strategy: CommandSelectionStrategy,
        disambiguationRule: SequentialDisambiguation
    ) = when (device?.platform) {
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

}

data class ConfigHeader(val appId: String)
