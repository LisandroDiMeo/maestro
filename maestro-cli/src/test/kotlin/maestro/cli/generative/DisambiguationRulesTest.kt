package maestro.cli.generative

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.viewdisambiguator.HasUniqueAccessibilityText
import maestro.cli.runner.gen.viewdisambiguator.HasUniqueId
import maestro.cli.runner.gen.viewdisambiguator.HasUniqueIdAndText
import maestro.cli.runner.gen.viewdisambiguator.HasUniqueText
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import maestro.orchestra.ElementSelector
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DisambiguationRulesTest {

    private val screen1 = "generative-test-resources/screen1.json"

    private val contactsMainScreen = TreeNodeReader.read(screen1)

    private val uniqueIdRule = HasUniqueId()
    private val uniqueTextRule = HasUniqueText()
    private val uniqueAccessibilityTextRule = HasUniqueAccessibilityText()
    private val uniqueIdAndText = HasUniqueIdAndText()
    private val allRules = SequentialDisambiguation(
        listOf(
            uniqueIdRule,
            uniqueTextRule,
            uniqueAccessibilityTextRule,
            uniqueIdAndText
        )
    )

    @Test
    fun `resource id disambiguator disambiguate all elements with unique id`() {
        val hierarchy = ViewHierarchy(root = contactsMainScreen)
        val flattenNodes = hierarchy.aggregate()
        val uniqueById = flattenNodes.filter { thisNode ->
            val matches = flattenNodes.filter { otherNode ->
                otherNode.attributes["resource-id"] == thisNode.attributes["resource-id"]
            }
            matches.size == 1
        }

        val availableWidgets = flattenNodes.map {
            it to uniqueIdRule.disambiguate(
                contactsMainScreen,
                it,
                flattenNodes
            )
        }
        val properlyDisambiguated =
            availableWidgets.filter { it.second != ElementSelector() }.map { it.first }
        Assertions.assertEquals(
            properlyDisambiguated.toSet(),
            uniqueById.toSet()
        )
        Assertions.assertNotEquals(
            properlyDisambiguated.toSet(),
            flattenNodes.toSet()
        )
    }

    @Test
    fun `text disambiguator disambiguate all elements with unique text`() {
        val hierarchy = ViewHierarchy(root = contactsMainScreen)
        val flattenNodes = hierarchy.aggregate()
        val uniqueByText = flattenNodes.filter { thisNode ->
            val matches = flattenNodes.filter { otherNode ->
                otherNode.attributes["text"] == thisNode.attributes["text"]
            }
            matches.size == 1
        }

        val availableWidgets = flattenNodes.map {
            it to uniqueTextRule.disambiguate(
                contactsMainScreen,
                it,
                flattenNodes
            )
        }
        val properlyDisambiguated =
            availableWidgets.filter { it.second != ElementSelector() }.map { it.first }
        Assertions.assertEquals(
            properlyDisambiguated.toSet(),
            uniqueByText.toSet()
        )
        Assertions.assertNotEquals(
            properlyDisambiguated.toSet(),
            flattenNodes.toSet()
        )
    }

    @Test
    fun `text disambiguator disambiguate all elements with unique accessibility text`() {
        val hierarchy = ViewHierarchy(root = contactsMainScreen)
        val flattenNodes = hierarchy.aggregate()
        val uniqueByText = flattenNodes.filter { thisNode ->
            val matches = flattenNodes.filter { otherNode ->
                otherNode.attributes["accessibilityText"] == thisNode.attributes["accessibilityText"]
            }
            matches.size == 1
        }

        val availableWidgets = flattenNodes.map {
            it to uniqueAccessibilityTextRule.disambiguate(
                contactsMainScreen,
                it,
                flattenNodes
            )
        }
        val properlyDisambiguated =
            availableWidgets.filter { it.second != ElementSelector() }.map { it.first }
        Assertions.assertEquals(
            properlyDisambiguated.toSet(),
            uniqueByText.toSet()
        )
        Assertions.assertNotEquals(
            properlyDisambiguated.toSet(),
            flattenNodes.toSet()
        )
    }

    @Test
    fun `has unique id and text rule can disambiguate by checking both uniqueness`() {
        val hierarchy = ViewHierarchy(
            root = TreeNode(
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "R",
                            "text" to "A"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "R",
                            "text" to "T"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "C",
                            "text" to "T"
                        )
                    )
                )
            )
        )
        val flattenNodes = hierarchy.aggregate()
        // We pick three nodes (a,b,c)
        // a,b will share the same resource id R
        // b,c will share the same text T
        // R nor T are unique, so b cannot be disambiguated
        val selectors =
            flattenNodes.filter { it.attributes.isNotEmpty() }.map { node ->
                uniqueIdAndText.disambiguate(
                    hierarchy.root,
                    node,
                    flattenNodes
                )
            }
        selectors.forEach {
            Assertions.assertNotEquals(
                it,
                ElementSelector()
            )
        }
    }

    @Test
    fun `sequential disambiguation can disambiguate by the easiest condition`() {
        val hierarchy = ViewHierarchy(
            root = TreeNode(
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "R",
                            "text" to "A"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "R",
                            "text" to "T"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "C",
                            "text" to "T"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "resource-id" to "X"
                        )
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "UNIQUE TEXt"
                        )
                    )
                )
            )
        )
        val flattenNodes = hierarchy.aggregate()
        // We pick three nodes (a,b,c)
        // a,b will share the same resource id R
        // b,c will share the same text T
        // R nor T are unique, so b cannot be disambiguated
        val selectors =
            flattenNodes.filter { it.attributes.isNotEmpty() }.map { node ->
                allRules.disambiguate(
                    hierarchy.root,
                    node,
                    flattenNodes
                )
            }
        selectors.forEach {
            Assertions.assertNotEquals(
                it,
                ElementSelector()
            )
        }
    }


    //
    //    @Test
    //    fun `when leafs are indistinguishable, its not possible to match them`() {
    //        val hierarchy = ViewHierarchy(root = contactsMainScreen)
    //        val flattenNodes = hierarchy.aggregate()
    //        val selectors = flattenNodes.map { node ->
    //            uniqueIdRule.disambiguate(
    //                hierarchy.root,
    //                node,
    //                flattenNodes,
    //                { ElementSelector(idRegex = node.attributes["resource-id"]) },
    //                {
    //                    uniqueTextRule.disambiguate(
    //                        hierarchy.root,
    //                        node,
    //                        flattenNodes,
    //                        {
    //                            ElementSelector(textRegex = it.attributes["text"])
    //                        },
    //                        {
    //                            idNotUniqueFallback.disambiguate(
    //                                hierarchy.root,
    //                                it,
    //                                flattenNodes,
    //                                {
    //                                    ElementSelector(
    //                                        textRegex = node.attributes["text"],
    //                                        idRegex = node.attributes["resource-id"]
    //                                    )
    //                                },
    //                                { ElementSelector() }
    //                            )
    //                        }
    //                    )
    //                }) to node
    //        }
    //        val id = "com.google.android.contacts:id/navigation_bar_item_icon_view"
    //        val impossibleToDisambiguate =
    //            flattenNodes.filter { it.attributes["resource-id"] == id }.toSet()
    //        val nonDisambiguated = selectors.filter { (selector, _) -> ElementSelector() == selector }
    //            .map { it.second }.toSet()
    //        Assertions.assertTrue(nonDisambiguated.containsAll(impossibleToDisambiguate))
    //    }


}
