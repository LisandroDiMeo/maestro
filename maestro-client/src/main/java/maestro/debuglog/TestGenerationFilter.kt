package maestro.debuglog

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class TestGenerationFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent?): FilterReply {
        return if (event != null && event.loggerName.contains("TestSuiteGenerator")) {
            FilterReply.ACCEPT;
        } else {
            FilterReply.DENY;
        }
    }
}
