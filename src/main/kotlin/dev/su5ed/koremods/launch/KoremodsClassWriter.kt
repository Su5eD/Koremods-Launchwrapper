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
