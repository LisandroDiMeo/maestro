package maestro.cli.runner.gen.commandselection

import maestro.TreeNode
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand

interface CommandSelectionStrategy {
    @kotlin.jvm.Throws(UnableToPickCommand::class)
    fun pickFrom(root: TreeNode): MaestroCommand

    private object UnableToPickCommand : Exception()
}

class AndroidCommandSelection : CommandSelectionStrategy {
    override fun pickFrom(root: TreeNode): MaestroCommand {
        val flattenNodes = root.aggregate()
        val elementDisambiguator = SimpleAndroidViewDisambiguator(root)
        return flattenNodes.filter { treeNode ->
            treeNode.clickable == true
        }.map {
            TapOnElementCommand(selector = elementDisambiguator.disambiguate(root, it))
        }.map {
            MaestroCommand(tapOnElement = it)
        }.random()
    }

    private fun disambiguateNode(treeNode: TreeNode, root: TreeNode): ElementSelector {
        val textRegex = treeNode.attributes["text"]
        val resourceId = treeNode.attributes["resource-id"]
        val directAncestor = root.aggregate().map {
            val x = it.children.filter { child -> child == treeNode }
            x
        }
        val flatDirectAncestors = directAncestor.flatten()
        val ancestor = flatDirectAncestors.randomOrNull()
        return ElementSelector(
            textRegex = textRegex,
            idRegex = resourceId,
            focused = treeNode.focused,
            enabled = treeNode.enabled,
            checked = treeNode.checked,
            selected = treeNode.selected,
            below = ElementSelector(
                textRegex = (ancestor?.attributes?.get("text") ?: ""),
                idRegex = (ancestor?.attributes?.get("resource-id") ?: ""),
                focused = ancestor?.focused,
                enabled = ancestor?.enabled,
                checked = ancestor?.checked,
                selected = ancestor?.selected
            )

        )
    }
}

interface ViewDisambiguator {
    @Throws(UnableToDisambiguateView::class)
    fun disambiguate(root: TreeNode, view: TreeNode): ElementSelector

    private object UnableToDisambiguateView : Exception()
}

class SimpleAndroidViewDisambiguator(private val root: TreeNode) : ViewDisambiguator {

    private val flattenTree: List<TreeNode> = root.aggregate()

    override fun disambiguate(root: TreeNode, view: TreeNode): ElementSelector {
        // First, we disambiguate with some trivial checks
        var elementSelector: ElementSelector? = null
        var idRegex = ""
        var textRegex = ""
        var belowSelector: ElementSelector? = null
        view.attributes["resource-id"]?.let { resourceId ->
            if (idIsUnique(resourceId))
                elementSelector = ElementSelector(idRegex = resourceId)
            idRegex = resourceId
        }
        view.attributes["text"]?.let { text ->
            if (hasUniqueTextContent(text))
                elementSelector = ElementSelector(textRegex = text)
            textRegex = text
        }
//        if (view != root) {
//            directAncestor(view)?.let {
//                belowSelector = disambiguate(root, it)
//            }
//        }
        return if (elementSelector == null) ElementSelector(idRegex = idRegex, textRegex = textRegex) else
            elementSelector!!
    }

    private fun directAncestor(view: TreeNode): TreeNode? {
        return flattenTree.filter { view in it.children }.firstOrNull()
    }

    private fun idIsUnique(id: String): Boolean {
        flattenTree.filter {
            val otherId = it.attributes["resource-id"]
            (otherId ?: "") == id
        }.also { return it.size == 1 }
    }

    private fun hasUniqueTextContent(text: String): Boolean {
        flattenTree.filter {
            (it.attributes["text"] ?: "") == text
        }.also { return it.size == 1 }
    }
}