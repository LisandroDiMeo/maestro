package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

abstract class ViewDisambiguator {
    /**
     * This method has to be thought as best-effort, since due to multiple reasons
     * (such as flakiness) the disambiguated view is not correctly reachable.
     * It's recommended that after view is disambiguated, check with [properlyDisambiguated]
     * if it satisfy that criteria.
     */
    abstract fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector

    abstract fun properlyDisambiguated(selector: ElementSelector): Boolean

    fun attributeIsUnique(
        value: String,
        attribute: String,
        flattenTree: List<TreeNode>
    ): Boolean {
        flattenTree.filter {
            val otherValue = it.attributes[attribute]
            (otherValue ?: "") == value
        }.also { return it.size == 1 }
    }

}

