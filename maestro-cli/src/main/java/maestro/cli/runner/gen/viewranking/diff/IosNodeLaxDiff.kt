package maestro.cli.runner.gen.viewranking.diff

import maestro.TreeNode

class IosNodeLaxDiff : NodeDiffStrategy {
    override fun areTheSame(elementA: TreeNode, elementB: TreeNode): Boolean {
        return elementA.attributes["elementType"] == elementB.attributes["elementType"]
    }
}
