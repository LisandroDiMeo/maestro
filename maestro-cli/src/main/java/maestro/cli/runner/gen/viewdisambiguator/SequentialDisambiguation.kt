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
        for(rule in rules) {
            val ruleResult = rule.disambiguate(root, view, flattenNodes)
            if (ruleResult != ElementSelector()) return ruleResult
        }
        return ElementSelector()
    }

    companion object {
        fun sequentialRuleForIdTextAccTextAndAllTogether() = SequentialDisambiguation(
            listOf(
                HasUniqueId(),
                HasUniqueText(),
                HasUniqueAccessibilityText(),
                HasUniqueIdAndText(),
            )
        )
    }
}
