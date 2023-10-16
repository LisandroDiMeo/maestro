package maestro.cli.report

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.diff.IosNodeLaxDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class IosNodeDiffTest {

    private val iosNodeLaxDiff = IosNodeLaxDiff()

    @Test
    fun `ios node is equal to itself`() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "elementType" to "42",
            )
        )
        Assertions.assertTrue(iosNodeLaxDiff.areTheSame(nodeA, nodeA))
    }

    @Test
    fun `ios node is equal to other if their element type are equals `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "elementType" to "42",
                "text" to "Hello world!"
            )
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "elementType" to "42",
                "text" to "Hello universe!"
            )
        )
        Assertions.assertTrue(iosNodeLaxDiff.areTheSame(nodeA, nodeB))
    }

    @Test
    fun `ios node is different to other if their element type are different `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "elementType" to "73",
                "text" to "Hello world!"
            )
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "elementType" to "42",
                "text" to "Hello world!"
            )
        )
        Assertions.assertFalse(iosNodeLaxDiff.areTheSame(nodeA, nodeB))
    }
}

