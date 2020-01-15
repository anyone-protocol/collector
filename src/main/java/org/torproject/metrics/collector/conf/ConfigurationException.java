/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.conf;

public class ConfigurationException extends Exception {

  public ConfigurationException() {}

  public ConfigurationException(String msg) {
    super(msg);
  }

  public ConfigurationException(String msg, Exception ex) {
    super(msg, ex);
  }

}
