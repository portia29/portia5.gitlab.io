import UtilsMy.textRawEnd
import UtilsMy.textRawStart

/**
 * Этот класс используется следующим образом, функция transform получает на вход текст заметки,
 * возвращает отформатированный текст и этот отформатированный текст перезаписывает оригинальное содержимое заметки.
 */
class TextFormatter {
    private var enabled = true
    fun transformLine(line: String): String {
        if (line.trim().contains("  ")) {
            // Detect multiple spaces in the middle of the line, it's usually a typos.
            throw LineTransform.MultispacesOnlyAtStart("Double space in line: [$line]")
        }
        return line.trimEnd()
            .replace("…", "...")
            .replace("’", "'")
            .replace("ʼ", "'")
            .replace("“", "\"")
            .replace("”", "\"")
    }

    fun transformParagraph(paragraph: String): String {
        val result = StringBuilder()
        UtilsMy.splitParagraphToLines(paragraph).forEach { line ->
            if (line == textRawStart) {
                enabled = false
            } else if (line == textRawEnd) {
                enabled = true
            }
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            if (enabled) {
                result.append(transformLine(line))
            } else {
                result.append(line)
            }
        }
        return result.toString()
    }

    fun transform(text: String): String {
        val result = StringBuilder()
        UtilsMy.splitToParagraphs(text).forEach { paragraph ->
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(transformParagraph(paragraph))
        }
        return result.toString()
    }
}