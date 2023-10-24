package maestro.cli.generative

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.ViewRanking
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ViewRankingTest {

    private lateinit var viewRanking: ViewRanking

    @BeforeEach
    fun setup() {
        viewRanking = ViewRanking()
    }

    @Test
    fun `view ranking starts with an empty model`() {
        Assertions.assertTrue(viewRanking.isEmpty())
    }

    @Test
    fun `view ranking has a non empty model after first pick from actions`() {
        val root = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Press Me")))
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to root,
            MaestroCommand(
                ScrollCommand()
            ) to root,
            MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to root.children[0],
        )

        viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false
        )
        Assertions.assertFalse(viewRanking.isEmpty())
    }

    @Test
    fun `view ranking selects an action at pick from the ones given`() {
        val root = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Press Me")))
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to root,
            MaestroCommand(
                ScrollCommand()
            ) to root,
            MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to root.children[0],
        )

        val action = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false
        )
        Assertions.assertTrue(action in commandsFetchedForTheFirstTime.map { it.first })
    }

    @Test
    fun `view ranking selects an action at pick prioritizing the tap actions`() {
        val root = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Press Me")))
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to root,
            MaestroCommand(
                ScrollCommand()
            ) to root,
            MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to root.children[0],
        )

        val action = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false
        )
        Assertions.assertEquals(action.tapOnElement, TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me")))
    }

    @Test
    fun `view ranking will not choose the same action twice consecutively`() {
        val root = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Press Me")))
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to root,
            MaestroCommand(
                ScrollCommand()
            ) to root,
            MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to root.children[0],
        )

        val action1 = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false
        )
        val action2 = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false
        )
        Assertions.assertNotEquals(action1, action2)

    }

    @Test
    fun `x`() {

    }
}
