package maestro.cli.runner.gen.viewranking.diff

import maestro.TreeNode

class AndroidNodeLaxDiff : NodeDiffStrategy {
    override fun areTheSame(elementA: TreeNode, elementB: TreeNode): Boolean {
        return elementA.attributes["className"] == elementB.attributes["className"]
    }
}
