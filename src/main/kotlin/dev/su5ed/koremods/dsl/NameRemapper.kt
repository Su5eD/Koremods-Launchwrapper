@file:Suppress("unused")

package dev.su5ed.koremods.dsl

import dev.su5ed.koremods.script.CoremodKtsScript
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper

fun CoremodKtsScript.mapMethodName(owner: String, name: String, desc: String): String {
    return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, name, desc)
}

fun CoremodKtsScript.mapFieldName(owner: String, name: String, desc: String): String {
    return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, name, desc)
}
