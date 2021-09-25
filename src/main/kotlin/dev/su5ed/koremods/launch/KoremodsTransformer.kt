package dev.su5ed.koremods.launch

import dev.su5ed.koremods.transformClass
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.tree.ClassNode

class KoremodsTransformer : IClassTransformer {
    
    override fun transform(name: String, transformedName: String, bytes: ByteArray): ByteArray {
        val reader = ClassReader(bytes)
        
        val node = ClassNode()
        reader.accept(node, 0)
        val flags = transformClass(name, node)
        
        val writer = createClassWriter(ASM5 or COMPUTE_MAXS or flags)
        node.accept(writer)
        
        return writer.toByteArray()
    }
}
