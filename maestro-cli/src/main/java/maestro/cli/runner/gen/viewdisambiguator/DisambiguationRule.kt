package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

interface DisambiguationRule {
    fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector
}
