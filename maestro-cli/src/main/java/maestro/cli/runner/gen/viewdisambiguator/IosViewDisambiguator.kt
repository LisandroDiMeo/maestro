package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class IosViewDisambiguator: ViewDisambiguator() {

    override fun disambiguate(root: TreeNode, view: TreeNode, flattenNodes: List<TreeNode>):
        ElementSelector {
        // First, we disambiguate with some trivial checks
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
        if (view.children.isNotEmpty()) {
            val childMatchers = view.children.map { disambiguate(root, it, flattenNodes) }
            childMatchers.firstOrNull { it != ElementSelector() }?.let {
                return ElementSelector(containsChild = it)
            }
        } else {
            return ElementSelector()
        }
        return ElementSelector(classNameRegex = null) // No selector
    }

    override fun properlyDisambiguated(selector: ElementSelector): Boolean {
        return !selector.idRegex.isNullOrEmpty() ||
            !selector.textRegex.isNullOrEmpty() ||
            selector.containsChild != null
    }

}
