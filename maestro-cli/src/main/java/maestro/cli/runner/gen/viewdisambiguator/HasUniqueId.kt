package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class HasUniqueId : AttributeIsUniqueRule() {

    override val attribute: String = "resource-id"
    override fun selector(view: TreeNode): ElementSelector =
        ElementSelector(idRegex = view.attributes[attribute])
}
