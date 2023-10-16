package maestro.cli.runner.gen.viewranking.diff

import maestro.TreeNode

class NodeStrictDiff : NodeDiffStrategy {
    override fun areTheSame(elementA: TreeNode, elementB: TreeNode): Boolean {
        return elementA.attributes == elementB.attributes &&
                elementA.clickable == elementB.clickable &&
                elementA.checked == elementB.checked &&
                elementA.enabled == elementB.enabled &&
                elementA.focused == elementB.focused &&
                elementA.selected == elementB.selected
    }
}
