package maestro.cli.runner.gen.commandselection.strategies.viewranking

fun <T> maximumValuesBy(comparator: Comparator<T>, elements: List<T>): List<T> {
    return elements.filter { x ->
        val aux = elements.map { y ->
            comparator.compare(x, y)
        }
        -1 !in aux
    }
}

fun <T> minimumValuesBy(comparator: Comparator<T>, elements: List<T>): List<T> {
    return elements.filter { x ->
        val aux = elements.map { y ->
            comparator.compare(x, y)
        }
        1 !in aux
    }
}
