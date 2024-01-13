package maestro.cli.runner.gen.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.orchestra.MaestroCommand
import maestro.orchestra.StopAppCommand

class ExecutedCommandsObservable {
    private val _commandInformationState: MutableStateFlow<CommandInformation?> =
        MutableStateFlow(null)
    val commandInformationState = _commandInformationState.asStateFlow()

    /**
     * Enqueue to the [commandInformationState] flow an update for the previously executed action.
     * Usually you may call this method after you know the destinations for such action, so you can properly
     * specify the [CommandInformation.destinations].
     * See RandomCommandSelection strategy for a sample usage.
     * @see maestro.cli.runner.gen.commandselection.strategies.RandomCommandSelection
     */
    fun performUpdate(commandInformation: CommandInformation) {
        _commandInformationState.value = commandInformation
    }

    /**
     * Enqueue to the [commandInformationState] a [StopAppCommand] action so the state flow can be
     * somehow closed.
     */
    fun close() {
        _commandInformationState.value = CommandInformation(
            commandExecuted = MaestroCommand(stopAppCommand = StopAppCommand("")),
            hash = "",
            destinations = emptyList(),
            usages = 0
        )
    }
}
