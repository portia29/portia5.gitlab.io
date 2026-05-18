import java.nio.file.Path

interface UrlMyInterface {
    val srcAbsolutePath: Path
    val srcRelativePath: Path
    val dstDirPath: Path
    val srcAbsolutePathString: String
    val srcRelativePathString: String
    val relativeUrl: String
    val absoluteUrl: String
    val dstRelativePath: Path
    val dstRelativePathString: String
    val dstAbsolutePath: Path
    val redirects: Set<String>
    val isPage: Boolean
    val isDirectory: Boolean
    val isRoot: Boolean
    val isRaw: Boolean
    val isMap: Boolean
    val isGen: Boolean
    val isIndexOfDirectory: Boolean
}