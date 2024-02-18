package maestro.cli.generative

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.cli.runner.gen.hierarchyanalyzer.HierarchyAnalyzer
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import maestro.orchestra.ElementSelector
import maestro.orchestra.EraseTextCommand
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AbstractHierarchyAnalyzerTest {

    private lateinit var hierarchyAnalyzer: HierarchyAnalyzer
    private var scrollable = false
    private var keyboardOpen = false
    private var isOutsideApp = false
    private var ignoredNodes = emptyList<TreeNode>()

    fun toggleScrollable() {
        scrollable = !scrollable
    }

    fun toggleKeyboard() {
        keyboardOpen = !keyboardOpen
    }

    fun toggleIsOutsideApp() {
        isOutsideApp = !isOutsideApp
    }

    fun setIgnoredNodes(nodesToIgnore: List<TreeNode>) {
        ignoredNodes = nodesToIgnore
    }

    @BeforeEach
    fun setUpBeforeEachCase() {
        /**
         * We'll use view ranking as we know the outcome for [ViewRanking.pickFrom]
         */
        hierarchyAnalyzer = object : HierarchyAnalyzer(
            disambiguationRule = SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether(),
            selectionStrategy = ViewRanking()
        ) {
            override fun isScrollable(nodes: List<TreeNode>): Boolean {
                return scrollable
            }

            override fun isKeyboardOpen(nodes: List<TreeNode>): Boolean {
                return keyboardOpen
            }

            override fun isOutsideApp(
                hierarchy: ViewHierarchy,
                packageName: String
            ): Boolean {
                return isOutsideApp
            }

            override fun removeIgnoredNodes(flattenNodes: List<TreeNode>): List<TreeNode> {
                return flattenNodes.filter { it !in ignoredNodes }
            }

        }
    }

    @Test
    fun `given a controlled strategy and default disambiguation rule, analyzer fetch expected command`() {
        val fakeHierarchy = ViewHierarchy(
            root = TreeNode(
                attributes = mutableMapOf(
                    "text" to "Hello World!"
                ),
                clickable = false,
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "A"
                        ),
                        clickable = true,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "B"
                        ),
                        clickable = false,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "B"
                        ),
                        clickable = true,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "C"
                        ),
                        clickable = true,
                    )
                )
            )
        )
        val expectedCommand = MaestroCommand(
            tapOnElement = TapOnElementCommand(
                selector = ElementSelector(textRegex = "A")
            )
        )
        val command = hierarchyAnalyzer.fetchCommandFrom(
            hierarchy = fakeHierarchy,
            newTest = true,
            wasLastActionForTest = false
        )
        Assertions.assertEquals(expectedCommand, command)
    }

    @Test
    fun `analyzer extracts widgets by removing ignored nodes, and then keeps only those who were disambiguated`(){
        val fakeHierarchy = ViewHierarchy(
            root = TreeNode(
                attributes = mutableMapOf(
                    "text" to "Hello World!"
                ),
                clickable = false,
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "A"
                        ),
                        clickable = true,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "B"
                        ),
                        clickable = false,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "B"
                        ),
                        clickable = true,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "C"
                        ),
                        clickable = true,
                    )
                )
            )
        )
        val ignoredNodes = listOf(
            TreeNode(
                attributes = mutableMapOf(
                    "text" to "C"
                ),
                clickable = true,
            )
        )
        val expectedToExtract = listOf(
            TreeNode(
                attributes = mutableMapOf(
                    "text" to "A"
                ),
                clickable = true,
            ) to ElementSelector(textRegex = "A"),
            fakeHierarchy.root to ElementSelector(textRegex = "Hello World!")
        )
        setIgnoredNodes(ignoredNodes)
        val extracted = hierarchyAnalyzer.extractWidgets(
            fakeHierarchy,
            fakeHierarchy.aggregate()
        )
        Assertions.assertEquals(expectedToExtract.toSet(), extracted.toSet())
    }

    @Test
    fun `analyzer extracts clickable actions by checking if node for action is clickable`() {
        val fakeHierarchy = ViewHierarchy(
            root = TreeNode(
                attributes = mutableMapOf(
                    "text" to "Hello World!"
                ),
                clickable = false,
                children = listOf(
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "A"
                        ),
                        clickable = true,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "B"
                        ),
                        clickable = false,
                    ),
                    TreeNode(
                        attributes = mutableMapOf(
                            "text" to "C"
                        ),
                        clickable = true,
                    )
                )
            )
        )
        val widgets = hierarchyAnalyzer.extractWidgets(fakeHierarchy, fakeHierarchy.aggregate())
        val clickableActions = hierarchyAnalyzer.extractClickableActions(widgets)
        Assertions.assertEquals(2, clickableActions.size)
        Assertions.assertTrue(
            TreeNode(
                attributes = mutableMapOf(
                    "text" to "C"
                ),
                clickable = true,
            ) in clickableActions.map { it.second }
        )
        Assertions.assertTrue(
            TreeNode(
                attributes = mutableMapOf(
                    "text" to "A"
                ),
                clickable = true,
            ) in clickableActions.map { it.second }
        )
    }

    @Test
    fun `default behavior for back press is expected to be null`() {
        Assertions.assertTrue(hierarchyAnalyzer.backPressCommand() == null)
    }

    @Test
    fun `keyboardOpenCommandsIfOpen returns keyboard commands if open, empty list otherwise`() {
        val fakeHierarchy = ViewHierarchy(
            root = TreeNode(
                attributes = mutableMapOf(
                    "text" to "Hello World!"
                )
            )
        )
        val noKeyboardCommands = hierarchyAnalyzer.keyboardOpenCommandsIfOpen(fakeHierarchy.aggregate())
        Assertions.assertTrue(noKeyboardCommands.isEmpty())
        toggleKeyboard()
        val keyboardCommands = hierarchyAnalyzer.keyboardOpenCommandsIfOpen(fakeHierarchy.aggregate())
        Assertions.assertEquals(3, keyboardCommands.size)
        Assertions.assertTrue(keyboardCommands.any { it is InputRandomCommand })
        Assertions.assertTrue(keyboardCommands.any { it is HideKeyboardCommand })
        Assertions.assertTrue(keyboardCommands.any { it is EraseTextCommand })
    }

    @Test
    fun `scrollCommandIfScrollable return scroll command if scrollable, null otherwise`() {
        val fakeHierarchy = ViewHierarchy(
            root = TreeNode(
                attributes = mutableMapOf(
                    "text" to "Hello World!"
                )
            )
        )
        Assertions.assertTrue(!hierarchyAnalyzer.isScrollable(fakeHierarchy.aggregate()))
        toggleScrollable()
        Assertions.assertTrue(hierarchyAnalyzer.isScrollable(fakeHierarchy.aggregate()))
    }


}
