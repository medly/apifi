package apifi

import apifi.codegen.CodeGenerator
import apifi.codegen.CommonFileContent
import apifi.parser.CommonSpecFileParser
import apifi.parser.SpecFileParser
import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.parser.OpenAPIV3Parser
import java.io.File
import java.nio.file.FileSystems

fun main(args: Array<String>) {
    val specFileDir = File(args[0])
    val outputDir = File(args[1])
    outputDir.mkdirs()
    val basePackageName = args[2]

    if (!specFileDir.isDirectory || !outputDir.isDirectory) {
        println("invalid spec file or output directory")
    } else {
        val specFiles = specFiles(specFileDir)
        val commonSpecFile = specFiles.firstOrNull { it.nameWithoutExtension == "common" }
        val commonContent = commonContent(commonSpecFile, basePackageName)
        (commonContent.modelFiles + commonContent.securityFiles.values).forEach { writeToFile(it, outputDir) }
        specFiles.filter { it != commonSpecFile }.forEach { specFile ->
            val openApi = OpenAPIV3Parser().read(specFile.absolutePath)
            val spec = SpecFileParser.parse(openApi)
            CodeGenerator.generate(spec, "$basePackageName.${specFile.nameWithoutExtension}", commonContent).forEach {
                writeToFile(it, outputDir)
            }
        }
    }
}

private fun specFiles(root: File): Sequence<File> {
    val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.yml")!!
    return root.walkTopDown().filter { file -> pathMatcher.matches(file.toPath()) }
}

private fun commonContent(commonSpecFile: File?, basePackageName: String): CommonFileContent {
    if(commonSpecFile != null) {
        val openApiSpec = OpenAPIV3Parser().read(commonSpecFile.absolutePath)
        val commonSpec = CommonSpecFileParser.parse(openApiSpec)
        return CodeGenerator.generateCommon(commonSpec, basePackageName)
    }
    return CommonFileContent(emptyList(), emptyMap())
}

private fun writeToFile(fileSpec: FileSpec, outputDir: File) {
    val outFileParentDir = File(outputDir, fileSpec.packageName.replace(".", File.separator))
    outFileParentDir.mkdirs()
    File(outFileParentDir, fileSpec.name).writeText(fileSpec.toString())
}