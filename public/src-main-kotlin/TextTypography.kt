import UtilsMy.abstractSeparatorTemp
import UtilsMy.textRawEnd
import UtilsMy.textRawStart

class TextTypography {
    private val typewriterApostrophes = true
    private val breakLevelOne = "</>"
    private val breakLevelOneBeautified = "<•>"
    private val dataStart = "<<"
    private val dataEnd = ">>"
    private var enabled = true
    private var lineTransform = LineTransform()

    fun transformWord(url: UrlMy, word: String): String {
        if (word == breakLevelOne) return breakLevelOneBeautified
        // 《》 ⟨⟩ ❝❞
        if (word == dataStart) return "❝"
        if (word == dataEnd) return "❞"
        if (!typewriterApostrophes) {
            if (!UtilsMy.isHyperlink(word)) {
                // ''''' - Wikipedia typewriter apostrophe.
                // ’’’’’ - U+2019 RIGHT SINGLE QUOTATION MARK / Substack curly apostrophe.
                // ʼʼʼʼʼ (U+02BC MODIFIER LETTER APOSTROPHE)
                return word.replace("'", "’")
            }
        }
        return word
    }

    fun transformLine(url: UrlMy, line: String): String {
        var newLine = line
        if (line.startsWith("- ")) {
            newLine = newLine.replaceFirst("- ", "— ")
        }
        newLine = newLine.replace("...", "…").replace(" - ", " — ")
        return lineTransform.transform(url, newLine, ::transformWord)
    }

    val shortSeparator = abstractSeparatorTemp
    val beautifiedShortSeparator = "⁂ ⁂ ⁂"

    fun transformParagraph(url: UrlMy, paragraph: String): String {
        if (enabled && paragraph == shortSeparator) return beautifiedShortSeparator
        if (enabled && paragraph == "...") return "∙∙∙"
        val result = StringBuilder()
        UtilsMy.splitParagraphToLines(paragraph).forEach { line ->
            if (line == textRawStart) {
                enabled = false
                lineTransform = LineTransform(false)
            } else if (line == textRawEnd) {
                enabled = true
                lineTransform = LineTransform(true)
            }
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            if (enabled) result.append(transformLine(url, line)) else result.append(line)
        }
        return result.toString()
    }

    fun transform(url: UrlMy, text: String): String {
        val result = StringBuilder()
        UtilsMy.splitToParagraphs(text).forEach { paragraph ->
            if (result.isNotEmpty()) {
                result.append("\n\n")
            }
            result.append(transformParagraph(url, paragraph))
        }
        return result.toString()
    }
}