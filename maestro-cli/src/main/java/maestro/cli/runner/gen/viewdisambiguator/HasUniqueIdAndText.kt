package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class HasUniqueIdAndText : AttributeIsUniqueRule() {

    override val attribute: String = "resource-id,text,accessibilityText"
    private val attributesToCheck = attribute.split(",")

    override fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector {
        val valuesOfAttributeForView = attributesToCheck.map { attr -> attr to view.attributes[attr] }
        val isUnique = attributeCombinationIsUnique(
            valuesOfAttributeForView,
            flattenNodes
        )
        return if (isUnique) selector(view)
        else ElementSelector()
    }

    private fun attributeCombinationIsUnique(
        values: List<Pair<String, String?>>,
        flattenTree: List<TreeNode>
    ): Boolean {
        flattenTree.filter {
            val otherValues = attributesToCheck.map { attr -> attr to it.attributes[attr] }
            otherValues.sortedBy { it.first } == values.sortedBy { it.first }
        }.also { return it.size == 1 }
    }

    override fun selector(view: TreeNode): ElementSelector =
        ElementSelector(
            idRegex = view.attributes["resource-id"],
            textRegex = view.attributes["text"] ?: view.attributes["accessibilityText"],
        )
}
