package maestro.cli.runner.gen.presentation.flowfilegeneration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import maestro.cli.runner.ConfigHeader
import maestro.orchestra.MaestroCommand
import maestro.orchestra.yaml.YamlFluentCommand
import java.io.File
import java.io.FileOutputStream

class FlowFileWriterImpl(private val packageName: String, private val strategy: String = "") : FlowFileWriter {
    override fun writeFlowFileFrom(
        commands: List<MaestroCommand>,
        id: Int
    ) {
        val packageDirectory = File("generated-flows/$packageName")
        val strategyDirectory = File("generated-flows/$packageName/$strategy")
        if (!packageDirectory.exists()) packageDirectory.mkdir()
        if (!strategyDirectory.exists()) strategyDirectory.mkdir()
        val yamlCommands = commands.map { YamlFluentCommand.fromCommand(it) }
        val config = ConfigHeader(packageName)
        val flowFileMapper = ObjectMapper(YAMLFactory())
        val configHeaderMapper =
            ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
        flowFileMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val tempGeneratedFlowFile = File("temp-generated-flow.yaml")
        flowFileMapper.writeValue(
            tempGeneratedFlowFile,
            yamlCommands
        )
        val configFile = File("aux_config.yaml")
        configHeaderMapper.writeValue(
            configFile,
            config
        )
        val generatedFlowFile = File("generated-flows/$packageName/$strategy/generated-flow-$id.yaml")
        FileOutputStream(
            generatedFlowFile,
            true
        ).use { output ->
            configFile
                .forEachBlock { buffer, bytesRead ->
                    output.write(
                        buffer,
                        0,
                        bytesRead
                    )
                }
            tempGeneratedFlowFile
                .forEachBlock { buffer, bytesRead ->
                    output.write(
                        buffer,
                        0,
                        bytesRead
                    )
                }
        }
        tempGeneratedFlowFile.delete()
        configFile.delete()
    }
}
