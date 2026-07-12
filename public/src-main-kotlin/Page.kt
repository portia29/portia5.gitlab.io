import java.util.Locale
import kotlin.io.path.readText

data class Page(val url: UrlMy) : UrlMyInterface by url {
    val raw: String = srcAbsolutePath.readText()
    val formatted: String = TextFormatter().transform(raw)
    // Also lenses.
    val chapters = mutableListOf<MutableList<MutableList<String>>>()

    private var _summaryParag: String? = null
    val summaryParag: String
        get() {
            initSummaryParag()
            return _summaryParag ?: throw IllegalStateException()
        }
    private fun initSummaryParag() {
        if (_summaryParag != null) {
            return
        }
        _summaryParag = ""
        when (chapters.size) {
            1, 2 -> {
                if (chapters[0].size == 1 && chapters[0][0].size == 1) {
                    _summaryParag = chapters[0][0][0]
                }
            }
            3 -> {
                if (chapters[0].size != 1 || chapters[0][0].size != 1) {
                    throw IllegalStateException()
                }
                _summaryParag = chapters[0][0][0]
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }
    fun summaryParag(link: Boolean): String {
        return if (summaryParag.isNotEmpty() && link) {
            "${summaryParag}\n - $absoluteUrl"
        } else {
            summaryParag
        }
    }
    
    private var _summarySection: MutableList<String>? = null
    val summarySection: MutableList<String>
        get() {
            initSummarySection()
            return _summarySection ?: throw IllegalStateException()
        }
    private fun initSummarySection() {
        if (_summarySection != null) {
            return
        }
        _summarySection = mutableListOf()
        when (chapters.size) {
            1 -> {
                if (chapters[0].size == 1 && chapters[0][0].size > 1) {
                    _summarySection = chapters[0][0].toMutableList()
                }
            }
            2 -> {
                if (chapters[0].size == 1 && chapters[0][0].size > 1) {
                    _summarySection = chapters[0][0].toMutableList()
                } else if (chapters[1].size == 1) {
                    _summarySection = chapters[1][0].toMutableList()
                }
            }
            3 -> {
                if (chapters[1].size != 1) throw IllegalStateException()
                _summarySection = chapters[1][0].toMutableList()
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }
    fun summarySection(link: Boolean): MutableList<String> {
        return if (summarySection.isNotEmpty() && link) {
            val result = summarySection.toMutableList()
            result.add(absoluteUrl)
            result
        } else {
            summarySection
        }
    }

    private var _summaryFull: MutableList<MutableList<String>>? = null
    val summaryFull: MutableList<MutableList<String>>
        get() {
            initSummaryFull()
            return _summaryFull ?: throw IllegalStateException()
        }
    private fun initSummaryFull() {
        if (_summaryFull != null) {
            return
        }
        _summaryFull = mutableListOf()
        when (chapters.size) {
            1 -> {
                if (chapters[0].size > 1) {
                    _summaryFull = chapters[0].map { it.toMutableList() }.toMutableList()
                }
            }
            2 -> {
                if (chapters[1].size > 1) {
                    _summaryFull = chapters[1].map { it.toMutableList() }.toMutableList()
                }
            }
            3 -> {
                _summaryFull = chapters[2].map { it.toMutableList() }.toMutableList()
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    var includeText: String = ""
    var beautyText: String = ""

    val navigation = !isRoot

    private var _title: String? = null
    val title: String
        get() {
            initTitle()
            return _title ?: throw IllegalStateException()
        }
    private fun initTitle() {
        if (_title != null) {
            return
        }
        if (isRoot) {
            _title = "Well… Yes!"
            return
        }
        var name = relativeUrl.removePrefix("/")
        if (name.contains('/')) {
            _title = "Well… \"$name\"!"
        } else {
            name = name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
            _title = "Well… $name!"
        }
    }
}