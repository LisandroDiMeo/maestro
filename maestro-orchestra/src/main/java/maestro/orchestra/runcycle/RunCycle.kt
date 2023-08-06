package maestro.orchestra.runcycle

import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra

abstract class RunCycle {
    open fun onCommandStart(commandId: Int, command: MaestroCommand) {}
    open fun onCommandComplete(commandId: Int, command: MaestroCommand) {}
    open fun onCommandFailed(
        commandId: Int,
        command: MaestroCommand,
        error: Throwable
    ): Orchestra.ErrorResolution {
        return Orchestra.ErrorResolution.FAIL
    }

    open fun onCommandSkipped(commandId: Int, command: MaestroCommand) {}
    open fun onCommandReset(command: MaestroCommand) {}

}
