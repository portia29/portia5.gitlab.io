
import UtilsMy.srcRawDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class GalleryGrid {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            GalleryGrid().main()
        }
    }

    @Serializable
    data class ImageInfo(val name: String, val w: Int, val h: Int)

    fun resolve(path: String): String {
        val rawDirPath = srcRawDir.absolutePathString()
        val imagesInfosFile = Paths.get("$rawDirPath$path/infos.json").toFile()
        val imagesInfos = Json.decodeFromString<List<ImageInfo>>(imagesInfosFile.readText())
        val displaySizeHorizontal = 300
        val displaySizeSquare = 340
        val builder = StringBuilder()
        builder.append("""<div class="justified-image-grid">""")
        imagesInfos.forEach {
            if (builder.isNotEmpty()) builder.appendLine()
            val imgElem = """<img src="$path/${it.name}" alt="${it.name}"/>"""
            val w = it.w.toDouble()
            val h = it.h.toDouble()
            val displaySize = if (w > h) displaySizeHorizontal else displaySizeSquare
            val iElem = """<i style="padding-bottom:${h/w*100}%"></i>"""
            val divElem = """<div style="flex-grow:${w*100/h};flex-basis:${w*displaySize/h}px;">$iElem$imgElem</div>"""
            builder.append(divElem)
        }
        for (i in 1..10) {
            if (builder.isNotEmpty()) builder.appendLine()
            builder.append("""<div class="placeholder"></div>""")
        }
        builder.appendLine()
        builder.append("""</div>""")
        return builder.toString()
    }

    fun main() {
        val html = resolve("/image/gallery")
        val template = UtilsMy.srcResDir.resolve("page-template.html").toFile()
        srcRawDir.resolve("test/image/test4.html").toFile().writeText(
            template.readText().replace("<!--DATA-->", html))
    }
}