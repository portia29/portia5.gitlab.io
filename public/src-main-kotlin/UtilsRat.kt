import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val printErrors = "true".toBoolean()

fun ratExec(vararg cmd: String): String {
    var result = ""
    val process = ProcessBuilder(*cmd).start()
    process.inputStream.reader(Charsets.UTF_8).use {
        result = it.readText()
    }
    if (printErrors) {
        process.errorStream.reader(Charsets.UTF_8).use {
            val errors = it.readText()
            if (errors.isNotEmpty()) {
                println("Error: $errors")
            }
        }
    }
    check(process.waitFor(10, TimeUnit.SECONDS))
    check(process.exitValue() == 0)
    return result
}

fun ratExec(workingDir: File, vararg command: String, enableQuotes: Boolean = false): String? {
    if (command.joinToString(" ").contains("\"") && !enableQuotes) {
        // Quotes in arguments is a common source of errors that produce disorienting
        // execution output, like no output at all, and its hard to pinpoint cause
        // of such errors by looking at such output. Quotes in arguments may have
        // a valid use, but considering stated problem we lay requirement of explicit
        // flag allowing quotes in arguments.
        // More:
        //  - https://stackoverflow.com/questions/12124935
        //  - https://stackoverflow.com/questions/18842307
        //  - https://stackoverflow.com/questions/32314645
        //  - https://blog.krecan.net/2008/02/09/processbuilder-and-quotes/
        throw IllegalStateException()
    }
    try {
        val proc = ProcessBuilder(*command)
            .directory(workingDir)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        proc.waitFor(1, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText().trim()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

/**
 * Works for cases when you call a program with such command and arguments in one line,
 * that this line splitted by whitespace will produce command and array of arguments
 * asseptable by the called program.
 */
fun ratExecSimple(workingDir: File, command: String): String? {
    return ratExec(workingDir, *command.split("\\s".toRegex()).toTypedArray())
}