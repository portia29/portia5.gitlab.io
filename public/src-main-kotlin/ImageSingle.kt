
class ImageSingle {
    fun resolve(path: String): String {
        val builder = StringBuilder()
        builder.append("""<div class="article-text">""")
        builder.appendLine()
        builder.append("""<img class="article-image" src="$path" alt="Image $path">""")
        builder.appendLine()
        builder.append("""</div>""")
        return builder.toString()
    }
}