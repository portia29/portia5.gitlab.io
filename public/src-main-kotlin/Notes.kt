
import UtilsMy.dstTestDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.util.LinkedList
import java.util.Locale
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

    interface FiltrableNote {
        fun hasAnyOfTags(vararg tags: String): Boolean
    }

    @Serializable
    data class Name(
        var name: String,
        val language: String
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
        var comment: String? = null,
        val names: List<Name> = mutableListOf(),
        val tags: Set<String>,
        val authors: MutableList<Author> = mutableListOf()
    ) : FiltrableNote {
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
            separator = "», «", prefix = " «", postfix = "».", transform = {
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
        fun drain(p: Predicate<Note>): LinkedList<Note> {
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
        fun appendByTag(b: StringBuilder, n: Note, tag: String): Boolean {
            if (!n.hasAnyOfTags(tag)) {
                return false
            }
            val list = drain(p = { t -> t.hasAnyOfTags(tag) })
            list.addFirst(n)
            val t = StringBuilder()
            t.append("[${tag.uppercase(Locale.US)}]")
            if (n.hasAnyOfTags("raw")) {
                list.forEach {
                    t.append(" ").append(it.raw)
                    it.raw?.endsWith('.')?.let { endsWithDot ->
                        if (!endsWithDot) {
                            t.append(".")
                        }
                    }
                }
            } else {
                t.append(formatWritings(list, defaultLang))
            }
            b.append(t)
            return true
        }
        while (index < length) {
            val n = notes[index]
            index++
            val text = StringBuilder()
            var appendByTag = false
            listOf("wikipedia", "anime", "film", "tv-series").forEach { tag ->
                if (appendByTag(text, n, tag)) {
                    appendByTag = true
                    return@forEach
                }
            }
            if (!appendByTag) {
                if (n.hasAnyOfTags("raw")) {
                    drain(p = { t -> false })
                    text.append(n.raw)
                } else {
                    val list = drain(p = { t -> t.authors == n.authors })
                    list.addFirst(n)
                    text.append(formatAuthors(list[0].authors, defaultLang))
                    text.append(":")
                    text.append(formatWritings(list, defaultLang))
                }
            }
            result.add(text.toString())
        }
        return result
    }

    fun saveTest(writingsIn: MutableList<Note>) {
        val outFile = UtilsMy.projectDir.parent.resolve("private/src-test-res/notes-full.txt")
        val text = StringBuilder()
        val notes = notesGrouped(writingsIn)
        notes.forEach { note ->
            if (text.isNotEmpty()) text.append(" ")
            text.append("█ ").append(note)
        }
        outFile.toFile().writeText(text.toString())
    }

    fun main() {
        val writingsIn = loadNotes(UtilsMy.projectDir.parent.resolve("private/src-main-res"))
        saveTest(writingsIn)
        val notesOut = UtilsMy.srcGenDir
        var separatorHit = false
        val notes = notesGrouped(writingsIn.filter {
            if (separatorHit) {
                return@filter false
            }
            if (it.hasAnyOfTags("separator")) {
                separatorHit = true
                return@filter false
            }
            true
        })
        val text = StringBuilder()
        val bs = "█ "
        val be = ""
        text.append(notes.joinToString("$be $bs", bs, be))
        notesOut.resolve("notes-public.txt").toFile().writeText(text.toString())
    }

    val notesSeparator = "█"
    val noteSeparator = "<>\n"

    fun saveNotes(dst: Path, writingsIn: MutableList<Note>) {
        val format = Json { prettyPrint = false }
        val outWritingsFile = dst.resolve("Notes.txt").toFile()
        val sb = StringBuilder()
        writingsIn.forEach {
            if (sb.isNotEmpty()) sb.append("\n\n")
            val comment = it.comment
            it.comment = null
            val n = format.encodeToString(it)
            if (n.contains(notesSeparator)) throw IllegalStateException(n)
            if (n.contains(noteSeparator)) throw IllegalStateException(n)
            sb.append("$notesSeparator ").append(n.subSequence(1, n.length - 1))
            if (comment != null) {
                if (comment.contains(notesSeparator)) throw IllegalStateException(n)
                if (comment.contains(noteSeparator)) throw IllegalStateException(n)
                sb.append("\n").append(noteSeparator).append(comment.trim())
            }
        }
        outWritingsFile.writeText(sb.toString())
    }

    fun loadNotes(srcDir: Path): MutableList<Note> {
        val writingsFile = srcDir.resolve("Notes.txt").toFile()
        if (!writingsFile.exists()) return emptyList<Note>().toMutableList()
        val strings = writingsFile.readText().split("$notesSeparator ")
        val notes = strings.filter { it.isNotBlank() }.map { noteString ->
            val parts = noteString.split("\n<>\n")
            val note = Json.decodeFromString<Note>("{${parts[0]}}")
            if (parts.size > 1) {
                note.comment = parts[1]
            }
            /*
            note.names.forEach {
                if (it.link != null) {
                    val t = "Main link ${it.language.uppercase(Locale.US)}: ${it.link}"
                    if (note.comment == null) {
                        note.comment = t
                    } else {
                        note.comment += "\n\n" + t
                    }
                    it.link = null
                }
            }
            */
            note
        }.toMutableList()
        saveNotes(srcDir, notes)
        return notes
    }

    @Suppress("unused")
    fun archivedCode(writingsIn: MutableList<Note>) {
        fun mainListLong(writingsIn: MutableList<Note>): StringBuilder {
            fun recommendationFilter(w: FiltrableNote): Boolean {
                return w.hasAnyOfTags("recommendation")
            }
            val b = StringBuilder()
            val fullList = LinkedList<Note>()
            fullList.addAll(writingsIn.filter { recommendationFilter(it) })
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
            val mainListAuthors = writingsIn.filter { recommendationFilter(it) }
                .groupBy { it.authors }.keys
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
        text.append(mainListLong(writingsIn))
        dstTestDir.resolve("library.txt").toFile().writeText(text.toString())
    }
}