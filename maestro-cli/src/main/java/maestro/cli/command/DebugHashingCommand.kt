package maestro.cli.command

import maestro.cli.App
import maestro.cli.DisableAnsiMixin
import maestro.cli.runner.HasherTester
import maestro.cli.session.MaestroSessionManager
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "debughash",
    description = [ "Debug hashing" ],
    hidden = true
)
class DebugHashingCommand : Callable<Int> {

    @CommandLine.Mixin
    var disableANSIMixin: DisableAnsiMixin? = null

    @CommandLine.ParentCommand
    private val parent: App? = null
    override fun call(): Int {
        val deviceId = parent?.deviceId
        return MaestroSessionManager.newSession(parent?.host, parent?.port, deviceId) { session ->
            val maestro = session.maestro
            HasherTester(
                maestro
            ).startDebugSession()
            0
        }
    }

}