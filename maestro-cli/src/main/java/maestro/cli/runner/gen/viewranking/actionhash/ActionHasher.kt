package maestro.cli.runner.gen.viewranking.actionhash

import maestro.TreeNode
import maestro.orchestra.MaestroCommand

interface ActionHasher {
    fun hashAction(
        root: TreeNode,
        action: MaestroCommand,
        view: TreeNode?
    ): String
}

class TreeDirectionHasher : ActionHasher {

    override fun hashAction(
        root: TreeNode,
        action: MaestroCommand,
        view: TreeNode?
    ): String {
        return if (action.tapOnElement != null) {
            // Tap based action, has a selector
            val directionHash = directionsFor(
                root,
                view!!
            ).hashCode()
            val typeHash = view.attributes["type"].hashCode()
            "${root.hashCode()}-$directionHash-$typeHash"
        } else {
            // Remove structural attributes (such as text, resource id, etc)
            // and only keep a type attribute (elementType or className).
            val extraHash = when {
                action.inputRandomTextCommand != null -> {
                    val origin = action.inputRandomTextCommand?.origin
                    origin?.second.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode().toString()
                }

                action.hideKeyboardCommand != null -> {
                    val origin = action.hideKeyboardCommand?.origin
                    origin?.second.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode().toString()
                }

                action.eraseTextCommand != null -> {
                    val origin = action.eraseTextCommand?.origin
                    origin?.second.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode().toString()
                }

                else -> action.hashCode().toString()
            }
            root.hashCode().toString() + extraHash
        }
    }

    private fun directionsFor(
        root: TreeNode,
        target: TreeNode
    ): String {
        val deque = ArrayDeque<TreeNode>()
        directionsOnTree(
            deque,
            root,
            target
        )
        deque.reverse()
        return buildString {
            deque.forEach { append("(${it.attributes["index"]})") }
        }
    }

    private fun directionsOnTree(
        deque: ArrayDeque<TreeNode>,
        current: TreeNode,
        target: TreeNode
    ) {
        deque.addFirst(current)
        val areTheSame = current.attributes["index"] == target.attributes["index"]
        if (current.children.isEmpty() && !areTheSame) {
            // If leaf reached, and are not the same element, then pop
            deque.removeFirst()
            return
        }
        if (areTheSame) {
            // If are the same node, then break the search.
            return
        }
        // Check if a children contains a path to the target
        val currentSize = deque.size
        for (child: TreeNode in current.children) {
            directionsOnTree(
                deque,
                child,
                target
            )
            // If path found, then break the search
            if (currentSize != deque.size) break
        }
        // No path found for all children, pop this node.
        if (currentSize == deque.size) deque.removeFirst()
    }

}

object TreeIndexer {

    fun simplifyTree(root: TreeNode): TreeNode {
        val newTree = root.copy()
        addIndexesToTree(
            newTree,
            0
        )
        return newTree
    }

    fun addIndexesToTree(
        root: TreeNode,
        i: Int = 0
    ): Int {
        val type = ((root.attributes["elementType"] ?: "").ifEmpty { root.attributes["className"] })
        root.attributes.clear()
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


fun main() {

}

private fun testAddIndex() {
    val t = TreeNode(
        attributes = mutableMapOf("letter" to "R"),
        children = listOf(
            TreeNode(
                attributes = mutableMapOf("letter" to "A"),
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf("letter" to "C"),
                    ),
                    TreeNode(
                        attributes = mutableMapOf("letter" to "D"),
                    )
                )
            ),
            TreeNode(
                attributes = mutableMapOf("letter" to "B"),
            )
        )
    )
    TreeIndexer.addIndexesToTree(t)
    println(t.aggregate().map { it.attributes["index"] + "-" + it.attributes["letter"] }.toString())
}
