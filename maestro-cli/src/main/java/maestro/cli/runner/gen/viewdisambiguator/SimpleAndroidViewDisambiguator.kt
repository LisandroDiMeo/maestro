package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class SimpleAndroidViewDisambiguator : ViewDisambiguator {

    override fun disambiguate(root: TreeNode, view: TreeNode, flattenNodes: List<TreeNode>):
        ElementSelector {
        // TODO: Refactor this to a set of self-evaluating rules. More clean and flexible.
        // First, we disambiguate with some trivial checks
        val idRegex = view.attributes["resource-id"]
        idRegex?.let {
            if (attributeIsUnique(it, "resource-id", flattenNodes)) return ElementSelector(
                idRegex = it,
            )
        }
        val textRegex = view.attributes["text"]
        textRegex?.let {
            if (attributeIsUnique(it, "text",flattenNodes)) return ElementSelector(
                textRegex = it,
                idRegex = idRegex,
            )
        }
        val accessibilityTextRegex = view.attributes["accessibilityText"]
        accessibilityTextRegex?.let {
            if (attributeIsUnique(it, "accessibilityText",flattenNodes)) return ElementSelector(
                textRegex = it,
                idRegex = idRegex
            )
        }
        val classNameRegex = view.attributes["className"]
        classNameRegex?.let {
            if (attributeIsUnique(it, "className",flattenNodes)) return ElementSelector(
                textRegex = if (textRegex.isNullOrEmpty()) accessibilityTextRegex else textRegex,
                idRegex = idRegex,
                classNameRegex = it
            )
        }
        val packageNameRegex = view.attributes["packageName"]
        packageNameRegex?.let {
            if (attributeIsUnique(it, "packageName", flattenNodes)) return ElementSelector(
                textRegex = if (textRegex.isNullOrEmpty()) accessibilityTextRegex else textRegex,
                idRegex = idRegex,
                classNameRegex = classNameRegex,
                packageNameRegex = it
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
        return true // the fuck?
    }

    private fun attributeIsUnique(value: String, attribute: String, flattenTree: List<TreeNode>): Boolean {
        flattenTree.filter {
            val otherValue = it.attributes[attribute]
            (otherValue ?: "") == value
        }.also { return it.size == 1 }
    }

}
