class LineTransform(
    private val multispacesOnlyAtStart: Boolean = true,
    private val spacesTransformer: (spaces: String) -> String = { spaces: String -> spaces }
) {

    class MultispacesOnlyAtStart(message: String) : Exception(message)

    // https://stackoverflow.com/questions/6151554/text-inside-div-not-showing-multiple-white-spaces-between-words
    // https://developer.mozilla.org/en-US/docs/Web/CSS/white-space
    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/pre
    val simpleSpacesTransformer: (spaces: String) -> String = { spaces: String ->
        spaces.replace(" ", "&nbsp;")
    }

    fun transform(url: UrlMy, line: String, wordTransformer: (url: UrlMy, word: String) -> String): String {
        val builder = StringBuilder()
        val spaces = StringBuilder()
        var firstWord = true
        var firstNonBlankWordTransformed = false
        line.split(' ').forEach { word ->
            if (firstWord) {
                firstWord = false
                builder.append(wordTransformer(url, word))
            } else {
                if (word.isEmpty()) {
                    spaces.append(' ')
                } else if (spaces.isNotEmpty()) {
                    spaces.append(' ')
                    builder.append(spacesTransformer(spaces.toString()))
                    spaces.clear()
                    builder.append(wordTransformer(url, word))
                } else {
                    builder.append(' ')
                    builder.append(wordTransformer(url, word))
                }
                if (multispacesOnlyAtStart) {
                    if (firstNonBlankWordTransformed) {
                        if (word.isEmpty()) {
                            throw MultispacesOnlyAtStart(line)
                        }
                    } else if (word.isNotBlank()) {
                        firstNonBlankWordTransformed = true
                    }
                }
            }
        }
        if (builder.length > 1 && builder[0] == ' ' && builder[1] != ' ') {
            val temp = StringBuilder()
            temp.append(spacesTransformer(" "))
            temp.append(builder.subSequence(1, builder.length))
            return temp.toString()
        }
        return builder.toString()
    }
}