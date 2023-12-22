package maestro.cli.generative

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import maestro.TreeNode
import java.io.File

object TreeNodeReader {
    fun read(filePath: String): TreeNode {
        val objectMapper = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(KotlinModule.Builder().build())

        val fileStream = File(filePath).inputStream()
        val screen1Json = fileStream.bufferedReader().use { it.readText() }
        return objectMapper.readValue(
            screen1Json,
            TreeNode::class.java
        )
    }
}

