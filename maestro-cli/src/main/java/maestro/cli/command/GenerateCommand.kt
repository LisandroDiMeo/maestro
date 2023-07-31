package maestro.cli.command

import maestro.cli.App
import maestro.cli.DisableAnsiMixin
import maestro.cli.report.ReportFormat
import maestro.cli.report.ReporterFactory
import maestro.cli.runner.TestSuiteGenerator
import maestro.cli.session.MaestroSessionManager
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "generate",
    description = [ "Explore and generate" ],
    hidden = true
)
class GenerateCommand : Callable<Int> {

    @CommandLine.Mixin
    var disableANSIMixin: DisableAnsiMixin? = null

    @CommandLine.ParentCommand
    private val parent: App? = null

    @CommandLine.Parameters
    private lateinit var packageName: String

    override fun call(): Int {
        val deviceId = parent?.deviceId
        return MaestroSessionManager.newSession(parent?.host, parent?.port, deviceId) { session ->
            val maestro = session.maestro
            val device = session.device
            TestSuiteGenerator(
                maestro = maestro,
                device = device,
                reporter = ReporterFactory.buildReporter(ReportFormat.JUNIT, "GeneratedSuite"),
                packageName
            ).generate()
            1
        }
    }
}
