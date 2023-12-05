package maestro.cli.generative

import maestro.cli.runner.gen.viewranking.ActionInformation
import maestro.cli.runner.gen.viewranking.MinimumHopsFinder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MinimumHopsFinderTest {
    @Test
    fun `given a model where an action A discovers an action B, the hops for A to B is one`() {
        val model = mapOf(
            "A" to ActionInformation(
                listOf("B"),
                1
            ),
            "B" to ActionInformation(
                emptyList(),
                1
            )
        )
        val paths = MinimumHopsFinder.minimumHopsForSources(
            model,
            listOf("A"),
            listOf("B")
        )
        Assertions.assertEquals(
            2,
            paths["A"]!!.size
        )
    }

    @Test
    fun `given a model where an action A cannot discover new actions, no path is retrieved`() {
        val model = mapOf(
            "A" to ActionInformation(
                listOf(),
                1
            ),
            "B" to ActionInformation(
                emptyList(),
                1
            )
        )
        val paths = MinimumHopsFinder.minimumHopsForSources(
            model,
            listOf("A"),
            listOf("B")
        )
        Assertions.assertEquals(
            null,
            paths["A"]
        )
    }

    @Test
    fun `given a model where an action A can reach B after more than one hop, the path can reach to B`() {
        val model = mapOf(
            "A" to ActionInformation(
                listOf("A1"),
                1
            ),
            "A1" to ActionInformation(
                listOf("A2"),
                1
            ),
            "A2" to ActionInformation(
                listOf("A3"),
                1
            ),
            "A3" to ActionInformation(
                listOf("B"),
                1
            ),
            "B" to ActionInformation(
                emptyList(),
                1
            )
        )
        val paths = MinimumHopsFinder.minimumHopsForSources(
            model,
            listOf("A"),
            listOf("B")
        )
        paths["A"]!!.also { path ->
            var current = model[path.first()]!!.first
            for (p in path) {
                if (p == path.first()) continue
                Assertions.assertTrue(p in current)
                current = model[p]!!.first
            }
            Assertions.assertEquals(
                "B",
                path.last()
            )
        }
    }

    @Test
    fun `building the path from A to B where A can reach by two ways, it picks the shortest`() {
        val model = mapOf(
            "A" to ActionInformation(
                listOf("A1"),
                1
            ),
            "A1" to ActionInformation(
                listOf(
                    "A2",
                    "A3"
                ),
                1
            ),
            "A2" to ActionInformation(
                listOf("A3"),
                1
            ),
            "A3" to ActionInformation(
                listOf("B"),
                1
            ),
            "B" to ActionInformation(
                emptyList(),
                1
            )
        )
        val paths = MinimumHopsFinder.minimumHopsForSources(
            model,
            listOf("A"),
            listOf("B")
        )
        Assertions.assertFalse("A2" in paths["A"]!!)
    }

    @Test
    fun `hops finder is able to find paths for multiple sources and multiple targets`() {
        val model = mapOf(
            "A" to ActionInformation(
                listOf("D"),
                1
            ),
            "B" to ActionInformation(
                listOf("E"),
                1
            ),
            "C" to ActionInformation(
                listOf("C"),
                1
            ),
            "D" to ActionInformation(
                listOf(
                    "E",
                    "F"
                ),
                1
            ),
            "E" to ActionInformation(
                listOf("G"),
                1
            ),
            "F" to ActionInformation(
                listOf("H"),
                1
            ),
            "G" to ActionInformation(
                listOf(
                    "E",
                    "J"
                ),
                1
            ),
            "H" to ActionInformation(
                listOf("K"),
                1
            ),
            "I" to ActionInformation(
                listOf(),
                1
            ),
            "J" to ActionInformation(
                listOf(),
                1
            ),
            "K" to ActionInformation(
                listOf("I"),
                1
            )
        )
        // All paths to I and J
        // A -> D -> E -> G -> J
        // A -> D -> F -> H -> K -> I
        // B -> E -> G -> J
        // C cannot reach I nor J
        val paths = MinimumHopsFinder.minimumHopsForSources(
            model,
            listOf(
                "A",
                "B",
                "C"
            ),
            listOf(
                "I",
                "J"
            ),
        )
        Assertions.assertFalse("C" in paths.keys)
        Assertions.assertTrue("A" in paths.keys)
        Assertions.assertTrue("B" in paths.keys)
    }
}
