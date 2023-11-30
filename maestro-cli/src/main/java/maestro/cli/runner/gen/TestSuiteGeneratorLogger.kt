package maestro.cli.runner.gen

import maestro.debuglog.LogConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TestSuiteGeneratorLogger {
    private var _logger: Logger? = null

    val logger: Logger
        get() {
            if (_logger == null) {
                LogConfig.switchLogbackConfiguration(
                    LoggingConfigurationFile
                )
                _logger = LoggerFactory.getLogger(TestSuiteGeneratorLogger::class.java)
            }
            return _logger!!
        }

    private const val LoggingConfigurationFile =
        "maestro-cli/src/main/resources/logback-generative.xml"
}
