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

package dev.su5ed.koremods.launchwrapper;

import dev.su5ed.koremods.api.SplashScreen;
import dev.su5ed.koremods.prelaunch.DependencyClassLoader;
import dev.su5ed.koremods.prelaunch.KoremodsPrelaunch;
import dev.su5ed.koremods.prelaunch.SplashScreenFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class SplashScreenFactoryImpl implements SplashScreenFactory {
    private static final List<String> LWJGL_DEP_PACKAGES = Arrays.asList(
            "org.lwjgl.", 
            "dev.su5ed.koremods.splash."
    );
    
    @Override
    public SplashScreen createSplashScreen(KoremodsPrelaunch prelaunch) {
        try {
            URL lwjglDep = prelaunch.extractDependency("LWJGL");
            DependencyClassLoader splashClassLoader = new DependencyClassLoader(
                    new URL[]{ prelaunch.modJarUrl, lwjglDep },
                    KoremodsPrelaunch.dependencyClassLoader,
                    LWJGL_DEP_PACKAGES
            );
            Class<?> splashClass = splashClassLoader.loadClass("dev.su5ed.koremods.splash.KoremodsSplashScreen");
            SplashScreen splash = (SplashScreen) splashClass.newInstance();
            splash.setTerminateOnClose(true);
            
            splash.startThread();
            splash.awaitInit();
            System.clearProperty("org.lwjgl.librarypath");

            return splash;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
