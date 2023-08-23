package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

interface ViewDisambiguator {
    /**
     * This method has to be thought as best-effort, since due to multiple reasons
     * (such as flakiness) the disambiguated view is not correctly reachable.
     * It's recommended that after view is disambiguated, check with [properlyDisambiguated]
     * if it satisfy that criteria.
     */
    fun disambiguate(root: TreeNode, view: TreeNode): ElementSelector

    fun properlyDisambiguated(selector: ElementSelector): Boolean

}
