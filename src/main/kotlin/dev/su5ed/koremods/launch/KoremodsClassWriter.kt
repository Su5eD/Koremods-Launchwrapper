package dev.su5ed.koremods.launch

import dev.su5ed.koremods.prelaunch.transform.ClassHierarchyManager.classExtends
import dev.su5ed.koremods.prelaunch.transform.ClassHierarchyManager.getSuperClass
import org.objectweb.asm.ClassWriter

/**
 * Credit: CodeChickenLib  
 * 
 * [CC_ClassWriter](https://github.com/TheCBProject/CodeChickenLib/blob/1.11.2/src/main/java/codechicken/lib/asm/CC_ClassWriter.java)
 * [ClassHierarchyManager](https://github.com/TheCBProject/CodeChickenLib/blob/1.11.2/src/main/java/codechicken/lib/asm/ClassHierarchyManager.java)
 */
internal class KoremodsClassWriter(writerFlags: Int) : ClassWriter(writerFlags) {
    
    override fun getCommonSuperClass(type1: String, type2: String): String {
        val c = type1.replace('/', '.')
        val d = type2.replace('/', '.')
        
        if (classExtends(d, c)) return type1
        else if (classExtends(c, d)) return type2
        return getParent(getSuperClass(c), d).replace('.', '/')
    }
    
    private fun getParent(type1: String, type2: String): String {
        return if (!classExtends(type2, type1)) getParent(getSuperClass(type1), type2)
        else type1
    }
}
