package maestro.cli.runner.gen.viewranking.diff

import maestro.TreeNode

class TreeSizeEquality : TreeDiffStrategy {
    override fun areTheSame(tree: List<TreeNode>, other: List<TreeNode>): Boolean {
        return tree.size == other.size
    }
}
