/*
 * SonarLint Core Library
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.sonarlint.core.container.analysis.filesystem;

import java.io.File;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Status;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;

class InputFileBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(InputFileBuilder.class);

  private final String moduleKey;
  private final PathResolver pathResolver;
  private final LanguageDetection langDetection;
  private final DefaultModuleFileSystem fs;
  private final Settings settings;
  private final FileMetadata fileMetadata;

  InputFileBuilder(String moduleKey, PathResolver pathResolver, LanguageDetection langDetection,
    DefaultModuleFileSystem fs, Settings settings, FileMetadata fileMetadata) {
    this.moduleKey = moduleKey;
    this.pathResolver = pathResolver;
    this.langDetection = langDetection;
    this.fs = fs;
    this.settings = settings;
    this.fileMetadata = fileMetadata;
  }

  String moduleKey() {
    return moduleKey;
  }

  PathResolver pathResolver() {
    return pathResolver;
  }

  LanguageDetection langDetection() {
    return langDetection;
  }

  FileSystem fs() {
    return fs;
  }

  @CheckForNull
  DefaultInputFile create(File file) {
    String relativePath = pathResolver.relativePath(fs.baseDir(), file);
    if (relativePath == null) {
      LOG.warn("File '{}' is ignored. It is not located in module basedir '{}'.", file.getAbsolutePath(), fs.baseDir());
      return null;
    }
    return new DefaultInputFile(moduleKey, relativePath);
  }

  /**
   * Optimization to not compute InputFile metadata if the file is excluded from analysis.
   */
  @CheckForNull
  DefaultInputFile completeAndComputeMetadata(DefaultInputFile inputFile, InputFile.Type type) {
    inputFile.setType(type);
    inputFile.setModuleBaseDir(fs.baseDir().toPath());
    inputFile.setCharset(fs.encoding());

    String lang = langDetection.language(inputFile);
    if (lang == null && !settings.getBoolean(CoreProperties.IMPORT_UNKNOWN_FILES_KEY)) {
      return null;
    }
    inputFile.setLanguage(lang);

    inputFile.initMetadata(fileMetadata.readMetadata(inputFile.file(), fs.encoding()));

    inputFile.setStatus(Status.ADDED);

    return inputFile;
  }

}