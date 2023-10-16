package maestro.cli.runner.gen.viewranking.diff

import maestro.TreeNode

class TreeStructureEquality(
    private val nodeDiffStrategy: NodeDiffStrategy
) : TreeDiffStrategy {
    override fun areTheSame(tree: List<TreeNode>, other: List<TreeNode>): Boolean {
        val sizeEquality = TreeSizeEquality()
        return sizeEquality.areTheSame(tree, other) &&
                (tree zip other).all { (nodeA, nodeB) ->
                    nodeDiffStrategy.areTheSame(nodeA, nodeB)
                }
    }
}
