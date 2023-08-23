package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class SimpleIosViewDisambiguator(private val treeNode: TreeNode): ViewDisambiguator {
    override fun disambiguate(root: TreeNode, view: TreeNode): ElementSelector {
        TODO("Not yet implemented")
    }

    override fun properlyDisambiguated(selector: ElementSelector): Boolean {
        TODO("Not yet implemented")
    }
}
