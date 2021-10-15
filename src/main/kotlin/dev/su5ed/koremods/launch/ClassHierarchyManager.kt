package dev.su5ed.koremods.launch

import net.minecraft.launchwrapper.IClassTransformer
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.objectweb.asm.ClassReader
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

class ClassHierarchyManager : IClassTransformer {
    override fun transform(name: String, transformedName: String, bytes: ByteArray?): ByteArray? {
        return bytes?.let {
            if (!superclasses.containsKey(transformedName)) {
                declareASM(it)
            }
            return it
        }
    }
}

class SuperCache {
    var superclass: String? = null
    var parents: MutableSet<String> = mutableSetOf()
    private var flattened = false

    fun add(parent: String) {
        parents.add(parent)
    }

    fun flatten() {
        if (!flattened) {
            ArrayList(parents).forEach { s ->
                declareClass(s)?.let { c ->
                    c.flatten()
                    parents.addAll(c.parents)
                }
            }
            flattened = true
        }
    }
}

private val superclasses: MutableMap<String, SuperCache> = mutableMapOf()

private fun toKey(name: String): String {
    return if (KoremodsSetup.runtimeDeobfuscationEnabled) FMLDeobfuscatingRemapper.INSTANCE.map(name.replace('.', '/')).replace('/', '.')
    else name
}

private fun unKey(name: String): String {
    return if (KoremodsSetup.runtimeDeobfuscationEnabled) FMLDeobfuscatingRemapper.INSTANCE.unmap(name.replace('.', '/')).replace('/', '.')
    else name
}

internal fun classExtends(name: String, superclass: String): Boolean {
    val mappedName = toKey(name)
    val mappedSuperName = toKey(superclass)

    return mappedName == mappedSuperName || declareClass(mappedName)?.let { cache ->
        cache.flatten()
        cache.parents.contains(mappedSuperName)
    } ?: false
}

internal fun getSuperClass(name: String): String {
    val mappedName = toKey(name)
    return declareClass(mappedName)?.run {
        flatten()
        superclass
    } ?: "java.lang.Object"
}

private fun declareClass(name: String): SuperCache? {
    val mappedName = toKey(name)

    return superclasses[mappedName]
        ?: try {
            declareASM(Launch.classLoader.getClassBytes(unKey(mappedName)))
        } catch (ignored: Exception) {
            null
        }
        ?: try {
            declareReflection(mappedName)
        } catch (ignored: Exception) {
            null
        }
}

@Throws(ClassNotFoundException::class)
private fun declareReflection(name: String): SuperCache {
    val clazz = Class.forName(name)
    val cache = getOrCreateCache(name)

    when {
        clazz.isInterface -> cache.superclass = "java.lang.Object"
        name == "java.lang.Object" -> return cache
        else -> cache.superclass = toKey(clazz.superclass.name)
    }

    cache.add(cache.superclass!!)
    clazz.interfaces.forEach { itf ->
        cache.add(toKey(itf.name))
    }

    return cache
}

private fun declareASM(bytes: ByteArray): SuperCache {
    val reader = ClassReader(bytes)
    val name = toKey(reader.className)
    val cache = getOrCreateCache(name)

    cache.superclass = toKey(reader.superName.replace('/', '.'))
    cache.add(cache.superclass!!)
    reader.interfaces.forEach { itf ->
        cache.add(toKey(itf.replace('/', '.')))
    }

    return cache
}

private fun getOrCreateCache(name: String): SuperCache {
    return superclasses.computeIfAbsent(name) { SuperCache() }
}
