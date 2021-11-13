/*
 * Copyright (C) 2013  Chicken-Bones
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package dev.su5ed.koremods.launch

import dev.su5ed.koremods.prelaunch.transform.ClassHierarchyManager.classExtends
import dev.su5ed.koremods.prelaunch.transform.ClassHierarchyManager.getSuperClass
import org.objectweb.asm.ClassWriter

/**
 * Credit: ChickenASM
 * 
 * See Also: [CC_ClassWriter](https://github.com/TheCBProject/CodeChickenLib/blob/1.11.2/src/main/java/codechicken/lib/asm/CC_ClassWriter.java)
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
