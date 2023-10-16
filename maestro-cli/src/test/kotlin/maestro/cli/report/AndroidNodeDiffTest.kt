package maestro.cli.report

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.diff.AndroidNodeLaxDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class AndroidNodeDiffTest {

    private val androidNodeLaxDiff = AndroidNodeLaxDiff()

    @Test
    fun `android node is equal to itself`() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "className" to "ExampleClassName",
            )
        )
        Assertions.assertTrue(androidNodeLaxDiff.areTheSame(nodeA, nodeA))
    }

    @Test
    fun `android node is equal to other if their class name are equals `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "className" to "ExampleClassName",
                "text" to "Hello world!"
            )
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "className" to "ExampleClassName",
                "text" to "Hello universe!"
            )
        )
        Assertions.assertTrue(androidNodeLaxDiff.areTheSame(nodeA, nodeB))
    }

    @Test
    fun `android node is different to other if their class name are different `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "className" to "ExampleClassName",
                "text" to "Hello world!"
            )
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "className" to "ExampleClassName2",
                "text" to "Hello world!"
            )
        )
        Assertions.assertFalse(androidNodeLaxDiff.areTheSame(nodeA, nodeB))
    }
}

