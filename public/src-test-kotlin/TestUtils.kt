import java.nio.file.Path

object TestUtils {
    private const val DUMMY_RAT_URL = "index.html"
    private val DUMMY_DST_DIR: Path = UtilsMy.testResDir.resolve("dst")
    val url = UrlMy(
        UtilsMy.testResDir.resolve(DUMMY_RAT_URL),
        Path.of(DUMMY_RAT_URL),
        DUMMY_DST_DIR)
}