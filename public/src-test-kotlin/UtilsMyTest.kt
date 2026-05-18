import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilsMyTest {
    @Test fun test() {
        assertEquals("Hello!", ratExecSimple(UtilsMy.currentPath.toFile(), "echo Hello!"))
        val git = ratExecSimple(UtilsMy.currentPath.toFile(), "git --version")
        assertTrue { git!!.startsWith("git version ") }
    }
}