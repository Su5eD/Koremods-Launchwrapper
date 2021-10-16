package dev.su5ed.koremods.launch

import dev.su5ed.koremods.dsl.computeFrames
import dev.su5ed.koremods.transformClass
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.tree.ClassNode

private const val CLASS_WRITER_FLAGS = ASM5 or COMPUTE_MAXS
private const val CLASS_WRITE_FRAMES_FLAGS = CLASS_WRITER_FLAGS or COMPUTE_FRAMES

class KoremodsTransformer : IClassTransformer {
    
    override fun transform(name: String, transformedName: String, bytes: ByteArray): ByteArray {
        val reader = ClassReader(bytes)
        
        val node = ClassNode()
        reader.accept(node, 0)
        
        return transformClass(name, node)?.let { props ->
            
            val writer = if (props.computeFrames) KoremodsClassWriter(CLASS_WRITE_FRAMES_FLAGS) else ClassWriter(CLASS_WRITER_FLAGS)
            node.accept(writer)
            
            return@let writer.toByteArray()
        } ?: bytes
    }
}
