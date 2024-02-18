package maestro.cli.runner.gen.actionhash

import maestro.TreeNode
import maestro.orchestra.MaestroCommand

/**
 * Hasher that produces a hash by checking:
 * - The directions for the view in the tree (how to traverse the tree to reach the action)
 * - The element type
 * - If the actions is not executed on a view (it's not Tap command) uses its action type.
 */
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
