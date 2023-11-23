package maestro.cli.runner.gen

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import maestro.DeviceInfo
import maestro.ElementFilter
import maestro.Filters
import maestro.Filters.asFilter
import maestro.FindElementResult
import maestro.Maestro
import maestro.MaestroException
import maestro.ViewHierarchy
import maestro.cli.runner.gen.commandselection.CommandInformation
import maestro.cli.runner.gen.hierarchyanalyzer.HierarchyAnalyzer
import maestro.cli.runner.gen.viewranking.actionhash.TreeIndexer
import maestro.js.GraalJsEngine
import maestro.js.JsEngine
import maestro.js.RhinoJsEngine
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.HideKeyboardCommand
import maestro.orchestra.InputRandomCommand
import maestro.orchestra.InputTextCommand
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.MaestroConfig
import maestro.orchestra.Orchestra
import maestro.orchestra.ScrollCommand
import maestro.orchestra.StopAppCommand
import maestro.orchestra.TapOnElementCommand
import maestro.orchestra.error.UnicodeNotSupportedError
import maestro.orchestra.filter.FilterWithDescription
import maestro.orchestra.filter.TraitFilters
import maestro.orchestra.runcycle.RunCycle
import maestro.utils.StringUtils.toRegexSafe
import okhttp3.OkHttpClient

