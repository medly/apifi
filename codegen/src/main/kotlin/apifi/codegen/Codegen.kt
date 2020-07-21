package apifi.codegen

import apifi.parser.OpenApiSpecReader
import io.swagger.v3.parser.OpenAPIV3Parser
import java.io.File


class CodegenIO {
    private val openAPIV3Parser = OpenAPIV3Parser()
    private val openApiSpecReader = OpenApiSpecReader()
    private val codeGenerator = CodeGenerator()

    fun execute(specFile: File, outputDir: File, basePackageName: String) {
        outputDir.mkdirs()

        if (!specFile.isFile || !outputDir.isDirectory) {
            throw Exception("invalid spec file or output directory")
        }

        val openApi = openAPIV3Parser.read(specFile.absolutePath)
        val spec = openApiSpecReader.read(openApi)
        val fileSpecs = codeGenerator.generate(spec, "$basePackageName.${specFile.nameWithoutExtension}")

        fileSpecs.forEach { fileSpec ->
            val outFileParentDir = File(outputDir, fileSpec.packageName.replace(".", File.separator))
            outFileParentDir.mkdirs()
            File(outFileParentDir, fileSpec.name).writeText(fileSpec.toString())
        }
    }

}

