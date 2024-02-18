package maestro.cli.runner.gen.actionhash

import maestro.TreeNode

object TreeIndexer {

    fun addTypeAndIndex(root: TreeNode): TreeNode {
        val newTree = root.clone()
        addIndexesToTree(
            newTree,
            0
        )
        return newTree
    }

    fun removeVisualAttributes(root: TreeNode): TreeNode {
        val newTree = root.clone()
        removeVisualAttributesInplace(newTree)
        return newTree
    }

    private fun removeVisualAttributesInplace(root: TreeNode) {
        val index = root.attributes["index"]
        val type = root.attributes["type"]
        root.attributes.clear()
        root.attributes["index"] = index ?: ""
        root.attributes["type"] = type ?: ""
        root.children.forEach {
            removeVisualAttributesInplace(it)
        }
    }

    private fun addIndexesToTree(
        root: TreeNode,
        i: Int = 0
    ): Int {
        val type = ((root.attributes["elementType"] ?: "").ifEmpty { root.attributes["className"] })
        root.attributes["index"] = i.toString()
        root.attributes["type"] = type ?: ""
        var k = i + 1
        root.children.forEach {
            k = addIndexesToTree(
                it,
                k
            )
        }
        return k
    }
}
