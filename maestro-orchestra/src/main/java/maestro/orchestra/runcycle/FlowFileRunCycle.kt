package maestro.orchestra.runcycle

import maestro.MaestroException
import maestro.orchestra.MaestroCommand
import maestro.orchestra.Orchestra
import java.util.IdentityHashMap
import java.util.logging.Logger

abstract class FlowFileRunCycle: RunCycle() {
    open fun onFlowStart(commands: List<MaestroCommand>) {}

    open fun onCommandMetadataUpdate(command: MaestroCommand, metadata: Orchestra.CommandMetadata){}
}
