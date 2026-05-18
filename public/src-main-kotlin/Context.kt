import java.nio.file.Path

class Context(
    override val srcTxtDir: Path = UtilsMy.srcTxtDir,
    override val srcRawDir: Path = UtilsMy.srcRawDir,
    override val srcResDir: Path = UtilsMy.srcResDir,
    override val srcGenDir: Path = UtilsMy.srcGenDir,
    override val dstMainDir: Path = UtilsMy.dstMainDir,
    override val dstTestDir: Path = UtilsMy.dstTestDir) : ContextInterface