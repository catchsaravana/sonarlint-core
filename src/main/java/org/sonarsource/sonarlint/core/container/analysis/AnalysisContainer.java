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
package org.sonarsource.sonarlint.core.container.analysis;

import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.resources.Languages;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileExclusions;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.sonarlint.core.analyzer.issue.IssuableFactory;
import org.sonarsource.sonarlint.core.analyzer.issue.IssueFilters;
import org.sonarsource.sonarlint.core.analyzer.noop.DefaultFileLinesContextFactory;
import org.sonarsource.sonarlint.core.analyzer.noop.NoOpHighlightableBuilder;
import org.sonarsource.sonarlint.core.analyzer.noop.NoOpSymbolizableBuilder;
import org.sonarsource.sonarlint.core.analyzer.noop.TestPlanBuilder;
import org.sonarsource.sonarlint.core.analyzer.noop.TestableBuilder;
import org.sonarsource.sonarlint.core.analyzer.perspectives.BatchPerspectives;
import org.sonarsource.sonarlint.core.analyzer.sensor.BatchExtensionDictionnary;
import org.sonarsource.sonarlint.core.analyzer.sensor.DefaultSensorContext;
import org.sonarsource.sonarlint.core.analyzer.sensor.DefaultSensorStorage;
import org.sonarsource.sonarlint.core.analyzer.sensor.LtsApiSensorContext;
import org.sonarsource.sonarlint.core.analyzer.sensor.PhaseExecutor;
import org.sonarsource.sonarlint.core.analyzer.sensor.SensorOptimizer;
import org.sonarsource.sonarlint.core.analyzer.sensor.SensorsExecutor;
import org.sonarsource.sonarlint.core.container.ComponentContainer;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.ComponentIndexer;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.DefaultLanguagesRepository;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.DefaultModuleFileSystem;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.FileIndexer;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.FileSystemLogger;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.InputFileBuilderFactory;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.InputPathCache;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.LanguageDetectionFactory;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.ModuleInputFileCache;
import org.sonarsource.sonarlint.core.container.global.ExtensionInstaller;
import org.sonarsource.sonarlint.core.container.global.ExtensionMatcher;
import org.sonarsource.sonarlint.core.container.global.ExtensionUtils;
import org.sonarsource.sonarlint.core.index.BatchComponentCache;
import org.sonarsource.sonarlint.core.index.DefaultIndex;

public class AnalysisContainer extends ComponentContainer {

  private static final Logger LOG = Loggers.get(AnalysisContainer.class);

  public AnalysisContainer(ComponentContainer globalContainer) {
    super(globalContainer);
  }

  @Override
  protected void doBeforeStart() {
    addBatchComponents();
    addBatchExtensions();
  }

  private void addBatchComponents() {
    add(
      new ProjectProvider(),
      DefaultIndex.class,
      DefaultFileLinesContextFactory.class,
      BatchComponentCache.class,

      // temp
      new AnalysisTempFolderProvider(),

      // file system
      InputPathCache.class,
      PathResolver.class,

      // tests
      TestPlanBuilder.class,
      TestableBuilder.class,

      // lang
      Languages.class,
      DefaultLanguagesRepository.class,

      AnalysisSettings.class,

      PhaseExecutor.class,
      SensorsExecutor.class,

      // file system
      ModuleInputFileCache.class,
      FileExclusions.class,
      InputFileBuilderFactory.class,
      FileMetadata.class,
      LanguageDetectionFactory.class,
      FileIndexer.class,
      ComponentIndexer.class,
      FileSystemLogger.class,
      DefaultModuleFileSystem.class,

      SensorOptimizer.class,

      DefaultSensorContext.class,
      DefaultSensorStorage.class,
      LtsApiSensorContext.class,
      BatchExtensionDictionnary.class,
      IssueFilters.class,

      // rules
      CheckFactory.class,

      // issues
      IssuableFactory.class,
      org.sonar.api.issue.NoSonarFilter.class,

      // Perspectives
      BatchPerspectives.class,
      NoOpHighlightableBuilder.class,
      NoOpSymbolizableBuilder.class);
  }

  private void addBatchExtensions() {
    getComponentByType(ExtensionInstaller.class).install(this, new BatchExtensionFilter());
  }

  @Override
  protected void doAfterStart() {
    LOG.debug("Start recursive analysis of project modules");
    Project p = getComponentByType(Project.class);
    getComponentByType(PhaseExecutor.class).execute(p);
  }

  static class BatchExtensionFilter implements ExtensionMatcher {
    @Override
    public boolean accept(Object extension) {
      return ExtensionUtils.isBatchSide(extension)
        && (ExtensionUtils.isInstantiationStrategy(extension, InstantiationStrategy.PER_BATCH)
          || ExtensionUtils.isInstantiationStrategy(extension, InstantiationStrategy.PER_PROJECT));
    }
  }

}