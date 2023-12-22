package maestro.cli.generative

import maestro.ViewHierarchy
import maestro.cli.runner.gen.hierarchyanalyzer.IOSHierarchyAnalyzer
import maestro.cli.runner.gen.viewdisambiguator.SequentialDisambiguation
import maestro.cli.runner.gen.viewranking.ViewRanking
import maestro.cli.runner.gen.viewranking.actionhash.TreeIndexer
import org.junit.jupiter.api.Test

class IOSHierarchyAnalyzerTest {
    private val contactsMainScreenJson = "generative-test-resources/ios_contacts.json"

    private val contactsMainScreen = TreeNodeReader.read(contactsMainScreenJson)

    @Test
    fun health_check() {
        val disambiguationRule =
            SequentialDisambiguation.sequentialRuleForIdTextAccTextAndAllTogether(true)
        val analyzer = IOSHierarchyAnalyzer(
            ViewRanking {},
            disambiguationRule
        )
        var hierarchy =
            contactsMainScreen.run { ViewHierarchy(root = TreeIndexer.addTypeAndIndex(this)) }
        val command = analyzer.fetchCommandFrom(
            hierarchy,
            true,
            false
        )
        println()
    }
}
