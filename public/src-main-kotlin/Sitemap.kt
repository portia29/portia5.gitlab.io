import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class Sitemap(c: ContextInterface) : ContextInterface by c {
    private val srcDirsPaths: Set<Path> = setOf(srcTxtDir, srcRawDir, srcGenDir)
    val urls: MutableList<UrlMy> = mutableListOf()
    val srcPages: MutableMap<String, Page> = mutableMapOf()
    private var _map: Page? = null
    fun getMap(): Page? { return _map }
    private var _mapOrder: Page? = null
    fun getMapOrder(): Page { return _mapOrder!! }
    private var _mapChaos: Page? = null
    fun getMapChaos(): Page { return _mapChaos!! }
    private var parsedMap = sortedMapOf<String, TreeSet<String>>()

    fun updateUrls() {
        urls.clear()
        srcDirsPaths.forEach { srcDirPath ->
            Files.walk(srcDirPath).use { stream ->
                stream.filter(Files::isRegularFile).forEach {
                    urls.add(UrlMy(it, srcDirPath.relativize(it), dstMainDir))
                }
            }
        }
        urls.sortBy { it.relativeUrl }
        srcPages.clear()
        srcPages.putAll(urls.filter { !it.isRaw }.associate { it.relativeUrl to Page(it) })
    }

    private fun genStep(url: String, suburls: Set<String>?, level: Int): String {
        val builder = StringBuilder()
        if (level > 0) builder.append('\n')
        for (i in 1..level) {
            builder.append("    ")
        }
        if (level > 0) builder.append("$level ")
        builder.append(url)
        suburls?.forEach { builder.append(genStep(it, parsedMap[it], level + 1)) }
        parsedMap.remove(url)
        return builder.toString()
    }

    private fun generate(roots: Set<String>): String {
        val builder = StringBuilder()
        roots.forEach {
            if (builder.isNotEmpty()) builder.append("\n")
            builder.append(genStep(it, parsedMap[it], 0))
        }
        return builder.toString()
    }

    fun updateMaps(mapOfLinks: SortedMap<String, TreeSet<String>>) {
        parsedMap = mapOfLinks
        // Recreate map page to allow it regeneration in reflective phase.
        val mapUrl = urls.find { it.relativeUrl == UtilsMy.MAP_RELATIVE_URL }
        if (mapUrl != null) {
            _map = Page(mapUrl)
            srcPages[UtilsMy.MAP_RELATIVE_URL] = getMap()!!
        }

        val mapOrderSrcRelativePath = Path.of(UtilsMy.MAP_ORDER_RELATIVE_PATH)
        val mapOrderSrcAbsolutePath: Path = srcGenDir.resolve(mapOrderSrcRelativePath)
        val mapOrderUri = UrlMy(mapOrderSrcAbsolutePath, mapOrderSrcRelativePath, dstMainDir)
        if (urls.contains(mapOrderUri)) throw IllegalStateException()
        urls.add(mapOrderUri)
        val mapChaosSrcRelativePath = Path.of(UtilsMy.MAP_CHAOS_RELATIVE_PATH)
        val mapChaosSrcAbsolutePath: Path = srcGenDir.resolve(mapChaosSrcRelativePath)
        val mapChaosUri = UrlMy(mapChaosSrcAbsolutePath, mapChaosSrcRelativePath, dstMainDir)
        if (urls.contains(mapChaosUri)) throw IllegalStateException()

        urls.sortBy { it.relativeUrl }
        urls.forEach {
            if (it.isRaw && it.dstRelativePathString.endsWith(".html")) {
                parsedMap[it.absoluteUrl] = null
            }
            if (!it.isRaw && !parsedMap.contains(it.absoluteUrl)) {
                parsedMap[it.absoluteUrl] = null
            }
        }

        mapOrderSrcAbsolutePath.toFile()
            .writeText(generate(setOf(urls.find { it.isRoot }!!.absoluteUrl)))
        _mapOrder = Page(mapOrderUri)
        srcPages[mapOrderUri.relativeUrl] = getMapOrder()
        mapChaosSrcAbsolutePath.toFile()
            .writeText(generate(parsedMap.keys.toSet()))
        _mapChaos = Page(mapChaosUri)
        srcPages[mapChaosUri.relativeUrl] = getMapChaos()
        testMap()
    }

    private fun testMap() {
        val mapFull = StringBuilder()
        urls.filter { it.isDirectory }.forEach {
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            mapFull.append("[1] ${it.relativeUrl} ${it.redirects}")
        }
        urls.filter { !it.isPage }.forEach {
            if (it.redirects.isNotEmpty()) throw IllegalStateException()
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            mapFull.append("[2] ${it.relativeUrl}")
        }
        urls.filter { it.isPage && !it.isDirectory }.forEach {
            if (mapFull.isNotEmpty()) mapFull.append('\n')
            if (it.redirects.size != 1) throw IllegalStateException()
            if (it.redirects.first() != "${it.relativeUrl}.html") throw IllegalStateException()
            mapFull.append("[3] ${it.relativeUrl}[.html]")
        }
        dstTestDir.resolve("sitemap-full.txt").toFile()
            .writeText(mapFull.toString())
    }
}