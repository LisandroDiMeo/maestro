package maestro.cli.generative

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.TapOnElementCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ViewRankingTest {

    private lateinit var viewRanking: ViewRanking

    private val screen1 = "generative-test-resources/contacts_main_screen.json"
    private val screen2 = "generative-test-resources/contacts_main_screen_with_phone_filter.json"
    private val screen3 =
        "generative-test-resources/contacts_main_screen_with_phone_and_email_filter.json"
    private val contactsMainScreen = TreeNodeReader.read(screen1)
    private val contactsMainScreenWithPhoneFilter = TreeNodeReader.read(screen2)
    private val contactsMainScreenWithPhoneAndEmailFilter = TreeNodeReader.read(screen3)

    @BeforeEach
    fun setup() {
        viewRanking = ViewRanking()
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
            false,
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
            false,
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
            false,
            false
        )
        Assertions.assertEquals(
            action.tapOnElement,
            TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
        )
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
            false,
            false
        )
        val action2 = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        Assertions.assertNotEquals(
            action1,
            action2
        )

    }

    @Test
    fun `view ranking prioritizes input text if no tap action exists`() {
        val root = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Type text")))
        )
        val inputRandomTextCommand = InputRandomCommand(
            origin = MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to root.children[0]
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to root,
            MaestroCommand(
                ScrollCommand()
            ) to root,
            MaestroCommand(
                inputRandomTextCommand = inputRandomTextCommand
            ) to root
        )

        val action = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        Assertions.assertEquals(
            action.inputRandomTextCommand,
            inputRandomTextCommand
        )
    }

    @Test
    fun `view ranking prioritizes unused actions`() {
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
            false,
            false
        )
        val action2 = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        val action3 = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        Assertions.assertEquals(
            action1.tapOnElement,
            TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
        )
        Assertions.assertEquals(
            action2.backPressCommand,
            BackPressCommand()
        )
        Assertions.assertEquals(
            action3.scrollCommand,
            ScrollCommand()
        )
    }

    @Test
    fun `view ranking properly adds edges between actions`() {
        val rootOrigin = TreeNode(
            children = listOf(TreeNode(attributes = mutableMapOf("text" to "Press Me")))
        )
        val commandsFetchedForTheFirstTime = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to rootOrigin,
            MaestroCommand(
                ScrollCommand()
            ) to rootOrigin,
            MaestroCommand(
                tapOnElement = TapOnElementCommand(selector = ElementSelector(textRegex = "Press Me"))
            ) to rootOrigin.children[0],
        )
        val rootDestination = TreeNode(
            children = listOf(TreeNode(mutableMapOf("text" to "Hello!")))
        )
        val commandsForNewScreen = listOf(
            MaestroCommand(
                BackPressCommand()
            ) to rootOrigin,
            MaestroCommand(
                ScrollCommand()
            ) to rootOrigin
        )
        val a = viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            rootOrigin,
            false,
            false
        )
        val b = viewRanking.pickFrom(
            commandsForNewScreen,
            rootDestination,
            false,
            false
        )
        Assertions.assertTrue(a in commandsFetchedForTheFirstTime.map { it.first })
        Assertions.assertTrue(b in commandsForNewScreen.map { it.first })
        val (_, node) = commandsFetchedForTheFirstTime[2]
        val actionHasher = TreeDirectionHasher()
        val hashesDestination = commandsForNewScreen.map { (a, b) ->
            actionHasher.hashAction(
                rootDestination,
                a,
                b
            )
        }
        val edges = viewRanking.edgesFor(
            a,
            node,
            rootOrigin
        )
        Assertions.assertEquals(
            edges?.first,
            hashesDestination
        )

    }

    @Test
    fun `view ranking not updates the model when the test is new and model is not empty`() {
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
            true,
            false
        )
        viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            false,
            false
        )
        // New test start here
        viewRanking.pickFrom(
            commandsFetchedForTheFirstTime,
            root,
            true,
            false
        )

        val edges = viewRanking.edgesFor(
            commandsFetchedForTheFirstTime[1].first,
            commandsFetchedForTheFirstTime[1].second,
            root
        )
        Assertions.assertEquals(
            edges?.first,
            emptyList<String>()
        )
    }

}
