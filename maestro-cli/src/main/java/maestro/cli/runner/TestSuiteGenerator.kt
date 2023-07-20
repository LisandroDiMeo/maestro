package maestro.cli.runner

import maestro.DeviceInfo
import maestro.Maestro
import maestro.TreeNode
import maestro.cli.device.Device
import maestro.cli.model.FlowStatus
import maestro.cli.report.TestSuiteReporter
import maestro.generation.TestGenerator
import maestro.js.JsEngine
import maestro.networkproxy.NetworkProxy
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.MaestroConfig
import maestro.orchestra.MaestroInitFlow
import maestro.orchestra.Orchestra
import maestro.orchestra.OrchestraAppState
import maestro.orchestra.TapOnElementCommand
import maestro.orchestra.runcycle.RunCycle
import maestro.orchestra.yaml.YamlCommandReader
import org.slf4j.LoggerFactory
import java.io.File

class TestSuiteGenerator(
    private val maestro: Maestro,
    private val device: Device? = null,
    private val reporter: TestSuiteReporter,
    private val packageName: String,
    private val includeTags: List<String> = emptyList(),
    private val excludeTags: List<String> = emptyList(),
) {
    private val logger = LoggerFactory.getLogger(TestSuiteInteractor::class.java)

    private fun disambiguateNode(node: TreeNode, root: TreeNode?): ElementSelector {
        return ElementSelector(
            textRegex = node.attributes["text"],
            idRegex = node.attributes["resource-id"],
            enabled = node.enabled,
            focused = node.focused,
            selected = node.selected,
        )

    }
    private val testGenerator = TestGenerator(maestro, packageName)
    private var iterations = 20

    // TODO: Avoid app leaving (check backStack?)
    //      Learning nodes (set some criteria to prioritize certain nodes?)
    //      use Ivan algorithm. At first implement random search,
    //      and avoid certain nodes.
    fun generate(){
        iterations--
        if(iterations <= 0) return
        val node = testGenerator.provideNode()
        val command = MaestroCommand(
            tapOnElement = TapOnElementCommand(
                disambiguateNode(node!!, null)
            )
        )
        val orchestra = Orchestra(
            maestro,
            onCommandStart = { _, command ->
                logger.info("${command.description()} RUNNING")
            },
            onCommandComplete = { _, command ->
                generate()
            }
        )
        orchestra.executeCommands(listOf(command))
    }

}

class TestGenerationOrchestra(
    private val maestro: Maestro,
    private val stateDir: File? = null,
    private val screenshotsDir: File? = null,
    private val lookupTimeoutMs: Long = 17000L,
    private val optionalLookupTimeoutMs: Long = 7000L,
    private val networkProxy: NetworkProxy = NetworkProxy(port = 8085),
    private val runCycle: RunCycle,
    private val nodePickerStrategy: NodePickerStrategy,
    private val commandSelectionStrategy: CommandSelectionStrategy,
    private val jsEngine: JsEngine = JsEngine()
) {
    private var copiedText: String? = null

    private var timeMsOfLastInteraction = System.currentTimeMillis()
    private var deviceInfo: DeviceInfo? = null

    private val rawCommandToMetadata = mutableMapOf<MaestroCommand, Orchestra.CommandMetadata>()

    fun startGeneration(){
        val hierarchy = maestro.viewHierarchy()
        val node = nodePickerStrategy.pickFrom(hierarchy.root)
        val command = commandSelectionStrategy.pickFrom(node)

    }

    private fun executeCommand() {

    }
}

interface Orchestrator {
    fun orchestrate(): FlowStatus
}


interface NodePickerStrategy {
    @kotlin.jvm.Throws(UnableToPickNode::class)
    fun pickFrom(treeNode: TreeNode): TreeNode

    private object UnableToPickNode : Exception()
}

interface CommandSelectionStrategy {
    @kotlin.jvm.Throws(UnableToPickCommand::class)
    fun pickFrom(treeNode: TreeNode): MaestroCommand

    private object UnableToPickCommand : Exception()
}




