
import UtilsMy.dstTestDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.util.LinkedList
import java.util.function.Predicate

/**
 * Novel - роман.
 */
class Notes {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Notes().main()
        }
    }

    interface FiltrableWriting {
        fun hasAnyOfTags(vararg tags: String): Boolean
    }

    @Serializable
    data class Name(
        val name: String,
        val language: String,
        val link: String? = null,
        val comment: String? = null
    )

    @Serializable
    data class Author(val names: List<Name>) {
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
    data class Note(
        val raw: String? = null,
        val comment: String? = null,
        val names: List<Name> = mutableListOf(),
        val tags: Set<String>,
        val authors: MutableList<Author> = mutableListOf(),
        val created: String? = null
    ) : FiltrableWriting {
        override fun hasAnyOfTags(vararg tags: String): Boolean {
            for (tag in tags) if (this.tags.contains(tag)) return true
            return false
        }
    }

    val defaultLang = "en"

    private fun selectName(names: List<Name>, language: String): String {
        var name = names.first().name
        if (names.size > 1) {
            names.forEach {
                if (it.language == language) name = it.name
            }
        }
        return name
    }

    @Suppress("SameParameterValue")
    private fun formatWritings(notes: List<Note>, language: String): String {
        return notes.joinToString(
            separator = "», «", prefix = ": «", postfix = "».", transform = {
                selectName(it.names, language)
            })
    }

    @Suppress("SameParameterValue")
    private fun formatAuthors(authors: List<Author>, language: String): String {
        return selectName(authors[0].names, language)
    }

    @Suppress("unused")
    fun notesGrouped(notes: List<Note>): List<String> {
        val result = mutableListOf<String>()
        val length = notes.size
        var index = 0
        val addNoteNumber = false
        fun drain(s: StringBuilder, p: Predicate<Note>): LinkedList<Note> {
            if (addNoteNumber) s.append("").append(result.size + 1).append(". ")
            val list = LinkedList<Note>()
            while (index < length) {
                val n = notes[index]
                if (!p.test(n)) {
                    break
                }
                list.add(n)
                index++
            }
            return list
        }
        while (index < length) {
            val n = notes[index]
            index++
            val text = StringBuilder()
            if (n.hasAnyOfTags("raw")) {
                if (n.hasAnyOfTags("wikipedia")) {
                    val list = drain(text, p = { t -> t.hasAnyOfTags("wikipedia") })
                    list.addFirst(n)
                    val t = StringBuilder()
                    list.forEach {
                        if (t.isNotEmpty()) t.append(" ")
                        t.append(it.raw)
                        it.raw?.endsWith('.')?.let { endsWithDot ->
                            if (!endsWithDot) {
                                t.append(".")
                            }
                        }
                    }
                    text.append(t)
                } else {
                    drain(text, p = { t -> false })
                    text.append(n.raw)
                }
            } else {
                if (n.hasAnyOfTags("tv-series")) {
                    val list = drain(text, p = { t -> t.hasAnyOfTags("tv-series") })
                    list.addFirst(n)
                    text.append("Television series")
                    text.append(formatWritings(list, defaultLang))
                } else if (n.hasAnyOfTags("anime")) {
                    val list = drain(text, p = { t -> t.hasAnyOfTags("anime") })
                    list.addFirst(n)
                    text.append("Anime")
                    text.append(formatWritings(list, defaultLang))
                } else {
                    val list = drain(text, p = { t -> t.authors == n.authors })
                    list.addFirst(n)
                    text.append(formatAuthors(list[0].authors, defaultLang))
                    text.append(formatWritings(list, defaultLang))
                }
            }
            result.add(text.toString())
        }
        return result
    }

    fun main() {
        val writingsIn = loadNotes(UtilsMy.projectDir.parent.resolve("private/src-main-res"))
        val libraryOut = UtilsMy.srcGenDir
        val notes = notesGrouped(writingsIn)
        val text = StringBuilder()
        val bs = "█ "
        val be = ""
        text.append(notes.subList(0, 100).joinToString("$be $bs", bs, be))
        libraryOut.resolve("notes-preview.txt").toFile().writeText(text.toString())
    }

    fun saveNotes(dst: Path, writingsIn: MutableList<Note>) {
        val format = Json { prettyPrint = true }
        val outWritingsFile = dst.resolve("notes.json").toFile()
        outWritingsFile.writeText(format.encodeToString(writingsIn))

    }

    fun loadNotes(srcDir: Path): MutableList<Note> {
        val writingsFile = srcDir.resolve("notes.json").toFile()
        if (!writingsFile.exists()) return emptyList<Note>().toMutableList()
        val notes = Json.decodeFromString<MutableList<Note>>(writingsFile.readText())
        saveNotes(srcDir, notes)
        return notes
    }

    @Suppress("unused")
    fun archivedCode(writingsIn: MutableList<Note>) {
        fun mainListLong(writingsIn: MutableList<Note>): StringBuilder {
            fun recommendationFilter(w: FiltrableWriting): Boolean {
                return w.hasAnyOfTags(
                    "recommendation", "visible"
                ) && !w.hasAnyOfTags("invisible")
            }

            fun entertainingFilter(w: FiltrableWriting): Boolean {
                return w.hasAnyOfTags("entertaining") && !w.hasAnyOfTags("invisible")
            }

            val b = StringBuilder()
            val fullList = LinkedList<Note>()
            fullList.addAll(writingsIn.filter { recommendationFilter(it) })
            fullList.addAll(writingsIn.filter { entertainingFilter(it) })
            val currentList = LinkedList<Note>()
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
                writingsIn.filter { !mainListAuthors.contains(it.authors) }
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
            writingsIn: MutableList<Note>, predicate: (Note) -> Boolean
        ): StringBuilder {
            val b = StringBuilder()
            val list = writingsIn.filter { predicate(it) }
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
        fun mainListShort(writingsIn: MutableList<Note>): StringBuilder {
            val b = StringBuilder()
            val list = writingsIn.filter {
                it.hasAnyOfTags(
                    "recommendation", "visible"
                ) && !it.hasAnyOfTags("invisible")
            }.groupBy { it.authors }.keys.toList()
            for (i in 0..9) {
                if (i != 0) {
                    b.append(", ")
                }
                b.append(formatAuthors(list[i], defaultLang))
            }
            b.append(".")
            return b
        }
        fun mainListSummary(writingsIn: MutableList<Note>): StringBuilder {
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
        text.append(mainListMedium(writingsIn) { it.hasAnyOfTags("recommendation") })
            .appendLine().appendLine()
        text.append(mainListMedium(writingsIn) { !it.hasAnyOfTags("fiction") }).appendLine()
            .appendLine()
        text.append(mainListMedium(writingsIn) { it.hasAnyOfTags("fiction") }).appendLine()
            .appendLine()
        text.append(mainListLong(writingsIn))
        dstTestDir.resolve("library.txt").toFile().writeText(text.toString())
        fun generateListSplitByParts() {
            val writingsIn = loadNotes(UtilsMy.srcResDir)
            val libraryOut = UtilsMy.srcGenDir
            val notes = notesGrouped(writingsIn)
            val partSize = 100
            var partCount = 0
            var partIndex = 0
            while (partIndex < notes.size) {
                partCount++
                partIndex += partSize
                val text = StringBuilder()
                text.append("Коллекция заметок, часть № $partCount")
                text.append(", заметки с ${partIndex - partSize + 1} по $partIndex, заметок всего ${notes.size}. ")
                val fromIndex = partIndex - partSize
                val toIndex = if (partIndex < notes.size) partIndex else notes.size
                val bs = "⟨"
                val be = "⟩"
                text.append(notes.subList(fromIndex, toIndex).joinToString("$be $bs", bs, be))
                libraryOut.resolve("library$partCount.txt").toFile().writeText(text.toString())
            }
        }
    }
}