class TestGenerationOrchestra(
    private val maestro: Maestro,
    private val packageName: String = "",
    private val lookupTimeoutMs: Long = 17000L,
    private val runCycle: RunCycle,
    private val hierarchyAnalyzer: HierarchyAnalyzer,
    private val httpClient: OkHttpClient? = null,
    private val testSize: Int = 5,
    private val endTestIfOutsideApp: Boolean = false,
) {
    private var copiedText: String? = null

    private var timeMsOfLastInteraction = System.currentTimeMillis()
    private var deviceInfo: DeviceInfo? = null
    private val commandsGenerated = mutableListOf<MaestroCommand>()

    private lateinit var jsEngine: JsEngine

    @Synchronized
    private fun initJsEngine() {
        if (this::jsEngine.isInitialized) {
            jsEngine.close()
        }
        val shouldUseGraalJs = true
        jsEngine = if (shouldUseGraalJs) {
            httpClient?.let { GraalJsEngine(it) } ?: GraalJsEngine()
        } else {
            httpClient?.let { RhinoJsEngine(it) } ?: RhinoJsEngine()
        }
    }

    fun startGeneration() {
        initJsEngine()
        commandsGenerated.clear()
        openApplication()
        // TODO's: improve active waits
        for (currentIteration in 1..testSize) {
            runBlocking {
                delay(2000L)
            }
            val hierarchy = maestro.viewHierarchy().run { ViewHierarchy(root = TreeIndexer.addTypeAndIndex(this.root)) }
            if (endTestIfOutsideApp && isOutsideApp(hierarchy) && currentIteration > 1) return
            val command = hierarchyAnalyzer.fetchCommandFrom(
                hierarchy,
                currentIteration == 1
            )
            commandsGenerated.add(command)

            runCycle.onCommandStart(currentIteration, command)
            try {
                deviceInfo = maestro.deviceInfo()
                executeCommand(command)
                runCycle.onCommandComplete(currentIteration, command)
            } catch (e: Throwable) {
                when (runCycle.onCommandFailed(currentIteration, command, e)) {
                    Orchestra.ErrorResolution.FAIL -> {
                        commandsGenerated.removeLast()
                    }
                    Orchestra.ErrorResolution.CONTINUE -> {
                        commandsGenerated.removeLast()
                    }
                }
            }
        }

    }

    private fun isOutsideApp(hierarchy: ViewHierarchy) =
        hierarchyAnalyzer.isOutsideApp(hierarchy, packageName)

    fun generatedCommands(): List<MaestroCommand> = commandsGenerated.toList()

    private fun openApplication() {
        val launchAppCommand = LaunchAppCommand(
            appId = packageName,
            clearState = false,
            clearKeychain = null,
            stopApp = true,
        )
        val maestroCommand = MaestroCommand(launchAppCommand = launchAppCommand)
        commandsGenerated.add(maestroCommand)
        runCycle.onCommandStart(0, maestroCommand)
        launchAppCommand(launchAppCommand)
        // TODO: Why I Need to wait the app to settle ?
        runBlocking {
            delay(2000L)
        }
        runCycle.onCommandComplete(0, maestroCommand)
    }

    private fun executeCommand(
        maestroCommand: MaestroCommand,
        config: MaestroConfig? = null
    ) {
        when (val command = maestroCommand.asCommand()) {
            is TapOnElementCommand -> {
                tapOnElement(
                    command,
                    command.retryIfNoChange ?: true,
                    command.waitUntilVisible ?: false,
                    config
                )
            }

            is BackPressCommand -> {
                backPress()
            }

            is HideKeyboardCommand -> {
                hideKeyboardCommand()
            }

            is StopAppCommand -> {
                stopAppCommand(command)
            }

            is ScrollCommand -> {
                scrollVerticalCommand()
            }

            is InputTextCommand -> {
                inputTextCommand(command)
            }

            is InputRandomCommand -> {
                inputTextRandomCommand(command)
            }

            else -> {
                true
            }
        }.also {
            if (it) timeMsOfLastInteraction = System.currentTimeMillis()
        }
    }

    private fun inputTextRandomCommand(command: InputRandomCommand): Boolean {
        inputTextCommand(InputTextCommand(text = command.genRandomString()))
        return true
    }

    private fun inputTextCommand(command: InputTextCommand): Boolean {
        if (!maestro.isUnicodeInputSupported()) {
            val isAscii = Charsets.US_ASCII.newEncoder()
                .canEncode(command.text)

            if (!isAscii) {
                throw UnicodeNotSupportedError(command.text)
            }
        }

        maestro.inputText(command.text)

        return true
    }

    private fun launchAppCommand(command: LaunchAppCommand): Boolean {
        try {
            if (command.clearKeychain == true) {
                maestro.clearKeychain()
            }
            if (command.clearState == true) {
                maestro.clearAppState(command.appId)
            }

            // For testing convenience, default to allow all on app launch
            val permissions = command.permissions ?: mapOf("all" to "allow")
            maestro.setPermissions(command.appId, permissions)

        } catch (e: Exception) {
            throw MaestroException.UnableToClearState("Unable to clear state for app ${command.appId}")
        }

        try {
            maestro.launchApp(
                appId = command.appId,
                launchArguments = command.launchArguments ?: emptyMap(),
                stopIfRunning = command.stopApp ?: true
            )
        } catch (e: Exception) {
            throw MaestroException.UnableToLaunchApp("Unable to launch app ${command.appId}: ${e.message}")
        }

        return true
    }

    private fun scrollVerticalCommand(): Boolean {
        maestro.scrollVertical()
        return true
    }

    private fun stopAppCommand(command: StopAppCommand): Boolean {
        maestro.stopApp(command.appId)
        return true
    }

    private fun tapOnElement(
        command: TapOnElementCommand,
        retryIfNoChange: Boolean,
        waitUntilVisible: Boolean,
        config: MaestroConfig?,
    ): Boolean {
        return try {
            val result = findElement(command.selector)
            maestro.tap(
                result.element,
                result.hierarchy,
                retryIfNoChange,
                waitUntilVisible,
                command.longPress ?: false,
                config?.appId
            )

            true
        } catch (e: MaestroException.ElementNotFound) {
            if (!command.selector.optional) {
                throw e
            } else {
                false
            }
        }
    }

    private fun hideKeyboardCommand(): Boolean {
        maestro.hideKeyboard()
        return true
    }

    private fun backPress(): Boolean {
        maestro.backPress()
        return true
    }

    private fun findElement(selector: ElementSelector, timeoutMs: Long = lookupTimeoutMs): FindElementResult {
        val (description, filterFunc) = buildFilter(selector, deviceInfo!!)
        return maestro.findElementWithTimeout(
            timeoutMs = timeoutMs,
            filterFunc
        ) ?: throw MaestroException.ElementNotFound(
            "Element not found: $description", maestro.viewHierarchy().root
        )
    }

    private fun buildFilter(
        selector: ElementSelector,
        deviceInfo: DeviceInfo,
    ): FilterWithDescription {
        val filters = mutableListOf<ElementFilter>()
        val descriptions = mutableListOf<String>()

        selector.textRegex
            ?.let {
                descriptions += "Text matching regex: $it"
                filters += Filters.deepestMatchingElement(
                    Filters.textMatches(it.toRegexSafe(Orchestra.REGEX_OPTIONS))
                )
            }

        selector.idRegex
            ?.let {
                descriptions += "Id matching regex: $it"
                filters += Filters.deepestMatchingElement(
                    Filters.idMatches(it.toRegexSafe(Orchestra.REGEX_OPTIONS))
                )
            }

        selector.classNameRegex
            ?.let {
                descriptions += "Class Name matching regex: $it"
                filters += Filters.deepestMatchingElement(
                    Filters.classMatches(it)
                )
            }

        selector.packageNameRegex
            ?.let {
                descriptions += "Package Name matching regex: $it"
                filters += Filters.deepestMatchingElement(
                    Filters.packageMatches(it)
                )
            }

        selector.size
            ?.let {
                descriptions += "Size: $it"
                filters += Filters.sizeMatches(
                    width = it.width,
                    height = it.height,
                    tolerance = it.tolerance,
                ).asFilter()
            }

        selector.below
            ?.let {
                descriptions += "Below: ${it.description()}"
                filters += Filters.below(buildFilter(it, deviceInfo).filterFunc)
            }

        selector.above
            ?.let {
                descriptions += "Above: ${it.description()}"
                filters += Filters.above(buildFilter(it, deviceInfo).filterFunc)
            }

        selector.leftOf
            ?.let {
                descriptions += "Left of: ${it.description()}"
                filters += Filters.leftOf(buildFilter(it, deviceInfo).filterFunc)
            }

        selector.rightOf
            ?.let {
                descriptions += "Right of: ${it.description()}"
                filters += Filters.rightOf(buildFilter(it, deviceInfo).filterFunc)
            }

        selector.containsChild
            ?.let {
                descriptions += "Contains child: ${it.description()}"
                filters += Filters.containsChild(findElement(it).element).asFilter()
            }

        selector.containsDescendants
            ?.let { descendantSelectors ->
                val descendantDescriptions =
                    descendantSelectors.joinToString("; ") { it.description() }
                descriptions += "Contains descendants: $descendantDescriptions"
                filters += Filters.containsDescendants(descendantSelectors.map {
                    buildFilter(
                        it,
                        deviceInfo
                    ).filterFunc
                })
            }

        selector.traits
            ?.map {
                TraitFilters.buildFilter(it)
            }
            ?.forEach { (description, filter) ->
                descriptions += description
                filters += filter
            }

        selector.enabled
            ?.let {
                descriptions += if (it) {
                    "Enabled"
                } else {
                    "Disabled"
                }
                filters += Filters.enabled(it)
            }

        selector.selected
            ?.let {
                descriptions += if (it) {
                    "Selected"
                } else {
                    "Not selected"
                }
                filters += Filters.selected(it)
            }

        selector.checked
            ?.let {
                descriptions += if (it) {
                    "Checked"
                } else {
                    "Not checked"
                }
                filters += Filters.checked(it)
            }

        selector.focused
            ?.let {
                descriptions += if (it) {
                    "Focused"
                } else {
                    "Not focused"
                }
                filters += Filters.focused(it)
            }

        var resultFilter = Filters.intersect(filters)
        resultFilter = selector.index
            ?.toDouble()
            ?.toInt()
            ?.let {
                Filters.compose(
                    resultFilter,
                    Filters.index(it)
                )
            } ?: Filters.compose(
            resultFilter,
            Filters.clickableFirst()
        )

        return FilterWithDescription(
            descriptions.joinToString(", "),
            resultFilter,
        )
    }
}
