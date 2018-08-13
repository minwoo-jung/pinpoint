/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.boot;

import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author HyunGil Jeong
 */
public class SpringBootPluginTestCase implements PinpointPluginTestInstance {

    private static final String ARCHIVE_PATH = "test/spring-boot/archive";
    private static final String EXECUTABLE_PATH = "test/spring-boot/test-executables";

    private final PinpointPluginTestContext context;
    private final PinpointPluginTestInstance delegate;
    private final LauncherType launcherType;
    private final String testId;
    private final File testJar;
    private final File springBootBase;

    private File executableJar;

    public SpringBootPluginTestCase(PinpointPluginTestContext context, PinpointPluginTestInstance delegate, TestAppSpringBootVersion version, LauncherType launcherType) {
        this.context = context;
        this.delegate = delegate;
        this.launcherType = launcherType;
        this.testId = this.delegate.getTestId();
        String executableName = version.getExecutableName();
        this.testJar = new File(this.launcherType.getPath(ARCHIVE_PATH), this.launcherType.getJarName(executableName));
        this.springBootBase = new File(this.launcherType.getPath(EXECUTABLE_PATH));
    }

    @Override
    public String getTestId() {
        return this.testId;
    }

    @Override
    public List<String> getClassPath() {
        List<String> libs = this.delegate.getClassPath();
        try {
            packageExecutableJar(libs);
        } catch (IOException e) {
            throw new RuntimeException("Error creating executable jar. Cause : " + e.getMessage());
        }
        return libs;
    }

    private void packageExecutableJar(List<String> libs) throws IOException {
        this.executableJar = new File(this.springBootBase, this.testJar.getName());
        try {
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(this.executableJar));

            Set<String> injectedEntries = injectDependencyEntries(jarOutputStream, libs);
            copyTestJar(jarOutputStream, injectedEntries);

            jarOutputStream.flush();
            jarOutputStream.close();
        } finally {
            this.executableJar.deleteOnExit();
        }
    }

    private Set<String> injectDependencyEntries(JarOutputStream jarOutputStream, List<String> libs) throws IOException {
        Set<String> injectedEntries = new HashSet<String>();
        for (String lib : libs) {
            File libFile = new File(lib);
            if (libFile.isDirectory()) {
                copyDirectory(jarOutputStream, injectedEntries, libFile);
            } else {
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(libFile);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith("META-INF")) {
                            continue;
                        }
                        if (injectedEntries.contains(entry.getName())) {
                            continue;
                        }
                        jarOutputStream.putNextEntry(entry);
                        InputStream is = jarFile.getInputStream(new JarEntry(entry.getName()));
                        copy(is, jarOutputStream);
                        is.close();
                        jarOutputStream.closeEntry();
                        injectedEntries.add(entry.getName());
                    }
                } catch (Exception e) {
                    // ignore - don't copy non-jar files
                } finally {
                    if (jarFile != null) {
                        jarFile.close();
                    }
                }
            }
        }
        return injectedEntries;
    }

    private void copyDirectory(JarOutputStream jarOutputStream, Set<String> injectedEntries, File libFile) throws IOException {
        for (File nestedFile : libFile.listFiles()) {
            if (nestedFile.isDirectory()) {
                String root = nestedFile.getName();
                copyNestedClassFile(jarOutputStream, injectedEntries, nestedFile, root);
            }
        }
    }

    private void copyNestedClassFile(JarOutputStream jarOutputStream, Set<String> injectedEntries, File file, String name) throws IOException {
        String entryName = name + "/";
        if (!injectedEntries.contains(entryName)) {
            JarEntry entry = new JarEntry(entryName);
            jarOutputStream.putNextEntry(entry);
            jarOutputStream.closeEntry();
            injectedEntries.add(entryName);
        }
        for (File nestedFile : file.listFiles()) {
            String fileName = nestedFile.getName();
            if (nestedFile.isDirectory()) {
                copyNestedClassFile(jarOutputStream, injectedEntries, nestedFile, entryName + fileName);
            } else {
                if (fileName.endsWith(".class")) {
                    String fileEntryName = entryName + fileName;
                    JarEntry fileEntry = new JarEntry(fileEntryName);
                    jarOutputStream.putNextEntry(fileEntry);
                    InputStream is = new BufferedInputStream(new FileInputStream(nestedFile));
                    copy(is, jarOutputStream);
                    jarOutputStream.closeEntry();
                    injectedEntries.add(fileEntryName);
                }
            }
        }
    }

    private void copyTestJar(JarOutputStream jarOutputStream, Set<String> injectedEntries) throws IOException {
        JarFile baseJar = new JarFile(this.testJar);
        Enumeration<JarEntry> entries = baseJar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (injectedEntries.contains(entry.getName())) {
                continue;
            }
            if (entry.getMethod() == ZipEntry.STORED) {
                jarOutputStream.putNextEntry(new JarEntry(entry));
            } else {
                jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
            }
            InputStream is = baseJar.getInputStream(entry);
            copy(is, jarOutputStream);
            is.close();
            jarOutputStream.closeEntry();
        }
    }

    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }

    @Override
    public List<String> getVmArgs() {
        List<String> vmArgs = new ArrayList<String>(this.delegate.getVmArgs());
        vmArgs.add("-Dpinpoint.test.class=" + this.context.getTestClass().getName());
        if (this.launcherType == LauncherType.WAR) {
            vmArgs.add("-jar");
        }
        return vmArgs;
    }

    @Override
    public String getMainClass() {
        return this.executableJar.getAbsolutePath();
    }

    @Override
    public List<String> getAppArgs() {
        return Collections.emptyList();
    }

    @Override
    public File getWorkingDirectory() {
        return this.springBootBase;
    }

    @Override
    public Scanner startTest(Process process) throws Throwable {
        return this.delegate.startTest(process);
    }

    @Override
    public void endTest(Process process) throws Throwable {

    }
}
