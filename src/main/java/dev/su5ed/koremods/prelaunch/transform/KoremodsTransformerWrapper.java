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

package dev.su5ed.koremods.prelaunch.transform;

import dev.su5ed.koremods.prelaunch.KoremodsSetup;
import net.minecraft.launchwrapper.IClassTransformer;

public class KoremodsTransformerWrapper implements IClassTransformer {
    private static final String TRANSFORMER_CLASS = "dev.su5ed.koremods.launch.KoremodsTransformer";
    private IClassTransformer actualTransformer;
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (this.actualTransformer == null) {
            if (KoremodsSetup.dependencyClassLoader != null) {
                try {
                    Class<?> cl = KoremodsSetup.dependencyClassLoader.loadClass(TRANSFORMER_CLASS);
                    actualTransformer = (IClassTransformer) cl.newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (this.actualTransformer != null) {
            return this.actualTransformer.transform(name, transformedName, bytes);
        }
        
        return bytes;
    }
}