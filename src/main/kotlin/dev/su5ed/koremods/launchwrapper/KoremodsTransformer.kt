/*
 * This file is part of Koremods, licensed under the MIT License
 *
 * Copyright (c) 2021 Garden of Fancy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.su5ed.koremods.launchwrapper

import dev.su5ed.koremods.dsl.TransformerPropertiesExtension
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
    
    override fun transform(name: String, transformedName: String, bytes: ByteArray?): ByteArray? {
        if (bytes == null) return bytes
        
        val reader = ClassReader(bytes)
        val node = ClassNode()
        reader.accept(node, 0)
        
        return transformClass(name, node).let { props ->
            val computeFrames = props.any(TransformerPropertiesExtension::computeFrames)
            val writer = if (computeFrames) KoremodsClassWriter(CLASS_WRITE_FRAMES_FLAGS) else ClassWriter(CLASS_WRITER_FLAGS)
            node.accept(writer)

            return@let writer.toByteArray()
        } ?: bytes
    }
}
