import UtilsAbsolute.testResDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

class LibraryTest {
    private val resLibrary: Path = testResDir.resolve("library")
    private val resMigration: Path = resLibrary.resolve("migration")
    private val resIn: Path = resMigration.resolve("in")
    private val resOut: Path = resMigration.resolve("out")
    private val format = Json { prettyPrint = true }

    @Serializable
    data class Name(
        val name: String, val language: String,
        val link: String? = null, val comment: String? = null
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
        val names: List<Name>, val authors: MutableList<Author>, val tags: Set<String>,
        val rating: Int
    )

    @Serializable
    data class WritingRecord(
        val names: List<Name>, val authors: MutableList<String>, val tags: Set<String>,
        val rating: Int
    )

    fun articlesFileFilter(w: Writing): Boolean {
        return (w.tags.contains("essay") || w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun otherFileFilter(w: Writing): Boolean {
        return (!w.tags.contains("essay") && !w.tags.contains("blogging"))
                && !w.tags.contains("chaos")
    }

    fun chaosFileFilter(w: Writing): Boolean {
        return w.tags.contains("chaos")
    }

    fun loadWritings1(srcDir: Path): MutableList<Writing> {
        val writingsIn: MutableList<Writing> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<Writing>>(it.toFile().readText()))
        }
        return writingsIn
    }

    fun writeWritings1(dst: Path, writings: List<Writing>) {
        val articlesToSave = writings.filter { articlesFileFilter(it) }.sortedBy { it.rating }
        val outArticleFile = dst.resolve("library-article.json").toFile()
        outArticleFile.writeText(format.encodeToString(articlesToSave))

        val othersToSave = writings.filter { otherFileFilter(it) }.sortedBy { it.rating }
        val outOtherFile = dst.resolve("library-other.json").toFile()
        outOtherFile.writeText(format.encodeToString(othersToSave))

        val chaosToSave = writings.filter { chaosFileFilter(it) }.sortedBy { it.rating }
        val outChaosFile = dst.resolve("library-chaos.json").toFile()
        outChaosFile.writeText(format.encodeToString(chaosToSave))

        val rest = writings.filter {
            !articlesFileFilter(it)
                    && !otherFileFilter(it) && !chaosFileFilter(it)
        }
        if (rest.isNotEmpty()) throw IllegalStateException()
    }

    fun migration(pathIn: Path, pathOut: Path) {
        val writings = loadWritings1(pathIn)
        writeWritings1(pathIn, writings)
        // Extract authors.
        val authors = mutableListOf<Author>()
        writings.forEach { authors.addAll(it.authors) }
        // Normalize authors.
        authors.sortBy { it.id() + it.names.size }
        val authorsMap = mutableMapOf<String, Author>()
        authors.forEach {
            val key = it.id()
            val duplicate = authorsMap[key]
            if (duplicate == null) {
                authorsMap[it.id()] = it
            } else {
                duplicate.names.addAll(it.names)
            }
        }
        //saveLibrary(pathOut, authorsMap, writings)
    }

    @Test
    fun migration() {
        //migration(resIn, resOut)
        //migration(UtilsAbsolute.srcResDir, UtilsAbsolute.srcResDir)
    }

    fun loadAuthors(srcDir: Path): MutableMap<String, Author> {
        val authorsIn: List<Author> = Json.decodeFromString<List<Author>>(
            srcDir.resolve("authors.json").toFile().readText()
        )
        val authorsMap = mutableMapOf<String, Author>()
        authorsIn.forEach {
            authorsMap[it.id()] = it
        }
        return authorsMap
    }

    fun loadWritings(srcDir: Path, authorsMap: MutableMap<String, Author>): MutableList<Writing> {
        val writingsIn: MutableList<WritingRecord> = arrayListOf()
        srcDir.listDirectoryEntries("library*.json").forEach {
            writingsIn.addAll(Json.decodeFromString<List<WritingRecord>>(it.toFile().readText()))
        }
        val writings = mutableListOf<Writing>()
        writingsIn.forEach { writingIn ->
            val authors = mutableListOf<Author>()
            writingIn.authors.forEach {
                authors.add(authorsMap[it]!!)
            }
            writings.add(Writing(writingIn.names, authors, writingIn.tags, writingIn.rating))
        }
        // Next is for case when author ID changed and to match writing with
        // its author we perform search in authors using all ID variations
        // of each author. Change of author ID may occur when we add author
        // name in another language.
        writings.forEach { writing ->
            writing.authors.forEachIndexed { i, author ->
                if (authorsMap[author.id()] == null) {
                    var newAuthor: Author? = null
                    authorsMap.values.forEach { authorFromMap ->
                        author.names.forEach { an ->
                            if (authorFromMap.names.contains(an)) {
                                newAuthor = authorFromMap
                            }
                        }
                    }
                    if (newAuthor == null) throw IllegalStateException(author.toString())
                    writing.authors[i] = newAuthor!!
                }
            }
        }
        return writings
    }

    @Test
    fun migrationTest() {
        val authors = loadAuthors(resOut)
        val writings = loadWritings(resOut, authors)
        //saveLibrary(resOut, authors, writings)
    }
}