/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.conf;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum containing all the properties keys of the configuration.
 * Specifies the key type.
 */
public enum Key {

  ShutdownGraceWaitMinutes(Long.class),
  RunOnce(Boolean.class),
  ExitlistUrl(URL.class),
  InstanceBaseUrl(String.class),
  IndexedPath(Path.class),
  RecentPath(Path.class),
  OutputPath(Path.class),
  StatsPath(Path.class),
  SyncPath(Path.class),
  HtdocsPath(Path.class),
  RelaySources(SourceType[].class),
  BridgeSources(SourceType[].class),
  BridgePoolAssignmentsSources(SourceType[].class),
  ExitlistSources(SourceType[].class),
  OnionPerfSources(SourceType[].class),
  WebstatsSources(SourceType[].class),
  RelayCacheOrigins(String[].class),
  RelayLocalOrigins(Path.class),
  RelaySyncOrigins(URL[].class),
  BridgeSyncOrigins(URL[].class),
  BridgeLocalOrigins(Path.class),
  BridgePoolAssignmentsLocalOrigins(Path.class),
  BridgePoolAssignmentsSyncOrigins(URL[].class),
  ExitlistSyncOrigins(URL[].class),
  OnionPerfSyncOrigins(URL[].class),
  WebstatsSyncOrigins(URL[].class),
  WebstatsLocalOrigins(Path.class),
  BridgedescsActivated(Boolean.class),
  BridgedescsOffsetMinutes(Integer.class),
  BridgedescsPeriodMinutes(Integer.class),
  BridgePoolAssignmentsActivated(Boolean.class),
  BridgePoolAssignmentsOffsetMinutes(Integer.class),
  BridgePoolAssignmentsPeriodMinutes(Integer.class),
  ExitlistsActivated(Boolean.class),
  ExitlistsOffsetMinutes(Integer.class),
  ExitlistsPeriodMinutes(Integer.class),
  RelaydescsActivated(Boolean.class),
  RelaydescsOffsetMinutes(Integer.class),
  RelaydescsPeriodMinutes(Integer.class),
  OnionPerfActivated(Boolean.class),
  OnionPerfOffsetMinutes(Integer.class),
  OnionPerfPeriodMinutes(Integer.class),
  UpdateindexActivated(Boolean.class),
  UpdateindexOffsetMinutes(Integer.class),
  UpdateindexPeriodMinutes(Integer.class),
  CompressRelayDescriptorDownloads(Boolean.class),
  DirectoryAuthoritiesAddresses(String[].class),
  DirectoryAuthoritiesFingerprintsForVotes(String[].class),
  DownloadAllServerDescriptors(Boolean.class),
  DownloadAllExtraInfoDescriptors(Boolean.class),
  KeepDirectoryArchiveImportHistory(Boolean.class),
  ReplaceIpAddressesWithHashes(Boolean.class),
  BridgeDescriptorMappingsLimit(Integer.class),
  OnionPerfHosts(URL[].class),
  WebstatsActivated(Boolean.class),
  WebstatsLimits(Boolean.class),
  WebstatsOffsetMinutes(Integer.class),
  WebstatsPeriodMinutes(Integer.class),
  SnowflakeStatsActivated(Boolean.class),
  SnowflakeStatsOffsetMinutes(Integer.class),
  SnowflakeStatsPeriodMinutes(Integer.class),
  SnowflakeStatsUrl(URL.class),
  SnowflakeStatsSources(SourceType[].class),
  SnowflakeStatsSyncOrigins(URL[].class),
  BridgedbMetricsActivated(Boolean.class),
  BridgedbMetricsOffsetMinutes(Integer.class),
  BridgedbMetricsPeriodMinutes(Integer.class),
  BridgedbMetricsSources(SourceType[].class),
  BridgedbMetricsLocalOrigins(Path.class),
  BridgedbMetricsSyncOrigins(URL[].class),
  BridgestrapStatsActivated(Boolean.class),
  BridgestrapStatsOffsetMinutes(Integer.class),
  BridgestrapStatsPeriodMinutes(Integer.class),
  BridgestrapStatsUrl(URL.class),
  BridgestrapStatsSources(SourceType[].class),
  BridgestrapStatsSyncOrigins(URL[].class);

  private Class clazz;
  private static Set<String> keys;

  /**
   * @param clazz Class of key value.
   */
  Key(Class clazz) {
    this.clazz = clazz;
  }

  public Class keyClass() {
    return clazz;
  }

  /** Verifies, if the given string corresponds to an enum value. */
  public static boolean has(String someKey) {
    if (null == keys) {
      keys = new HashSet<>();
      for (Key key : values()) {
        keys.add(key.name());
      }
    }
    return keys.contains(someKey);
  }

}
