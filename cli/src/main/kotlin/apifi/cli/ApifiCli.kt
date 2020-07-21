package apifi.cli

import apifi.codegen.CodegenIO
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file


/**
 * Entry point - The main function
 */
fun main(args: Array<String>) = ApifiCli().main(args)


/**
 * Implementation of CLI using Apifi API
 *
 * Can use env variable to pass in sensitive information
 */
class ApifiCli : CliktCommand( // command name is inferred as apifi-cli
        name = "apifi-codegen",
        help = """
            Generates Kotlin Source files for given Open API Spec file
        """
) {
    private val inputFile by option("-f", "--input-file", help = "Open API Spec file that is entry point, this file can refer to another yaml files")
            .file(canBeFile = true, canBeDir = false, mustExist = true)
            .required()

    private val outDir by option("-o", "--out-dir", help = "Output dir where source should be generated")
            .file(canBeFile = false, canBeDir = true, mustExist = true)
            .required()

    private val basePackage by option("-p", "--base-package", help = "package name for generated classes")
            .required()


    override fun run() {
        CodegenIO().execute(inputFile, outDir, basePackage)
    }
}


