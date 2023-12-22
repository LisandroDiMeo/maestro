package maestro.cli.runner.gen.viewdisambiguator

import maestro.TreeNode
import maestro.orchestra.ElementSelector

class SequentialDisambiguation(
    private val rules: List<DisambiguationRule>
) : DisambiguationRule {
    override fun disambiguate(
        root: TreeNode,
        view: TreeNode,
        flattenNodes: List<TreeNode>
    ): ElementSelector {
        for (rule in rules) {
            val ruleResult = rule.disambiguate(
                root,
                view,
                flattenNodes
            )
            if (ruleResult != ElementSelector()) return ruleResult
        }
        return ElementSelector()
    }

    companion object {
        fun sequentialRuleForIdTextAccTextAndAllTogether(
            fallbackToFirstMatch: Boolean = false
        ) = SequentialDisambiguation(
            listOf(
                HasUniqueId(),
                HasUniqueText(),
                HasUniqueAccessibilityText(),
                HasUniqueIdAndText()
            ) + if (fallbackToFirstMatch) listOf(pickFirstForAttribute) else emptyList()
        )

        private val pickFirstForAttribute: DisambiguationRule = object : DisambiguationRule {
            override fun disambiguate(
                root: TreeNode,
                view: TreeNode,
                flattenNodes: List<TreeNode>
            ): ElementSelector {
                val text = view.attributes["text"]
                val accessibilityText = view.attributes["accessibilityText"]
                val resId = view.attributes["resource-id"]
                val textRegex = if (text != null && text.trim().isNotEmpty()) text
                else accessibilityText
                return ElementSelector(
                    textRegex = textRegex,
                    idRegex = resId
                )
            }
        }
    }
}
