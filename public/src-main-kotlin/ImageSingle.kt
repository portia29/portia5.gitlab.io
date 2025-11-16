
class ImageSingle {
    fun resolve(path: String): String {
        val builder = StringBuilder()
        builder.append("""<img src="$path" alt="Image $path">""")
        return builder.toString()
    }
}