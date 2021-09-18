package dev.su5ed.koremods.launch

import dev.su5ed.koremods.transformClass
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class KoremodsTransformer : IClassTransformer {
    
    override fun transform(name: String, transformedName: String, bytes: ByteArray): ByteArray {
        val reader = ClassReader(bytes)
        
        val node = ClassNode()
        reader.accept(node, 0)
        transformClass(name, node)
        
        val writer = ClassWriter(Opcodes.ASM5)
        node.accept(writer)
        
        return writer.toByteArray()
    }
}
