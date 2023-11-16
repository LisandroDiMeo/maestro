package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class HasUniqueText : AttributeIsUniqueRule() {

    override val attribute: String = "text"
    override fun selector(view: TreeNode): ElementSelector =
        ElementSelector(textRegex = view.attributes[attribute])
}
