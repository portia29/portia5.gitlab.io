import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.Test

/**
 * Command for set commit dates in Git repository:
 * - https://stackoverflow.com/a/454750/1305018
 */
class GitTest {

    val repo = UtilsMy.testResDir.resolve("gitrepo").toFile()

    @Test
    fun main() {
        if (!repo.exists()) return
        println(
            ratExec(
                repo,
                "git",
                "log",
                "--pretty=format:%ad - %an: %s",
                "--after=2000-01-01T00:00:00",
                "--until=2000-01-31T23:59:59",
                "--author=Dmitry Ratty"
            )
        )
        println(
            "\n" + ratExec(
                repo,
                "git",
                "log",
                "--pretty=format:%ad - %an: %s",
                "--after=2000-01-01T00:00:00 +0300",
                "--until=2000-01-31T23:59:59 +0400",
                "--author=Dmitry Ratty"
            )
        )
        println(
            "\n" + ratExec(
                repo,
                "git",
                "log",
                "--pretty=format:%ad - %an: %s",
                "--after=2000-01-01T00:00:00+03:00",
                "--until=2000-01-31T23:59:59+04:00",
                "--author=Dmitry Ratty"
            )
        )
    }

    @Test
    fun test1() {
        if (!repo.exists()) return
        val requestDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val startDay = YearMonth.of(1999, 12).atEndOfMonth()
        val startLocalDateTime = startDay.atTime(23, 59, 59).atOffset(ZoneOffset.ofHours(3))
        val since = requestDateFormatter.format(startLocalDateTime)

        val endDay = YearMonth.of(2000, 1).atEndOfMonth()
        val endLocalDateTime = endDay.atTime(23, 59, 59).atOffset(ZoneOffset.ofHours(3))
        val until = requestDateFormatter.format(endLocalDateTime)

        val requestResult = ratExec(
            repo,
            "git",
            "log",
            "--pretty=format:%ad - %an: %s",
            "--since=$since",
            "--until=$until",
            "--author=Dmitry Ratty"
        )
        if (requestResult!!.split('\n').size != 3) {
            throw IllegalStateException(requestResult)
        }
        println("\nSince $since, until $until\n$requestResult")
    }

    @Test
    fun test2() {
        if (!repo.exists()) return
        val requestDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val startDay = YearMonth.of(2000, 1).atDay(1)
        val startLocalDateTime = startDay.atStartOfDay().atOffset(ZoneOffset.ofHours(3))
        val since = requestDateFormatter.format(startLocalDateTime)

        val endDay = YearMonth.of(2000, 1).atEndOfMonth()
        val endLocalDateTime = endDay.atTime(23, 59, 59).atOffset(ZoneOffset.ofHours(3))
        val until = requestDateFormatter.format(endLocalDateTime)

        val requestResult = ratExec(
            repo,
            "git",
            "log",
            "--pretty=format:%cd - %an: %s",
            "--since=$since",
            "--until=$until",
            "--author=Dmitry Ratty"
        )
        if (requestResult!!.split('\n').size != 2) {
            throw IllegalStateException(requestResult)
        }
        println("\nSince $since, until $until\n$requestResult")
    }
}