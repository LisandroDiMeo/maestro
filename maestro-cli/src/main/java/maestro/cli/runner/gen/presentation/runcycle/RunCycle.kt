package maestro.cli.runner.gen.presentation.runcycle

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
        throw error
    }

    open fun onCommandSkipped(commandId: Int, command: MaestroCommand) {}
    open fun onCommandReset(command: MaestroCommand) {}

}

