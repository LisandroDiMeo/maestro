package maestro.cli.runner.gen.viewranking.encoding

interface Encoder<T, E> {
    fun encode(element: T): E
}
