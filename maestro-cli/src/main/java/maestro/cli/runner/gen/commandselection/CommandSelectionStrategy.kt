package maestro.cli.runner.gen.commandselection

import maestro.TreeNode
import maestro.drivers.AndroidDriver
import maestro.orchestra.Command
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import kotlin.reflect.KClass

interface CommandSelectionStrategy {
    @kotlin.jvm.Throws(UnableToPickCommand::class)
    fun pickFrom(root: TreeNode): MaestroCommand

    fun commandsFor(node: TreeNode, selector: ElementSelector): List<MaestroCommand>

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

    override fun commandsFor(node: TreeNode, selector: ElementSelector): List<MaestroCommand> {
        val commands = mutableListOf<Command>()
        if(node.clickable == true) commands.add(TapOnElementCommand(selector))
        return commands.map { MaestroCommand(it) }
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
        val idRegex = view.attributes["resource-id"]
        idRegex?.let { if (idIsUnique(it)) return ElementSelector(idRegex = it) }
        val textRegex = view.attributes["text"]
        textRegex?.let {
            if (hasUniqueTextContent(it)) return ElementSelector(
                textRegex = it,
                idRegex = idRegex
            )
        }
        val belowSelector: ElementSelector? = if (view == root) null else disambiguate(
            root,
            directAncestor(view)!!
        )
        belowSelector?.let {
            return ElementSelector(
                idRegex = idRegex,
                textRegex = textRegex,
                below = belowSelector,
            )
        }
        return ElementSelector() // No selector
    }

    private fun directAncestor(view: TreeNode): TreeNode? {
        return flattenTree.firstOrNull { view in it.children }
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
