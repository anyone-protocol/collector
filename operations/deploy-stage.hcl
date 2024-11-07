job "collector-stage" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  update {
    max_parallel      = 1
    healthy_deadline  = "15m"
    progress_deadline = "20m"
  }

  group "collector-stage-group" {
    count = 1

    volume "collector-data" {
      type      = "host"
      read_only = false
      source    = "collector-stage"
    }

    network {
      mode = "bridge"
      port "http-port" {
        static = 9100
        to     = 80
        host_network = "wireguard"
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "collector-jar-stage-task" {
      driver = "docker"

      env {
        LOGBASE = "data/logs"
      }

      volume_mount {
        volume      = "collector-data"
        destination = "/srv/collector/data"
        read_only   = false
      }

      config {
        image   = "ghcr.io/anyone-protocol/collector:DEPLOY_TAG"
        image_pull_timeout = "15m"
        volumes = [
          "local/collector.properties:/srv/collector/collector.properties:ro",
          "local/logs:/srv/collector/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 1024
      }

      template {
        change_mode = "noop"
        data        = <<EOH
######## Collector Properties
#
######## Run Configuration ########
# If RunOnce=true, the activated modules below will only be
# run one time and without any delay.
# Make sure only to run non-interfering modules together.
RunOnce = false
##
# Waiting time for regular shutdown in minutes.
ShutdownGraceWaitMinutes = 10
## the following defines, if this module is activated
BridgedescsActivated = false
# period in minutes
BridgedescsPeriodMinutes = 60
# offset in minutes since the epoch and
BridgedescsOffsetMinutes = 9
## the following defines, if this module is activated
BridgePoolAssignmentsActivated = false
# period in minutes
BridgePoolAssignmentsPeriodMinutes = 60
# offset in minutes since the epoch and
BridgePoolAssignmentsOffsetMinutes = 9
## the following defines, if this module is activated
ExitlistsActivated = false
# period in minutes
ExitlistsPeriodMinutes = 60
# offset in minutes since the epoch and
ExitlistsOffsetMinutes = 2
## the following defines, if this module is activated
RelaydescsActivated = true
# period in minutes
RelaydescsPeriodMinutes = 5
# offset in minutes since the epoch and
RelaydescsOffsetMinutes = 0
## the following defines, if this module is activated
OnionPerfActivated = true
# period in minutes
OnionPerfPeriodMinutes = 10
# offset in minutes since the epoch and
OnionPerfOffsetMinutes = 2
# the following defines, if this module is activated
UpdateindexActivated = true
# period in minutes
UpdateindexPeriodMinutes = 5
# offset in minutes since the epoch and
UpdateindexOffsetMinutes = 0
# the following defines, if this module is activated
WebstatsActivated = false
# period in minutes
WebstatsPeriodMinutes = 360
# offset in minutes since the epoch and
WebstatsOffsetMinutes = 31
# the following defines, if this module is activated
SnowflakeStatsActivated = false
# period in minutes
SnowflakeStatsPeriodMinutes = 480
# offset in minutes since the epoch and
SnowflakeStatsOffsetMinutes = 100
# the following defines, if this module is activated
BridgedbMetricsActivated = false
# period in minutes
BridgedbMetricsPeriodMinutes = 480
# offset in minutes since the epoch and
BridgedbMetricsOffsetMinutes = 340
# the following defines, if this module is activated
BridgestrapStatsActivated = false
# period in minutes
BridgestrapStatsPeriodMinutes = 480
# offset in minutes since the epoch and
BridgestrapStatsOffsetMinutes = 100

######## General Properties ########
# The URL of this instance.  This will be the base URL
# written to index.json, i.e. please change this to the mirrors url!
InstanceBaseUrl = http://{{ env `NOMAD_HOST_ADDR_http-port` }}
# The top-level directory for archived descriptors.
IndexedPath = data/indexed
# The top-level directory for the recent descriptors that were
# published in the last 72 hours.
RecentPath = data/indexed/recent
# The top-level directory for the retrieved descriptors that will
# be archived.
OutputPath = data/out
# Some statistics are stored here.
StatsPath = data/stats
# Path for descriptors downloaded from other instances
SyncPath = data/sync
# Directory served via an external web server and managed by us which contains
# (hard) links to files in ArchivePath and RecentPath and which therefore must
# be located on the same file system. Also contains index.json and its
# compressed versions index.json.gz, index.json.bz2, and index.json.xz.
HtdocsPath = data/htdocs
######## Relay descriptors ########
#
## Define descriptor sources
#  possible values: Sync, Cache, Remote, Local
RelaySources = Remote
#  Retrieve files from the following CollecTor instances.
#  List of URLs separated by comma.
RelaySyncOrigins = https://collector.torproject.org
#
## Path to Tor data directory to read cached-* files from
## the listed path(s). If there is more that one separated by comma.
RelayCacheOrigins = in/relay-descriptors/cacheddesc/
## Relative path to directory to import directory archives from.
## Note that when importing bandwidth files, the parent directory name is
## included in the @source annotation and in the file name. Recommended
## source names are nicknames of directory authorities using these files.
RelayLocalOrigins = in/relay-descriptors/archives/
#
## Keep a history of imported directory archive files to know which files
## have been imported before. This history can be useful when importing
## from a changing source to avoid importing descriptors over and over
## again, but it can be confusing to users who don't know about it.
KeepDirectoryArchiveImportHistory = true
#
## Comma separated list of directory authority addresses (IP[:port]) to
## download missing relay descriptors from
DirectoryAuthoritiesAddresses = 65.21.12.154:9130,148.251.23.105:9130,135.181.231.123:9130,148.251.23.105:9131,135.181.231.123:9131,148.251.23.105:9132,135.181.231.123:9132
#
## Comma separated list of directory authority fingerprints to download
## votes
DirectoryAuthoritiesFingerprintsForVotes = D684B877417A224B727C04714ECF04A95987FD36,9578CA946B1AEAB730ECF474590C9A29A6D3A16C,55F541EAE4429C650CB3A7569AE08F4C8A200D53,4FD213ABF97101D4AE3A63CB602F4C09BD71EAEC,94421420BAEC4F35E6C1B257B87D5397FC77C3C2,468F3D7ACB8F57B6450154BB18DB4E4D4C4E8FE8,E33E6491E3856683B1B0F6E31940C536B80E1485
#
## Download all server descriptors from the directory authorities at most
## once a day (only if DownloadRelayDescriptors is true)
DownloadAllServerDescriptors = true
#
## Download all extra-info descriptors from the directory authorities at
## most once a day (only if DownloadRelayDescriptors is true)
DownloadAllExtraInfoDescriptors = true
#
## Compress relay descriptors downloads by adding .z to the URLs
CompressRelayDescriptorDownloads = true
#
#
######## Bridge descriptors ########
#
## Define descriptor sources
#  possible values: Sync, Local
BridgeSources = Local
#  Retrieve files from the following instances.
#  List of URLs separated by comma.
BridgeSyncOrigins = https://collector.torproject.org
## Relative path to directory to import bridge descriptor snapshots from
BridgeLocalOrigins = in/bridge-descriptors/
#
## Replace IP addresses in sanitized bridge descriptors with 10.x.y.z
## where x.y.z = H(IP address | bridge identity | secret)[:3], so that we
## can learn about IP address changes.
ReplaceIpAddressesWithHashes = false
#
## Limit internal bridge descriptor mapping state to the following number
## of days, or inf for unlimited.
BridgeDescriptorMappingsLimit = inf
#
#
######## Bridge pool assignments ########
#
## Define descriptor sources
#  possible values: Sync, Local
BridgePoolAssignmentsSources = Local
##  Retrieve files from the following instances.
##  List of URLs separated by comma.
BridgePoolAssignmentsSyncOrigins = https://collector.torproject.org
## Relative path to directory to read bridge pool assignment files from
BridgePoolAssignmentsLocalOrigins = in/bridge-pool-assignments/
#
#
######## Exit lists ########
#
## Define descriptor sources
#  possible values: Sync, Remote
ExitlistSources = Remote
##  Retrieve files from the following instances.
##  List of URLs separated by comma.
ExitlistSyncOrigins = https://collector.torproject.org
## Where to download exit-lists from.
ExitlistUrl = https://check.torproject.org/exit-addresses
#
######## OnionPerf downloader ########
#
## Define descriptor sources
#  possible values: Remote, Sync
OnionPerfSources = Remote
#  Retrieve files from the following CollecTor instances.
#  List of URLs separated by comma.
OnionPerfSyncOrigins = https://collector.torproject.org
#
## OnionPerf base URLs
## Hosts must be configured to use the first subdomain part of the given URL as
## source name, e.g., SOURCE=first for the first URL below, SOURCE=second for
## the second, etc.:
## OnionPerfHosts = http://first.torproject.org/, http://second.torproject.org/
OnionPerfHosts = http://95.216.32.105:9221/,http://88.99.219.105:9221/,http://176.9.29.53:9221/,https://collector.torproject.org/recent/onionperf/
######## Tor Weblogs ########
#
## Define descriptor sources
#  possible values: Local, Sync
WebstatsSources = Local
#  Retrieve files from the following CollecTor instances.
#  List of URLs separated by comma.
WebstatsSyncOrigins = https://collector.torproject.org
## Relative path to directory to import logfiles from.
WebstatsLocalOrigins = in/webstats
# Default 'true' behaves as stated in section 4 of
# https://metrics.torproject.org/web-server-logs.html
WebstatsLimits = true
#
#
######## Snowflake statistics ########
#
## Define descriptor sources
#  possible values: Sync, Remote
SnowflakeStatsSources = Remote
##  Retrieve files from the following instances.
##  List of URLs separated by comma.
SnowflakeStatsSyncOrigins = https://collector.torproject.org
## Where to download snowflake statistics from.
SnowflakeStatsUrl = https://snowflake-broker.torproject.net/metrics
#
######## BridgeDB statistics ########
#
## Define descriptor sources
#  possible values: Local, Sync
BridgedbMetricsSources = Local
## Relative path to directory to import BridgeDB metrics from.
BridgedbMetricsLocalOrigins = in/bridgedb-stats
##  Retrieve files from the following instances.
##  List of URLs separated by comma.
BridgedbMetricsSyncOrigins = https://collector.torproject.org
#
######## Bridgestrap statistics ########
#
## Define descriptor sources
#  possible values: Sync, Remote
BridgestrapStatsSources = Remote
##  Retrieve files from the following instances.
##  List of URLs separated by comma.
BridgestrapStatsSyncOrigins = https://collector.torproject.org
## Where to download snowflake statistics from.
BridgestrapStatsUrl = https://bridges.torproject.org/bridgestrap-collector
#
        EOH
        destination = "local/collector.properties"
      }

      service {
        name     = "collector-jar-stage"
        tags     = ["logging"]
      }
    }

    task "collector-nginx-stage-task" {
      driver = "docker"

      volume_mount {
        volume      = "collector-data"
        destination = "/var/www/collector"
        read_only   = true
      }

      config {
        image   = "nginx"
        volumes = [
          "local/nginx-collector:/etc/nginx/conf.d/default.conf:ro"
        ]
        ports = ["http-port"]
      }

      resources {
        cpu    = 256
        memory = 256
      }

      service {     
        name     = "collector-stage"
        tags     = ["collector", "logging"]
        port     = "http-port"
        check {
          name     = "collector nginx http server alive"
          type     = "tcp"
          interval = "10s"
          timeout  = "10s"
          check_restart {
            limit = 10
            grace = "30s"
          }
        }
      }

      template {
        change_mode = "noop"
        data        = <<EOH
##
# The following is a simple nginx configuration to run CollecTor.
##
log_format default '[$time_iso8601] $remote_addr - $remote_user $request $status $body_bytes_sent $http_referer $http_user_agent $http_x_forwarded_for';

server {

  root /var/www/collector/htdocs;
  access_log /dev/stdout default; 
  error_log /dev/stderr warn;

  # This option make sure that nginx will follow symlinks to the appropriate
  # CollecTor folders
  autoindex on;

  index index.html;

  listen 0.0.0.0:80;

  location / {
    try_files $uri $uri/ =404;
  }

  location ~/\.ht {
    deny all;
  }
}
        EOH
        destination = "local/nginx-collector"
      }
    }
  }
}
