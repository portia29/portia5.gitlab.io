/*

import com.github.adriankuta.datastructure.tree.TreeNode
import kotlin.test.Test
import kotlin.test.assertEquals

*/

class TreeTest {

    /*
    // https://github.com/AdrianKuta/Tree-Data-Structure
    testImplementation("com.github.adriankuta:tree-structure:3.0.2")

    @Test fun test1() {
        val root = TreeNode("L1")
        val l21 = TreeNode("L2-1")
        val l22 = TreeNode("L2-2")
        root.addChild(l21)
        root.addChild(l22)
        val l31 = TreeNode("L3-1")
        l21.addChild(l31)
        val l32 = TreeNode("L3-2")
        val l33 = TreeNode("L3-3")
        l22.addChild(l32)
        l22.addChild(l33)
        val out = """
                    L1
                    ├── L2-1
                    │   └── L3-1
                    └── L2-2
                        ├── L3-2
                        └── L3-3
                    """.trimIndent()
        assertEquals(out, root.prettyString().trim())
    }

    @Test fun test2() {
        val root = TreeNode("L1")
        val l21 = TreeNode("L2-1")
        val l22one = TreeNode("L2-2")
        val l22two = TreeNode("L2-2")
        root.addChild(l21)
        root.addChild(l22one)
        root.addChild(l22two)
        val l31 = TreeNode("L3-1")
        l21.addChild(l31)
        val l32 = TreeNode("L3-2")
        val l33 = TreeNode("L3-3")
        l22one.addChild(l32)
        l22one.addChild(l33)
        val out = """
                    L1
                    ├── L2-1
                    │   └── L3-1
                    ├── L2-2
                    │   ├── L3-2
                    │   └── L3-3
                    └── L2-2
                    """.trimIndent()
        assertEquals(out, root.prettyString().trim())
    }

    @Test fun test3() {
        val root = TreeNode("L1")
        val l21 = TreeNode("L2-1")
        val l22one = TreeNode("L2-2")
        val l22two = TreeNode("L2-2")
        root.addChild(l21)
        root.addChild(l22one)
        root.addChild(l22two)
        val l31 = TreeNode("L3-1")
        l21.addChild(l31)
        val l32 = TreeNode("L3-2")
        val l33 = TreeNode("L3-3")
        l22one.addChild(l32)
        l22one.addChild(l33)
        assertEquals(true, root.removeChild(l22two))
        val out = """
                    L1
                    ├── L2-1
                    │   └── L3-1
                    └── L2-2
                        ├── L3-2
                        └── L3-3
                    """.trimIndent()
        assertEquals(out, root.prettyString().trim())
    }

    @Test fun test4() {
        val root = TreeNode("L1")
        val l21 = TreeNode("L2-1")
        val l22one = TreeNode("L2-2")
        val l22two = TreeNode("L2-2")
        root.addChild(l21)
        root.addChild(l22one)
        root.addChild(l22two)
        val l31 = TreeNode("L3-1")
        l21.addChild(l31)
        val l32 = TreeNode("L3-2")
        val l33 = TreeNode("L3-3")
        l22one.addChild(l32)
        l22one.addChild(l33)
        assertEquals(true, root.removeChild(l22two))
        assertEquals(false, root.removeChild(l22two))
        val out = """
                    L1
                    ├── L2-1
                    │   └── L3-1
                    └── L2-2
                        ├── L3-2
                        └── L3-3
                    """.trimIndent()
        assertEquals(out, root.prettyString().trim())
    }

    @Test fun test5() {
        val root = TreeNode("L1")
        val l21 = TreeNode("L2-1")
        val l22one = TreeNode("L2-2")
        val l22two = TreeNode("L2-2")
        root.addChild(l21)
        root.addChild(l22one)
        root.addChild(l22two)
        val l31 = TreeNode("L3-1")
        l21.addChild(l31)
        val l32 = TreeNode("L3-2")
        val l33 = TreeNode("L3-3")
        l22one.addChild(l32)
        l22one.addChild(l33)
        assertEquals(true, root.removeChild(l22two))
        assertEquals(false, root.removeChild(l22two))
        assertEquals(true, root.removeChild(l22one))
        val out = """
                    L1
                    └── L2-1
                        └── L3-1
                    """.trimIndent()
        assertEquals(out, root.prettyString().trim())
    }

    */
}