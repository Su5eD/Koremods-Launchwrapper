package dev.su5ed.koremods.launch

import org.objectweb.asm.ClassWriter

internal fun createClassWriter(writerFlags: Int): ClassWriter {
    return if (writerFlags and ClassWriter.COMPUTE_FRAMES != 0) KoremodsClassWriter(writerFlags) 
    else ClassWriter(writerFlags)
}

// Credit: CodeChickenLib

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
