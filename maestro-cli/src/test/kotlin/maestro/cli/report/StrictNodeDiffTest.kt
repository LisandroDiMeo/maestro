package maestro.cli.report

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.diff.NodeStrictDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StrictNodeDiffTest {

    private val strictNodeDiffTest = NodeStrictDiff()

    @Test
    fun `a node is equal to itself in strict diff`() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello",
                "resource-id" to "R.id.element"
            ),
            clickable = true,
            enabled = true
        )
        Assertions.assertTrue(strictNodeDiffTest.areTheSame(nodeA, nodeA))
    }

    @Test
    fun `node is equal to other if their attributes equals `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello",
                "resource-id" to "R.id.element"
            ),
            clickable = true,
            enabled = true
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello",
                "resource-id" to "R.id.element"
            ),
            clickable = true,
            enabled = true
        )
        Assertions.assertTrue(strictNodeDiffTest.areTheSame(nodeA, nodeB))
    }

    @Test
    fun `node is equal to other if their attributes equals even if their children not `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello",
                "resource-id" to "R.id.element"
            ),
            clickable = true,
            enabled = true
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello",
                "resource-id" to "R.id.element"
            ),
            clickable = true,
            enabled = true,
            children = listOf(nodeA)
        )
        Assertions.assertTrue(strictNodeDiffTest.areTheSame(nodeA, nodeB))
    }

    @Test
    fun `node is different to other if their attributes are different `() {
        val nodeA = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello world!"
            ),
            enabled = true
        )
        val nodeB = TreeNode(
            attributes = mutableMapOf(
                "text" to "Hello world!"
            ),
            enabled = false
        )
        Assertions.assertFalse(strictNodeDiffTest.areTheSame(nodeA, nodeB))
    }
}

