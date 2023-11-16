package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class HasUniqueAccessibilityText : AttributeIsUniqueRule() {
    override val attribute: String = "accessibilityText"

    override fun selector(view: TreeNode): ElementSelector =
        ElementSelector(textRegex = view.attributes[attribute])
}
