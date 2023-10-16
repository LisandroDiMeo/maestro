package maestro.cli.runner.gen.viewranking.diff

interface ElementDiffStrategy<T> {
    fun areTheSame(elementA: T, elementB: T): Boolean
}
