package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

/**
 * A DisambiguationRule is a mechanism to write a [ElementSelector]
 * for a view given a root and its corresponding flat tree representation.
 * Note that a disambiguation is not always possible.
 */
interface DisambiguationRule {
    /**
     * Attempts to produce an [ElementSelector] from given [view].
     */
    fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector
}
