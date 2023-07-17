package maestro.generation

import maestro.Maestro
import maestro.TreeNode
import maestro.ViewHierarchy

class TestGenerator(private val maestro: Maestro, private val packageName: String) {
    fun provideNode(): TreeNode? {
        val viewHierarchy = maestro.viewHierarchy()
        return traverseTree(viewHierarchy.root).randomOrNull()
    }

    private fun traverseTree(treeNode: TreeNode): List<TreeNode> {
        val nodes = mutableListOf<TreeNode>()
        if(treeNode.clickable == true) nodes.add(treeNode)
        treeNode.children.forEach { child ->
            nodes.addAll(traverseTree(child))
        }
        return nodes
    }
}

