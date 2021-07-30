/* Copyright 2019--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.bridgestrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.descriptor.BridgestrapStats;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorParser;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.metrics.collector.conf.Annotation;
import org.torproject.metrics.collector.conf.Configuration;
import org.torproject.metrics.collector.conf.ConfigurationException;
import org.torproject.metrics.collector.conf.Key;
import org.torproject.metrics.collector.cron.CollecTorMain;
import org.torproject.metrics.collector.downloader.Downloader;
import org.torproject.metrics.collector.persist.BridgestrapStatsPersistence;
import org.torproject.metrics.collector.persist.PersistenceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedSet;
import java.util.TreeSet;

public class BridgestrapStatsDownloader extends CollecTorMain {

  private static final Logger logger = LoggerFactory.getLogger(
      BridgestrapStatsDownloader.class);

  private String recentPathName;

  private String outputPathName;

  /** Instantiate the bridgestrap-stats module using the given configuration. */
  public BridgestrapStatsDownloader(Configuration config) {
    super(config);
    this.mapPathDescriptors.put("recent/bridgestrap", BridgestrapStats.class);
  }

  @Override
  public String module() {
    return "BridgestrapStats";
  }

  @Override
  protected String syncMarker() {
    return "BridgestrapStats";
  }

  @Override
  protected void startProcessing() throws ConfigurationException {

    this.recentPathName = config.getPath(Key.RecentPath).toString();
    logger.debug("Downloading bridgestrap stats...");
    URL url = config.getUrl(Key.BridgestrapStatsUrl);
    byte[] downloadedBytes;
    try {
      downloadedBytes = Downloader.downloadFromHttpServer(url);
    } catch (IOException e) {
      logger.warn("Failed downloading {}.", url, e);
      return;
    }
    if (null == downloadedBytes) {
      logger.warn("Could not download {}.", url);
      return;
    }
    logger.debug("Finished downloading {}.", url);

    Path parsedBridgestrapStatsFile = this.config.getPath(Key.StatsPath)
        .resolve("processed-bridgestrap-stats");
    SortedSet<Path> previouslyProcessedFiles = this.readProcessedFiles(
        parsedBridgestrapStatsFile);
    SortedSet<Path> processedFiles = new TreeSet<>();
    DescriptorParser descriptorParser =
        DescriptorSourceFactory.createDescriptorParser();
    SortedSet<LocalDateTime> bridgestrapStatsEnds = new TreeSet<>();
    this.outputPathName = config.getPath(Key.OutputPath).toString();
    for (Descriptor descriptor : descriptorParser.parseDescriptors(
        downloadedBytes, null, null)) {
      if (descriptor instanceof BridgestrapStats) {
        BridgestrapStats bridgestrapStats = (BridgestrapStats) descriptor;
        LocalDateTime bridgestrapStatsEnd = bridgestrapStats.bridgestrapStatsEnd();
        bridgestrapStatsEnds.add(bridgestrapStatsEnd);
        BridgestrapStatsPersistence persistence
            = new BridgestrapStatsPersistence(bridgestrapStats);
        File tarballFile = new File(outputPathName + "/"
            + persistence.getStoragePath());
        Path relativeFileName = Paths.get(tarballFile.getName());
        processedFiles.add(relativeFileName);
        if (previouslyProcessedFiles.contains(relativeFileName)) {
          continue;
        }
        if (tarballFile.exists()) {
          continue;
        }
        File rsyncFile = new File(this.recentPathName + "/"
            + persistence.getRecentPath());
        File[] outputFiles = new File[] { tarballFile, rsyncFile };
        for (File outputFile : outputFiles) {
          this.writeToFile(outputFile, Annotation.BridgestrapStats.bytes(),
              bridgestrapStats.getRawDescriptorBytes());
        }
      }
    }
    if (bridgestrapStatsEnds.isEmpty()) {
      logger.warn("Could not parse downloaded bridgestrap stats.");
      return;
    } else if (bridgestrapStatsEnds.last().isBefore(LocalDateTime.now()
        .minusHours(48L))) {
      logger.warn("The latest bridgestrap stats are older than 48 hours: {}.",
          bridgestrapStatsEnds.last());
    }

    this.writeProcessedFiles(parsedBridgestrapStatsFile, processedFiles);
    this.cleanUpDirectories();
  }

  /**
   * Write the given byte array(s) to the given file.
   *
   * <p>If the file already exists, it is overwritten. If the parent directory
   * (or any of its parent directories) does not exist, it is created. If
   * anything goes wrong, log a warning and return.</p>
   *
   * @param outputFile File to write to.
   * @param bytes One or more byte arrays.
   */
  private void writeToFile(File outputFile, byte[] ... bytes) {
    try {
      if (!outputFile.getParentFile().exists()
          && !outputFile.getParentFile().mkdirs()) {
        logger.warn("Could not create parent directories of {}.", outputFile);
        return;
      }
      OutputStream os = new FileOutputStream(outputFile);
      for (byte[] b : bytes) {
        os.write(b);
      }
      os.close();
    } catch (IOException e) {
      logger.warn("Could not write downloaded bridgestrap stats to {}",
          outputFile.getAbsolutePath(), e);
    }
  }

  /** Delete all files from the rsync (out) directory that have not been
   * modified in the last three days (seven weeks). */
  private void cleanUpDirectories() {
    PersistenceUtils.cleanDirectory(
        Paths.get(this.recentPathName, "bridgestrap"),
        Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli());
    PersistenceUtils.cleanDirectory(
        Paths.get(this.outputPathName, "bridgestrap"),
        Instant.now().minus(49, ChronoUnit.DAYS).toEpochMilli());
  }
}

