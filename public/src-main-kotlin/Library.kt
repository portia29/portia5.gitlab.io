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

    private fun mainList(writingsIn: MutableList<Writing>, groupByAuthor: Boolean): StringBuilder {
        val b = StringBuilder()
        if (groupByAuthor) {
            writingsIn.filter { recommendationFilter(it) }.sortedBy { it.rating }
                .groupBy { it.authors }.forEach { (authors, writings) ->
                    if (b.isNotEmpty()) {
                        b.append(" ")
                    }
                    b.append(formatAuthors(authors, "ru"))
                    b.append(formatWritings(writings, "ru"))
                }
            writingsIn.filter { entertainingFilter(it) }.sortedBy { it.rating }
                .groupBy { it.authors }.forEach { (authors, writings) ->
                    if (b.isNotEmpty()) {
                        b.append(" ")
                    }
                    b.append(formatAuthors(authors, "ru"))
                    b.append(formatWritings(writings, "ru"))
                }
        } else {
            val fullList = LinkedList<Writing>()
            fullList.addAll(writingsIn.filter { recommendationFilter(it) }.sortedBy { it.rating })
            fullList.addAll(writingsIn.filter { entertainingFilter(it) }.sortedBy { it.rating })
            val currentList = LinkedList<Writing>()
            fullList.forEach { w ->
                if (currentList.isNotEmpty() && currentList.first().authors != w.authors) {
                    if (b.isNotEmpty()) {
                        b.append(" ")
                    }
                    b.append(formatAuthors(currentList.first().authors, "ru"))
                    b.append(formatWritings(currentList, "ru"))
                    currentList.clear()
                }
                currentList.add(w)
            }
            if (currentList.isNotEmpty()) {
                if (b.isNotEmpty()) {
                    b.append(" ")
                }
                b.append(formatAuthors(currentList.first().authors, "ru"))
                b.append(formatWritings(currentList, "ru"))
                currentList.clear()
            }
        }
        return b
    }

    fun main() {
        val authors = loadAuthors(UtilsAbsolute.srcResDir)
        val writingsIn = loadWritings(UtilsAbsolute.srcResDir, authors)
        val libraryOut = UtilsAbsolute.srcGenDir
        val builder = StringBuilder("Коллекция прочитанного. </> ")
        builder.append(mainList(writingsIn, false))
        builder.append("\n\n")
        builder.append("Ещё я читал этих авторов. </> ")
        val mainListAuthors = writingsIn.filter {
            recommendationFilter(it) || entertainingFilter(it)
        }.groupBy { it.authors }.keys
        val authorsList =
            writingsIn.filter { !mainListAuthors.contains(it.authors) }.sortedBy { it.rating }
                .groupBy { it.authors }.keys.toMutableList()
        builder.append(
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
        libraryOut.resolve("library.txt").toFile().writeText(builder.toString())
    }

    private fun printCount(inputWritings: List<Writing>) {
        val novelCount = inputWritings.count { it.tags.contains("novel") }
        val recommendation = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("recommendation")
        }
        val entertaining = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("entertaining")
        }
        val archive = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("archive")
        }
        val chaos = inputWritings.count {
            it.tags.contains("novel") && it.tags.contains("chaos")
        }
        if ((recommendation + entertaining + archive + chaos) != novelCount) {
            throw IllegalStateException()
        }
        // Total novels 163, listed 128, unlisted 35.
        // Recommendation 10, entertaining 0, archive 109, chaos 9.
        val unlisted = 35
        print("Total novels ${novelCount + unlisted}, listed $novelCount, unlisted $unlisted.")
        print(" ")
        print("Recommendation $recommendation, entertaining $entertaining,")
        print(" ")
        print("archive $archive, chaos $chaos.")
        print("\n")
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

        val rest = writings.filter {
            !articlesFileFilter(it)
                    && !otherFileFilter(it) && !chaosFileFilter(it)
        }
        if (rest.isNotEmpty()) throw IllegalStateException()
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

    fun articlesFileFilter(w: FiltrableWriting): Boolean {
        return w.containTags("essay", "blogging") && !w.containTags("chaos")
    }

    fun otherFileFilter(w: FiltrableWriting): Boolean {
        return (!w.containTags("essay") && !w.containTags("blogging")) && !w.containTags("chaos")
    }

    fun chaosFileFilter(w: FiltrableWriting): Boolean {
        return w.containTags("chaos")
    }

    fun recommendationFilter(w: FiltrableWriting): Boolean {
        return w.containTags("recommendation")
    }

    fun entertainingFilter(w: FiltrableWriting): Boolean {
        return w.containTags("entertaining")
    }
}