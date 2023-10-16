package maestro.cli.runner.gen.viewranking.encoding

import maestro.orchestra.MaestroCommand

class ScreenEncoder : Encoder<List<MaestroCommand>, ScreenIdentifier> {
    override fun encode(element: List<MaestroCommand>): ScreenIdentifier {
        return element.hashCode().toString()
    }
}
