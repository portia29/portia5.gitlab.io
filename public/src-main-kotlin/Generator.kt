
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class Generator(c: ContextInterface = Context()) : ContextInterface by c {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Generator().main()
        }
    }

    private val includeTransform = IncludeTransform(this)
    private val htmlTransform = HtmlTransform()
    private val redirects = mutableSetOf<String>()
    val sitemap = Sitemap(c)
    var firstRun = true

    private fun cleanDstDirs() {
        dstMainDir.toFile().deleteRecursively()
        dstMainDir.toFile().mkdir()
        srcGenDir.toFile().deleteRecursively()
        srcGenDir.toFile().mkdir()
    }

    private fun copyRawRes() {
        Files.walk(srcRawDir).forEach { srcRaw: Path ->
            if (srcRaw == srcRawDir) return@forEach
            Files.copy(srcRaw, dstMainDir.resolve(srcRawDir.relativize(srcRaw)), REPLACE_EXISTING)
        }
    }

    private fun genRedirects() {
        sitemap.urls.forEach {
            if (it.isIndexOfDirectory) {
                redirects.add("${it.relativeUrl} ${it.relativeUrl}.html 200")
            }
        }
        dstMainDir.resolve("_redirects").toFile().writeText(redirects.joinToString("\n"))
    }

    private fun processPage(page: Page) {
        page.srcAbsolutePath.toFile().writeText(page.formatted)
        includeTransform.transform(page)
        page.beautyText = TextTypography().transform(page.url, page.includeText)
        val bodyHtml = htmlTransform.textToHtml(page.url, page.beautyText)
        val htmlFile = page.dstAbsolutePath.toFile()
        htmlFile.parentFile.mkdirs()
        htmlFile.writeText(htmlTransform.htmlPage(page.title, bodyHtml))
    }

    private fun main() {
        cleanDstDirs()
        Favicon().main()
        Library().main()
        sitemap.updateUrls()
        sitemap.srcPages.forEach { processPage(it.value) }
        firstRun = false
        sitemap.updateMaps(htmlTransform.mapOfLinks)
        processPage(sitemap.getMapOrder())
        processPage(sitemap.getMapChaos())
        if (sitemap.getMap() != null) {
            processPage(sitemap.getMap()!!)
        }
        genRedirects()
        dstTestDir.resolve("links-list.txt").toFile()
            .writeText(htmlTransform.setOfLinks.joinToString("\n"))
        dstTestDir.resolve("long-words-list.txt").toFile()
            .writeText(htmlTransform.setOfLongWords.joinToString("\n"))
        copyRawRes()
    }
}