package maestro.cli.generative

import maestro.TreeNode
import maestro.cli.runner.gen.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.actionhash.TreeIndexer
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ActionHasherTest {

    private val actionHasher = TreeDirectionHasher()

    /**
     * Contacts App has a design issue that overlaps two screens (the main)
     * under the search screen, so you can still detect main screen views when you are
     * on the search screen.
     */
    private val contactsMainScreenJson = "generative-test-resources/contacts_main_screen.json"

    private val contactsMainScreen = TreeNodeReader.read(contactsMainScreenJson)

    private val searchScreenWithFirstInputFile =
        "generative-test-resources/searchscreen_first_input.json"
    private val searchScreenWithSecondInputFile =
        "generative-test-resources/searchscreen_second_input.json"


    private val searchScreenWithFirstInput = TreeNodeReader.read(searchScreenWithFirstInputFile)
    private val searchScreenWithSecondInput = TreeNodeReader.read(searchScreenWithSecondInputFile)


    @Test
    fun `hashing back press over different screens produce different hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA"
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val treeB = TreeNode(
            attributes = mutableMapOf(
                "letter" to "E",
                "className" to "ClassE"
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "F",
                        "className" to "ClassF"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "G",
                        "className" to "ClassG"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(
            treeA,
            MaestroCommand(backPressCommand = BackPressCommand()),
            null
        )
        val hashB = actionHasher.hashAction(
            treeB,
            MaestroCommand(backPressCommand = BackPressCommand()),
            null
        )
        Assertions.assertNotEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing a back press over the same screen with different attributes, produces the same hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val treeB = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hola"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(
            treeA,
            MaestroCommand(backPressCommand = BackPressCommand()),
            null
        )
        val hashB = actionHasher.hashAction(
            treeB,
            MaestroCommand(backPressCommand = BackPressCommand()),
            null
        )
        Assertions.assertEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing two tap actions over the same screen, different view, produce different hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassC" })
        val hashB = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassB" })
        Assertions.assertNotEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing two tap actions over the same view on the same screen produce the same hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassC" })
        val hashB = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassC" })
        Assertions.assertEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing two tap actions similar over different screens produce a different hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val treeB = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassD",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassE",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassF",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassC" })
        val hashB = actionHasher.hashAction(treeB,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeB.aggregate()
                                                .first { it.attributes["type"] == "ClassF" })
        Assertions.assertNotEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing a tap action do not take into account attributes other than type`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val treeB = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hellooooo"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(treeA,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeA.aggregate()
                                                .first { it.attributes["type"] == "ClassB" })
        val hashB = actionHasher.hashAction(treeB,
                                            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector())),
                                            treeB.aggregate()
                                                .first { it.attributes["type"] == "ClassB" })
        Assertions.assertEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing a input action takes into account the action that opened the keyboard`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(
            treeA,
            MaestroCommand(InputRandomCommand(origin = "A")),
            null
        )
        val hashB = actionHasher.hashAction(
            treeA,
            MaestroCommand(
                InputRandomCommand(
                    origin = "B"
                )
            ),
            null
        )
        Assertions.assertNotEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing two input actions with different text over the same screen produce the same hash`() {
        val treeA = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "text" to "Holaaa",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val treeB = TreeNode(
            attributes = mutableMapOf(
                "letter" to "A",
                "className" to "ClassA",
            ),
            children = listOf(
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "B",
                        "className" to "ClassB",
                        "text" to "Hello"
                    ),
                ),
                TreeNode(
                    attributes = mutableMapOf(
                        "letter" to "C",
                        "className" to "ClassC",
                        "text" to "Hola",
                        "resource-id" to "R.id.exampleid"
                    ),
                ),
            )
        ).run { TreeIndexer.addTypeAndIndex(this) }
        val hashA = actionHasher.hashAction(
            treeA,
            MaestroCommand(
                InputRandomCommand(
                    origin = "A"
                )
            ),
            null
        )
        val hashB = actionHasher.hashAction(
            treeB,
            MaestroCommand(
                InputRandomCommand(
                    origin = "A"
                )
            ),
            null
        )
        Assertions.assertEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `hashing contacts main screen with all tap events produces a different hash for each node`() {
        val treeWithIndexes = TreeIndexer.addTypeAndIndex(contactsMainScreen)
        val flattenNodes = treeWithIndexes.aggregate()
        val tapCommand = MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector()))
        val hashes = flattenNodes.map {
            actionHasher.hashAction(
                treeWithIndexes,
                tapCommand,
                it
            )
        }
        val setOfHashes = hashes.toSet()
        Assertions.assertEquals(
            setOfHashes.size,
            hashes.size
        )
    }

    @Test
    fun `hashing contacts main screen with all back actions produces a unique hash`() {
        val treeWithIndexes = TreeIndexer.addTypeAndIndex(contactsMainScreen)
        val flattenNodes = treeWithIndexes.aggregate()
        val backCommand = MaestroCommand(backPressCommand = BackPressCommand())
        val hashes = flattenNodes.map {
            actionHasher.hashAction(
                treeWithIndexes,
                backCommand,
                it
            )
        }
        Assertions.assertEquals(
            hashes.toSet().size,
            1
        )
    }

    @Test
    fun `hashing two input actions on contacts main screen with different origins produce different hashes`() {
        val treeWithIndexes = TreeIndexer.addTypeAndIndex(contactsMainScreen)
        val random = Random(1024)
        val flattenNodes = treeWithIndexes.aggregate()
        val tapCommandA =
            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "ButtonA")))
        val tapCommandB =
            MaestroCommand(tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "ButtonB")))
        val inputCommandA = MaestroCommand(
            inputRandomTextCommand = InputRandomCommand(
                origin = "A"
            )
        )
        val inputCommandB = MaestroCommand(
            inputRandomTextCommand = InputRandomCommand(
                origin = "B"
            )
        )
        val hashA = actionHasher.hashAction(
            treeWithIndexes,
            inputCommandA,
            null
        )
        val hashB = actionHasher.hashAction(
            treeWithIndexes,
            inputCommandB,
            null
        )
        Assertions.assertNotEquals(
            hashA,
            hashB
        )
    }

    @Test
    fun `after input twice in the same textfield, if nothing but the field changes, is idempotent`() {
        val searchScreenFirstInput = TreeIndexer.addTypeAndIndex(this.searchScreenWithFirstInput)
        val searchScreenSecondInput = TreeIndexer.addTypeAndIndex(this.searchScreenWithSecondInput)
        val actionThatOpenedTheField = MaestroCommand(
            tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "open"))
        )
        val inputRandomCommand =
            InputRandomCommand(origin = "")
        val firstInput = MaestroCommand(inputRandomTextCommand = inputRandomCommand)
        val secondInput = MaestroCommand(inputRandomTextCommand = inputRandomCommand)

        val firstHash = actionHasher.hashAction(
            searchScreenFirstInput,
            firstInput,
            null
        )
        val secondHash = actionHasher.hashAction(
            searchScreenSecondInput,
            secondInput,
            null
        )
        Assertions.assertEquals(
            firstHash,
            secondHash
        )
    }


}
