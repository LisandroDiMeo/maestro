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
        val simpleTree = TreeIndexer.removeVisualAttributes(root)
        return if (action.tapOnElement != null) {
            // Tap based action, has a selector
            val directionHash = directionsFor(
                simpleTree,
                view!!
            ).hashCode()
            val typeHash = view.attributes["type"].hashCode()
            "$directionHash-$typeHash"
        } else {
            // Remove structural attributes (such as text, resource id, etc)
            // and only keep a type attribute (elementType or className).
            when {
                action.inputRandomTextCommand != null -> {
                    val origin = action.inputRandomTextCommand?.origin
                    origin?.second?.run { TreeIndexer.removeVisualAttributes(this) }.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode()
                        .toString() + action.asCommand()?.javaClass.hashCode().toString()
                }

                action.hideKeyboardCommand != null -> {
                    val origin = action.hideKeyboardCommand?.origin
                    origin?.second?.run { TreeIndexer.removeVisualAttributes(this) }.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode()
                        .toString() + action.asCommand()?.javaClass.hashCode().toString()
                }

                action.eraseTextCommand != null -> {
                    val origin = action.eraseTextCommand?.origin
                    origin?.second?.run { TreeIndexer.removeVisualAttributes(this) }.hashCode()
                        .toString() + origin?.first?.asCommand()?.javaClass.hashCode()
                        .toString() + action.asCommand()?.javaClass.hashCode().toString()
                }

                else -> simpleTree.hashCode().toString() + action.hashCode().toString()
            }
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

fun TreeNode.clone(): TreeNode {
    return TreeNode(
        selected = selected,
        checked = checked,
        enabled = enabled,
        clickable = clickable,
        focused = focused,
        attributes = attributes.toMap().toMutableMap(),
        children = children.map { it.clone() }
    )
}
