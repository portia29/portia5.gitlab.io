import UtilsAbsolute.dstTestDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.util.*

/**
 * Novel - роман.
 */
class Library {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Library().main()
        }
    }

    interface FiltrableWriting {
        fun containAnyOfTags(vararg tags: String): Boolean
    }

    @Serializable
    data class Database(val texts: List<Writing>)

    @Serializable
    data class Name(
        val name: String,
        val language: String,
        val link: String? = null,
        val comment: String? = null
    )

    @Serializable
    data class Author(val names: LinkedHashSet<Name>) {
        fun id(): String {
            val nameEn = names.filter { it.language == "en" }
            require(nameEn.size < 2)
            return if (nameEn.size == 1) {
                nameEn.first().name
            } else {
                names.first().name
            }
        }

        fun sort(): String {
            return id() + names.size
        }
    }

    @Serializable
    data class Writing(
        val names: List<Name>,
        val authors: MutableList<Author>,
        val tags: Set<String>,
        val rating: Int
    ) : FiltrableWriting {
        override fun containAnyOfTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

    val defaultLang = "en"

    private fun formatWritings(writings: List<Writing>, language: String): String {
        return writings.joinToString(
            separator = "», «", prefix = ": «", postfix = "».", transform = {
                var writingName = it.names[0].name
                if (it.names.size > 1) {
                    it.names.forEach { wn ->
                        if (wn.language == language) writingName = wn.name
                    }
                }
                writingName
            })
    }

    private fun formatAuthors(authors: List<Author>, language: String): String {
        var author = authors[0].names.first().name
        if (authors[0].names.size > 1) {
            authors[0].names.forEach {
                if (it.language == language) author = it.name
            }
        }
        return author
    }

    fun main() {
        val writingsIn = loadWritings(UtilsAbsolute.srcResDir)
        testGeneration(writingsIn)
        val text = StringBuilder()
        val fullList = LinkedList<Writing>()
        fullList.addAll(writingsIn.sortedBy { it.rating })
        val currentList = LinkedList<Writing>()
        fullList.forEach { w ->
            if (currentList.isNotEmpty() && currentList.first().authors != w.authors) {
                if (text.isNotEmpty()) {
                    text.append(" ")
                }
                text.append(formatAuthors(currentList.first().authors, defaultLang))
                text.append(formatWritings(currentList, defaultLang))
                currentList.clear()
            }
            currentList.add(w)
        }
        if (currentList.isNotEmpty()) {
            if (text.isNotEmpty()) {
                text.append(" ")
            }
            text.append(formatAuthors(currentList.first().authors, defaultLang))
            text.append(formatWritings(currentList, defaultLang))
            currentList.clear()
        }
        val libraryOut = UtilsAbsolute.srcGenDir
        libraryOut.resolve("library.txt").toFile().writeText(text.toString())
    }

    fun saveLibrary(dst: Path, writingsIn: MutableList<Writing>) {
        val format = Json { prettyPrint = true }
        val writingsToSave = writingsIn.sortedBy { it.rating }
        val outWritingsFile = dst.resolve("library.json").toFile()
        outWritingsFile.writeText(format.encodeToString(writingsToSave))
    }

    fun loadWritings(srcDir: Path): MutableList<Writing> {
        val writingsFile = srcDir.resolve("library.json").toFile()
        val writings = Json.decodeFromString<MutableList<Writing>>(writingsFile.readText())
        saveLibrary(srcDir, writings)
        return writings
    }

    fun testGeneration(writingsIn: MutableList<Writing>) {
        fun mainListLong(writingsIn: MutableList<Writing>): StringBuilder {
            fun recommendationFilter(w: FiltrableWriting): Boolean {
                return w.containAnyOfTags(
                    "recommendation", "visible"
                ) && !w.containAnyOfTags("invisible")
            }

            fun entertainingFilter(w: FiltrableWriting): Boolean {
                return w.containAnyOfTags("entertaining") && !w.containAnyOfTags("invisible")
            }

            val b = StringBuilder()
            val fullList = LinkedList<Writing>()
            fullList.addAll(writingsIn.filter { recommendationFilter(it) }.sortedBy { it.rating })
            fullList.addAll(writingsIn.filter { entertainingFilter(it) }.sortedBy { it.rating })
            val currentList = LinkedList<Writing>()
            fullList.forEach { w ->
                if (currentList.isNotEmpty() && currentList.first().authors != w.authors) {
                    if (b.isNotEmpty()) {
                        b.append(" ")
                    }
                    b.append(formatAuthors(currentList.first().authors, defaultLang))
                    b.append(formatWritings(currentList, defaultLang))
                    currentList.clear()
                }
                currentList.add(w)
            }
            if (currentList.isNotEmpty()) {
                if (b.isNotEmpty()) {
                    b.append(" ")
                }
                b.append(formatAuthors(currentList.first().authors, defaultLang))
                b.append(formatWritings(currentList, defaultLang))
                currentList.clear()
            }
            b.append(" ")
            b.append("Ещё я читал этих авторов. ")
            val mainListAuthors = writingsIn.filter {
                recommendationFilter(it) || entertainingFilter(it) || it.tags.contains("invisible")
            }.groupBy { it.authors }.keys
            val authorsList =
                writingsIn.filter { !mainListAuthors.contains(it.authors) }.sortedBy { it.rating }
                    .groupBy { it.authors }.keys.toMutableList()
            b.append(
                authorsList.joinToString(
                    separator = ", ", prefix = "", postfix = ".", transform = {
                        it.joinToString(
                            separator = ", ", prefix = "", postfix = "", transform = { author ->
                                author.names.first().name
                            })
                    })
            )
            return b
        }

        fun mainListMedium(
            writingsIn: MutableList<Writing>, predicate: (Writing) -> Boolean
        ): StringBuilder {
            val b = StringBuilder()
            val list = writingsIn.filter { predicate(it) }.sortedBy { it.rating }
            for (i in 0..9) {
                val w = list[i]
                if (b.isNotEmpty()) {
                    b.append(" ")
                }
                b.append(formatAuthors(w.authors, defaultLang))
                b.append(formatWritings(listOf(w), defaultLang))
            }
            return b
        }

        fun mainListShort(writingsIn: MutableList<Writing>): StringBuilder {
            val b = StringBuilder()
            val list = writingsIn.filter {
                it.containAnyOfTags(
                    "recommendation", "visible"
                ) && !it.containAnyOfTags("invisible")
            }.sortedBy { it.rating }.groupBy { it.authors }.keys.toList()
            for (i in 0..9) {
                if (i != 0) {
                    b.append(", ")
                }
                b.append(formatAuthors(list[i], defaultLang))
            }
            b.append(".")
            return b
        }

        fun mainListSummary(writingsIn: MutableList<Writing>): StringBuilder {
            val b = StringBuilder()
            val writingsCount = writingsIn.size
            val authorsCount = writingsIn.groupBy { it.authors }.keys.size
            b.append("Коллекция штук, прочитанных мной,")
            b.append(" штук всего $writingsCount, авторов всего $authorsCount.")
            return b
        }

        val text = StringBuilder()
        text.append(mainListSummary(writingsIn)).appendLine().appendLine()
        text.append(mainListShort(writingsIn)).appendLine().appendLine()
        text.append(mainListMedium(writingsIn) { it.containAnyOfTags("recommendation") })
            .appendLine().appendLine()
        text.append(mainListMedium(writingsIn) { !it.containAnyOfTags("fiction") }).appendLine()
            .appendLine()
        text.append(mainListMedium(writingsIn) { it.containAnyOfTags("fiction") }).appendLine()
            .appendLine()
        text.append(mainListLong(writingsIn))
        dstTestDir.resolve("library.txt").toFile().writeText(text.toString())
    }
}