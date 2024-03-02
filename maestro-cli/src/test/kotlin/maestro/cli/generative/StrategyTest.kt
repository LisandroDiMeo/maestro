package maestro.cli.generative

import kotlinx.coroutines.runBlocking
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.strategies.random.RandomCommandSelection
import maestro.cli.runner.gen.commandselection.strategies.viewranking.ViewRanking
import maestro.cli.runner.gen.hierarchyanalyzer.AndroidHierarchyAnalyzer
import maestro.cli.runner.gen.presentation.model.ExecutedCommandsObservable
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StrategyTest {
    private val screen1 = "generative-test-resources/contacts_main_screen.json"
    private val contactsMainScreen = TreeNodeReader.read(screen1)

    @Test
    fun `both strategies starts with the same destinations`(): Unit = runBlocking {
        val disambiguationRule =
            SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether(
                false
            )
        val executedCommandsObservable1 = ExecutedCommandsObservable()
        val executedCommandsObservable2 = ExecutedCommandsObservable()

        val random = RandomCommandSelection(
            executedCommandsObservable = executedCommandsObservable1
        )
        val vr = ViewRanking(executedCommandsObservable = executedCommandsObservable2)
        val androidHierarchyAnalyzerVR = AndroidHierarchyAnalyzer(
            selectionStrategy = vr,
            disambiguationRule = disambiguationRule
        )
        val androidHierarchyAnalyzerRandom = AndroidHierarchyAnalyzer(
            selectionStrategy = random,
            disambiguationRule = disambiguationRule
        )
        val hierarchy = ViewHierarchy(contactsMainScreen)
        val f1 = androidHierarchyAnalyzerVR.fetchCommandFrom(
            hierarchy, true, false
        )
        val f2 = androidHierarchyAnalyzerRandom.fetchCommandFrom(
            hierarchy, true, false
        )
        assertEquals(
            executedCommandsObservable1.commandInformationState.value?.destinations,
            executedCommandsObservable2.commandInformationState.value?.destinations
        )
        assertTrue(
            executedCommandsObservable1.commandInformationState.value != null
        )
    }
}