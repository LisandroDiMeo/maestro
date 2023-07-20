package maestro.orchestra.runcycle

import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra

interface RunCycle {
    fun onCommandStart(commandId: Int, command: MaestroCommand)
    fun onCommandComplete(commandId: Int, command: MaestroCommand)
    fun onCommandFailed(commandId: Int, command: MaestroCommand, error: Throwable): Orchestra.ErrorResolution
    fun onCommandSkipped(commandId: Int, command: MaestroCommand)
    fun onCommandReset(command: MaestroCommand)
}
