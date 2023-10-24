package maestro.cli.generative

import maestro.TreeNode
import maestro.cli.runner.gen.viewranking.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.viewranking.actionhash.TreeIndexer
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.TapOnElementCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ActionHasherTest {

    private val actionHasher = TreeDirectionHasher()

    @Test
    fun `hashing two back press over two screens produce different hash`() {
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
            MaestroCommand(InputRandomCommand(origin = MaestroCommand(BackPressCommand()) to TreeNode())),
            null
        )
        val hashB = actionHasher.hashAction(
            treeA,
            MaestroCommand(
                InputRandomCommand(
                    origin = MaestroCommand(
                        tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "Hola"))
                    ) to TreeNode()
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
    fun `hashing two input actions with different text over the same screen produce the same hash`(){
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
                    origin = MaestroCommand(
                        tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "Holaaa"))
                    ) to treeA.aggregate().first { it.attributes["type"] == "ClassC" }
                )
            ),
            null
        )
        val hashB = actionHasher.hashAction(
            treeB,
            MaestroCommand(
                InputRandomCommand(
                    origin = MaestroCommand(
                        tapOnElement = TapOnElementCommand(ElementSelector(textRegex = "Hola"))
                    ) to treeB.aggregate().first { it.attributes["type"] == "ClassC" }
                )
            ),
            null
        )
        Assertions.assertEquals(
            hashA,
            hashB
        )
    }


}
