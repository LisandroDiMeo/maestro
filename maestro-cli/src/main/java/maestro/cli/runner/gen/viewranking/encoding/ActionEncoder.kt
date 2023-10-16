package maestro.cli.runner.gen.viewranking.encoding

import maestro.orchestra.MaestroCommand

class ActionEncoder : Encoder<MaestroCommand, ActionIdentifier> {
    override fun encode(element: MaestroCommand): ActionIdentifier {
        return ActionIdentifier(identifier = element.hashCode().toString(), element)
    }
}
