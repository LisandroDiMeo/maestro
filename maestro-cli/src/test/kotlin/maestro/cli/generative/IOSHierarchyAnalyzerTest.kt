package maestro.cli.generative

import hierarchy.AXElement.Companion.ELEMENT_TYPES
import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.hierarchyanalyzer.IOSHierarchyAnalyzer
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class IOSHierarchyAnalyzerTest {
    private val testScreenFile = "generative-test-resources/ios_analyzer_test_screen.json"
    private val testScreenNoKeyboardFile = "generative-test-resources/ios_contacts.json"
    private val testScreen = TreeNodeReader.read(testScreenFile)
    private val testScreenNoKeyboard = TreeNodeReader.read(testScreenNoKeyboardFile)

    @Test
    fun `given an ios screen, analyzer removes the ignored nodes`() {
        /**
         * Ignored nodes are:
         * - SBSwitcherWindow (TopBar)
         * - SystemInputAssistantView (Input suggestions)
         * - Key Type elements (to avoid detecting keyboard keys as buttons)
         */
        val flattenTree = testScreen.aggregate()
        val keyElementType = ELEMENT_TYPES.keys.first { ELEMENT_TYPES[it] == "key" }
        Assertions.assertTrue(keyElementType in flattenTree.map { it.attributes["elementType"] })
        val assistantViewSubTree = testScreen
            .aggregate()
            .firstOrNull { it.attributes["resource-id"] == "SystemInputAssistantView" }
            ?.aggregate()
        Assertions.assertTrue(assistantViewSubTree != null)
        assistantViewSubTree?.isNotEmpty()?.let { Assertions.assertTrue(it) }
        val topBarSubTree = testScreen
            .aggregate()
            .firstOrNull { it.attributes["resource-id"] == "SBSwitcherWindow:Main" }
            ?.aggregate()
        Assertions.assertTrue(topBarSubTree != null)
        topBarSubTree?.isNotEmpty()?.let { Assertions.assertTrue(it) }

        val analyzer = mockIosHierarchyAnalyzer()
        val testScreenWithoutIgnoredNodes = analyzer.removeIgnoredNodes(testScreen.aggregate())

        assistantViewSubTree?.forEach {
            Assertions.assertFalse(it in testScreenWithoutIgnoredNodes)
        }
        topBarSubTree?.forEach {
            Assertions.assertFalse(it in testScreenWithoutIgnoredNodes)
        }
        testScreenWithoutIgnoredNodes.forEach {
            Assertions.assertFalse(it.attributes["elementType"] == keyElementType)
        }
    }

    @Test
    fun `analyzer detects when keyboard is open`() {
        Assertions.assertTrue(
            mockIosHierarchyAnalyzer().isKeyboardOpen(testScreen.aggregate())
        )
        Assertions.assertFalse(
            mockIosHierarchyAnalyzer().isKeyboardOpen(testScreenNoKeyboard.aggregate())
        )
    }

    @Test
    fun `an ios screen is always scrollable`() {
        Assertions.assertTrue(
            mockIosHierarchyAnalyzer().isScrollable(testScreen.aggregate())
        )
    }

    @Test
    fun `analyzer can detects when its outside app if none node has AUT package name`() {
        val analyzer = mockIosHierarchyAnalyzer()
        val isInApp = !analyzer.isOutsideApp(
            ViewHierarchy(testScreen),
            "com.apple.reminders"
        )
        Assertions.assertTrue(isInApp)
        val isOutsideApp = analyzer.isOutsideApp(
            ViewHierarchy(testScreenNoKeyboard),
            "com.apple.reminders"
        )
        Assertions.assertTrue(isOutsideApp)
    }



    private fun mockIosHierarchyAnalyzer() = IOSHierarchyAnalyzer(
        object : CommandSelectionStrategy(
            actionHasher = object : ActionHasher {
                override fun hashAction(
                    root: TreeNode,
                    action: MaestroCommand,
                    view: TreeNode?
                ): String {
                    TODO("Not yet implemented")
                }
            },
            ExecutedCommandsObservable()
        ) {
            override fun pickFrom(
                availableCommands: List<Pair<MaestroCommand, TreeNode?>>,
                root: TreeNode,
                newTest: Boolean,
                wasLastActionForTest: Boolean
            ): MaestroCommand {
                TODO("Not yet implemented")
            }

            override fun usagesForAction(actionHash: String): Int {
                TODO("Not yet implemented")
            }

            override fun updateUsagesForActionToExecute(actionToPerform: String) {
                TODO("Not yet implemented")
            }
        },
        object : DisambiguationRule {
            override fun disambiguate(
                root: TreeNode,
                view: TreeNode,
                flattenNodes: List<TreeNode>
            ): ElementSelector {
                TODO("Not yet implemented")
            }
        }
    )


}
