/* Copyright 2019--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.persist;

import org.torproject.descriptor.BridgestrapStats;
import org.torproject.metrics.collector.conf.Annotation;

import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BridgestrapStatsPersistence
    extends DescriptorPersistence<BridgestrapStats> {

  private static final String BRIDGESTRAP = "bridgestrap";

  public BridgestrapStatsPersistence(BridgestrapStats desc) {
    super(desc, Annotation.BridgestrapStats.bytes());
    calculatePaths();
  }

  private void calculatePaths() {
    DateTimeFormatter directoriesFormatter = DateTimeFormatter
        .ofPattern("uuuu/MM/dd").withZone(ZoneOffset.UTC);
    String[] directories = this.desc.bridgestrapStatsEnd()
        .format(directoriesFormatter).split("/");
    DateTimeFormatter fileFormatter = DateTimeFormatter
        .ofPattern("uuuu-MM-dd-HH-mm-ss").withZone(ZoneOffset.UTC);
    String fileOut = this.desc.bridgestrapStatsEnd().format(fileFormatter)
        + "-bridgestrap-stats";
    this.recentPath = Paths.get(BRIDGESTRAP, fileOut).toString();
    this.storagePath = Paths.get(BRIDGESTRAP, directories[0], directories[1],
        directories[2], fileOut).toString();
  }
}

