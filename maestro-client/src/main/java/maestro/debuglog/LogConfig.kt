package maestro.debuglog

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.status.NopStatusListener
import org.slf4j.LoggerFactory
import java.io.File

object LogConfig {
    private const val LOG_PATTERN = "[%-5level] %logger{36} - %msg%n"

    fun configure(logFileName: String) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.statusManager.add(NopStatusListener())
        loggerContext.reset()

        val encoder = createEncoder(loggerContext)
//        createAndAddConsoleAppender(loggerContext, encoder) // un-comment to enable console logs
        createAndAddFileAppender(loggerContext, encoder, logFileName)

        loggerContext.getLogger("ROOT").level = Level.INFO
    }

    private fun createEncoder(loggerContext: LoggerContext): PatternLayoutEncoder {
        return PatternLayoutEncoder().apply {
            context = loggerContext
            pattern = LOG_PATTERN
            start()
        }
    }

    private fun createAndAddConsoleAppender(
        loggerContext: LoggerContext,
        encoder: PatternLayoutEncoder
    ) {
        val consoleAppender = ch.qos.logback.core.ConsoleAppender<ILoggingEvent>().apply {
            context = loggerContext
            setEncoder(encoder)
            start()
        }

        loggerContext.getLogger("ROOT").addAppender(consoleAppender)
    }

    private fun createAndAddFileAppender(
        loggerContext: LoggerContext,
        encoder: PatternLayoutEncoder,
        logFileName: String
    ) {
        val fileAppender = FileAppender<ILoggingEvent>().apply {
            context = loggerContext
            setEncoder(encoder)
            this.file = logFileName
            start()
        }

        loggerContext.getLogger("ROOT").addAppender(fileAppender)
    }

    fun switchLogbackConfiguration(
        configurationFilePath: String
    ) {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.reset()

        val configurator = JoranConfigurator()
        configurator.context = loggerContext

        try {
            configurator.doConfigure(File(configurationFilePath))
        } catch (e: Exception) {
            // Handle any potential configuration errors
            println("FILE NOT FOUND ${e.message}")
            e.printStackTrace()
        }
    }

}
