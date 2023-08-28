package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class SimpleIosViewDisambiguator: ViewDisambiguator {

    override fun disambiguate(root: TreeNode, view: TreeNode, flattenNodes: List<TreeNode>):
        ElementSelector {
        val idRegex = view.attributes["resource-id"]
        idRegex?.let {
            if (attributeIsUnique(it, "resource-id", flattenNodes)) return ElementSelector(
                idRegex = it,
            )
        }
        val textRegex = view.attributes["text"]
        textRegex?.let {
            if (attributeIsUnique(it, "text", flattenNodes)) return ElementSelector(
                textRegex = it,
                idRegex = idRegex,
            )
        }
        val accessibilityTextRegex = view.attributes["accessibilityText"]
        accessibilityTextRegex?.let {
            if (attributeIsUnique(it, "accessibilityText", flattenNodes)) return ElementSelector(
                textRegex = it,
                idRegex = idRegex
            )
        }
        return ElementSelector(
            textRegex = if (textRegex.isNullOrEmpty()) accessibilityTextRegex else textRegex,
            idRegex = idRegex,
        )
    }

    private fun directAncestor(view: TreeNode, flattenTree: List<TreeNode>): TreeNode? {
        return flattenTree.firstOrNull { view in it.children }
    }

    override fun properlyDisambiguated(selector: ElementSelector): Boolean {
        if (
            selector.textRegex.isNullOrEmpty()
            && selector.idRegex.isNullOrEmpty()
            && selector.below == null
        ) return false
        var belowSelectors = 0
        val belowSelectorsThreshold = 3
        while (selector.below != null && belowSelectors < belowSelectorsThreshold) {
            selector.below?.let { belowSelectors++ }
        }
        return belowSelectors < belowSelectorsThreshold
    }

    private fun attributeIsUnique(value: String, attribute: String, flattenTree: List<TreeNode>):
        Boolean {
        flattenTree.filter {
            val otherValue = it.attributes[attribute]
            (otherValue ?: "") == value
        }.also { return it.size == 1 }
    }
}
