package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

abstract class AttributeIsUniqueRule : DisambiguationRule {

    abstract fun selector(view: TreeNode): ElementSelector

    abstract val attribute: String

    override fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector {
        val valueOfAttributeForView = view.attributes[attribute]
        val isUnique = valueOfAttributeForView?.let {
            attributeIsUnique(
                valueOfAttributeForView,
                attribute,
                flattenNodes
            )
        } ?: false
        return if (isUnique) selector(view)
        else ElementSelector()
    }

    private fun attributeIsUnique(
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
