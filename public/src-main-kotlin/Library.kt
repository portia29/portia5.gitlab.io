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
        fun containTags(vararg tags: String): Boolean
    }

    @Serializable
    data class Name(val name: String, val language: String, val comment: String? = null)

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
        override fun containTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

    @Serializable
    data class WritingRecord(
        val names: List<Name>, val authors: MutableList<String>, val tags: Set<String>,
        val rating: Int
    ) : FiltrableWriting {
        override fun containTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

    val defaultLang = "en"

    private fun mainListLong(writingsIn: MutableList<Writing>): StringBuilder {
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
                        separator = ", ",
                        prefix = "",
                        postfix = "",
                        transform = { author ->
                            author.names.first().name
                        })
                })
        )
        return b
    }

    private fun mainListMedium(writingsIn: MutableList<Writing>, predicate: (Writing) -> Boolean): StringBuilder {
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

    private fun mainListShort(writingsIn: MutableList<Writing>): StringBuilder {
        val b = StringBuilder()
        val list = writingsIn.filter { recommendationFilter(it) }.sortedBy { it.rating }
        for (i in 0..9) {
            val w = list[i]
            if (i != 0) {
                b.append(", ")
            }
            b.append(formatAuthors(w.authors, defaultLang))
        }
        b.append(".")
        return b
    }

    private fun mainListSummary(writingsIn: MutableList<Writing>): StringBuilder {
        val b = StringBuilder()
        val writingsCount = writingsIn.size
        val authorsCount = writingsIn.groupBy { it.authors }.keys.size
        b.append("Коллекция штук, прочитанных мной," +
                " штук всего $writingsCount, авторов всего $authorsCount.")
        b.append(" ").append("Коллекция авторов, которых я читал, авторов всего $authorsCount," +
                " произведений всего $writingsCount.")
        return b
    }

    fun main() {
        val authors = loadAuthors(UtilsAbsolute.srcResDir)
        val writingsIn = loadWritings(UtilsAbsolute.srcResDir, authors)
        val libraryOut = UtilsAbsolute.srcGenDir
        val text = StringBuilder()
        text.append(mainListSummary(writingsIn)).appendLine().appendLine()
        text.append(mainListShort(writingsIn)).appendLine().appendLine()
        //text.append(mainListMedium(writingsIn) { recommendationFilter(it) }).append("\n\n")
        text.append(mainListMedium(writingsIn) { !it.containTags("fiction") })
        text.appendLine().appendLine()
        text.append(mainListMedium(writingsIn) { it.containTags("fiction") })
        text.appendLine().appendLine()
        text.append(mainListLong(writingsIn))
        libraryOut.resolve("library.txt").toFile().writeText(text.toString())
    }

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

    fun saveLibrary(
        dst: Path,
        authorsMap: MutableMap<String, Author>,
        writingsIn: MutableList<Writing>
    ) {
        val format = Json { prettyPrint = true }
        val outAuthorsFile = dst.resolve("library-authors.json").toFile()
        outAuthorsFile.writeText(format.encodeToString(authorsMap.values.toList()))

        val writings = mutableListOf<WritingRecord>()
        writingsIn.forEach { w ->
            val a = w.authors.map { it.id() }.toMutableList()
            writings.add(WritingRecord(w.names, a, w.tags, w.rating))
        }

        val writingsToSave = writings.sortedBy { it.rating }
        val outWritingsFile = dst.resolve("library-writings.json").toFile()
        outWritingsFile.writeText(format.encodeToString(writingsToSave))
    }

    fun loadAuthors(srcDir: Path): MutableMap<String, Author> {
        val authorsIn: List<Author> = Json.decodeFromString<List<Author>>(
            srcDir.resolve("library-authors.json").toFile().readText()
        )
        val authorsMap = mutableMapOf<String, Author>()
        authorsIn.forEach {
            authorsMap[it.id()] = it
        }
        return authorsMap
    }

    fun loadWritings(srcDir: Path, authorsMap: MutableMap<String, Author>): MutableList<Writing> {
        val writingsFile = srcDir.resolve("library-writings.json").toFile()
        val writingsRecords = Json.decodeFromString<List<WritingRecord>>(writingsFile.readText())
        val writings = mutableListOf<Writing>()
        writingsRecords.forEach { writingRecord ->
            val authors = mutableListOf<Author>()
            writingRecord.authors.forEach { authorId ->
                var authorById = authorsMap[authorId]
                if (authorById == null) {
                    // For case when author ID changed and to match writing with
                    // its author we perform search in authors using all ID variations
                    // of each author. Change of author ID may occur when we add author
                    // name in another language.
                    authorsMap.values.forEach { authorFromMap ->
                        if (authorFromMap.names.find { it.name == authorId } != null) {
                            authorById = authorFromMap
                            println("Author ID changed from $authorId to ${authorById.id()}")
                        }
                    }
                    if (authorById == null) {
                        val e = "$writingRecord - $authorId"
                        throw IllegalStateException(e)
                    }
                }
                authors.add(authorById)
            }
            writings.add(
                Writing(
                    writingRecord.names,
                    authors,
                    writingRecord.tags,
                    writingRecord.rating
                )
            )
        }
        saveLibrary(srcDir, authorsMap, writings)
        return writings
    }

    fun recommendationFilter(w: FiltrableWriting): Boolean {
        return w.containTags("recommendation", "visible") && !w.containTags("invisible")
    }

    fun entertainingFilter(w: FiltrableWriting): Boolean {
        return w.containTags("entertaining") && !w.containTags("invisible")
    }
}