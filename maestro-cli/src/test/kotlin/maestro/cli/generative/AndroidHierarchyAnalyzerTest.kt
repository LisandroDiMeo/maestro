package maestro.cli.generative

import maestro.TreeNode
import maestro.ViewHierarchy
import maestro.cli.runner.gen.actionhash.ActionHasher
import maestro.cli.runner.gen.commandselection.strategies.CommandSelectionStrategy
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer.Companion.ANDROID_IGNORED_RESOURCES
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.cli.runner.gen.viewdisambiguator.DisambiguationRule
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AndroidHierarchyAnalyzerTest {
    private val testScreenFile = "generative-test-resources/contacts_main_screen.json"
    private val testScreen = TreeNodeReader.read(testScreenFile)
    private val testScreenNoScrollableFile = "generative-test-resources/android_screen_no_scrollable.json"
    private val testScreenNoScrollable = TreeNodeReader.read(testScreenNoScrollableFile)
    private val testScreenWithKeyboardFile = "generative-test-resources/searchscreen_first_input.json"
    private val testScreenWithKeyboard = TreeNodeReader.read(testScreenWithKeyboardFile)

    @Test
    fun `given an android screen, analyzer removes the ignored nodes`() {
        /**
         * Ignored nodes are:
         * - System UI nodes
         * - Input method nodes (keys)
         */
        val flattenTree = testScreen.aggregate()
        val expectedIgnoredNodes = flattenTree.filter {
            val resourceAndPackage = "${it.attributes["resource-id"]}-${it.attributes["packageName"]}"
            ANDROID_IGNORED_RESOURCES.any { res -> resourceAndPackage.contains(res) }
        }
        val analyzer = mockAndroidHierarchyAnalyzer()
        val cleanedNodes = analyzer.removeIgnoredNodes(flattenTree)
        Assertions.assertTrue(expectedIgnoredNodes.none { it in cleanedNodes })
    }

    @Test
    fun `analyzer detects when keyboard is open`() {
        Assertions.assertFalse(
            mockAndroidHierarchyAnalyzer().isKeyboardOpen(testScreen.aggregate())
        )
        Assertions.assertTrue(
            mockAndroidHierarchyAnalyzer().isKeyboardOpen(testScreenWithKeyboard.aggregate())
        )
    }

    @Test
    fun `android screen is scrollable if at least there is a node with scroll view class name`() {
        Assertions.assertTrue(
            mockAndroidHierarchyAnalyzer().isScrollable(testScreen.aggregate())
        )
        Assertions.assertFalse(
            mockAndroidHierarchyAnalyzer().isScrollable(testScreenNoScrollable.aggregate())
        )
    }

    @Test
    fun `analyzer detects when its outside app if none node has AUT package name`() {
        val analyzer = mockAndroidHierarchyAnalyzer()
        val isInApp = !analyzer.isOutsideApp(
            ViewHierarchy(testScreen),
            "com.google.android.contacts"
        )
        Assertions.assertTrue(isInApp)
        val isOutsideApp = analyzer.isOutsideApp(
            ViewHierarchy(testScreenNoScrollable),
            "com.google.android.contacts"
        )
        Assertions.assertTrue(isOutsideApp)
    }

    @Test
    fun `back press command is not null for android analyzer`(){
        Assertions.assertTrue(mockAndroidHierarchyAnalyzer().backPressCommand() != null)
    }

    private fun mockAndroidHierarchyAnalyzer() = AndroidHierarchyAnalyzer(
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
