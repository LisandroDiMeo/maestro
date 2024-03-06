package maestro.cli.runner

import maestro.Maestro
import maestro.ViewHierarchy
import maestro.cli.runner.gen.TestSuiteGeneratorLogger
import maestro.cli.runner.gen.actionhash.TreeDirectionHasher
import maestro.cli.runner.gen.actionhash.TreeIndexer
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.hierarchyanalyzer.HierarchyAnalyzer
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import maestro.orchestra.MaestroCommand
import kotlin.random.Random

class HasherTester(
    private val maestro: Maestro,
) {

    private val hasher = TreeDirectionHasher()
    private val mapOfActions = mutableMapOf<String, List<MaestroCommand>>()
    private val logger = TestSuiteGeneratorLogger.logger
    private lateinit var hierarchyAnalyzer: HierarchyAnalyzer

    private fun commandsForCurrentHierarchy(newTest: Boolean): List<Pair<String, MaestroCommand>> {
        val hierarchy = maestro.viewHierarchy(true)
        val hierarchyWithIndexes = hierarchy.run { ViewHierarchy(root = TreeIndexer.addTypeAndIndex(this.root)) }
        val commandsForCurrentHierarchy = hierarchyAnalyzer.commandsForHierarchy(
            hierarchyWithIndexes
        )
        return commandsForCurrentHierarchy.map { (command, node) ->
            hasher.hashAction(
                hierarchyWithIndexes.root,
                command,
                node
            ) to command
        }
    }

    fun startDebugSession() {
        logger.info("Starting debugging session for hashes...")
        logger.info("Defaulting analyzer for Android, we need to add iOS too")
        hierarchyAnalyzer = AndroidHierarchyAnalyzer(
            ViewRanking(
                random = Random(1234)
            ),
            SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether()
        )
        logger.info("Write 'close' and press enter to end the session.\nWrite nt to mark as new test")
        var line = ""
        var newTest = true
        while(line != "close"){
            val hashedActions = commandsForCurrentHierarchy(newTest)
            hashedActions.forEach { (hash, action) ->
                if (hash !in mapOfActions.keys) {
                    mapOfActions[hash] = listOf(action)
                }
                else {
                    mapOfActions[hash] = (mapOfActions[hash]?: emptyList()) + listOf(action)
                }
            }
            line = readln()
            logger.info("Detected the following <hash, action>")
            hashedActions.forEach {
                logger.info("Action: ${it.second.asCommand()?.description()} | Hash: ${it.first}")
            }
            newTest = line == "nt"
        }
    }

}