/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.conf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class ConfigurationTest {

  private Random randomSource = new Random();

  @Rule
  public TemporaryFolder tmpf = new TemporaryFolder();

  @Test()
  public void testKeyCount() {
    assertEquals("The number of properties keys in enum Key changed."
        + "\n This test class should be adapted.",
        76, Key.values().length);
  }

  @Test()
  public void testConfiguration() {
    Configuration conf = new Configuration();
    String val = "xyz";
    conf.setProperty(Key.OutputPath.name(), val);
    assertEquals(1, conf.size());
    assertEquals(val, conf.getProperty(Key.OutputPath.name()));
  }

  private String propLine(Key key, String val) {
    return key.name() + " = " + val + "\n";
  }

  @Test()
  public void testArrayValues() throws Exception {
    String[] array = new String[randomSource.nextInt(30) + 1];
    for (int i = 0; i < array.length; i++) {
      array[i] = Integer.toBinaryString(randomSource.nextInt(100));
    }
    String[] arrays = new String[] {
      Arrays.toString(array).replace("[", "").replace("]", ""),
      Arrays.toString(array).replace("[", "").replace("]", "")
          .replaceAll(" ", "")
    };
    Configuration conf = new Configuration();
    for (String input : arrays) {
      conf.clear();
      conf.setProperty(Key.RelayCacheOrigins.name(), input);
      assertArrayEquals("expected " + Arrays.toString(array) + "\nreceived: "
          + Arrays.toString(conf
              .getStringArray(Key.RelayCacheOrigins)),
          array, conf.getStringArray(Key.RelayCacheOrigins));
    }
  }

  @Test()
  public void testBoolValues() throws Exception {
    Configuration conf = new Configuration();
    conf.setProperty(Key.CompressRelayDescriptorDownloads.name(), "false");
    conf.setProperty(Key.RunOnce.name(), "trUe");
    conf.setProperty(Key.ReplaceIpAddressesWithHashes.name(), "false");
    assertFalse(conf.getBool(Key.CompressRelayDescriptorDownloads));
    assertTrue(conf.getBool(Key.RunOnce));
    assertFalse(conf.getBool(Key.ReplaceIpAddressesWithHashes));
  }

  @Test()
  public void testIntValues() throws Exception {
    Configuration conf = new Configuration();
    conf.setProperty(Key.BridgeDescriptorMappingsLimit.name(), "inf");
    assertEquals(Integer.MAX_VALUE,
        conf.getInt(Key.BridgeDescriptorMappingsLimit));
    int randomInt = randomSource.nextInt(Integer.MAX_VALUE);
    conf.clear();
    conf.load(new ByteArrayInputStream(
        propLine(Key.BridgeDescriptorMappingsLimit,
        "" + randomInt).getBytes()));
    assertEquals(randomInt,
        conf.getInt(Key.BridgeDescriptorMappingsLimit));
  }

  @Test()
  public void testFileValues() throws Exception {
    String[] files = new String[] { "/the/path/file.txt", "another/path"};
    Configuration conf = new Configuration();
    for (String file : files) {
      conf.clear();
      conf.setProperty(Key.OutputPath.name(), file);
      assertEquals(new File(file),
          conf.getPath(Key.OutputPath).toFile());
    }
  }

  @Test()
  public void testSourceTypeValues() throws Exception {
    String[] types = new String[] { "Local", "Cache", "Remote", "Sync"};
    Configuration conf = new Configuration();
    for (String type : types) {
      conf.clear();
      conf.setProperty(Key.BridgeSources.name(), type);
      Set<SourceType> sts = conf.getSourceTypeSet(Key.BridgeSources);
      assertEquals(1, sts.size());
      assertTrue(sts.contains(SourceType.valueOf(type)));
    }
  }

  @Test()
  public void testUrlArrayValues() throws Exception {
    URL[] array = new URL[randomSource.nextInt(30) + 1];
    for (int i = 0; i < array.length; i++) {
      array[i] = new URL("http://localhost/"
          + Integer.toBinaryString(randomSource.nextInt(100)));
    }
    String input =
        Arrays.toString(array).replace("[", "").replace("]", "")
            .replaceAll(" ", "");
    Configuration conf = new Configuration();
    conf.clear();
    conf.setProperty(Key.RelaySyncOrigins.name(), input);
    assertArrayEquals("expected " + Arrays.toString(array) + "\nreceived: "
        + Arrays.toString(conf
            .getUrlArray(Key.RelaySyncOrigins)),
        array, conf.getUrlArray(Key.RelaySyncOrigins));
  }

  @Test(expected = ConfigurationException.class)
  public void testPathValueException() throws Exception {
    Configuration conf = new Configuration();
    conf.setProperty(Key.RelayLocalOrigins.name(), "\\\u0000:");
    conf.getPath(Key.RelayLocalOrigins);
  }

  @Test(expected = ConfigurationException.class)
  public void testUrlValueException() throws Exception {
    Configuration conf = new Configuration();
    conf.setProperty(Key.ExitlistUrl.name(), "xxx://y.y.y");
    conf.getUrl(Key.ExitlistUrl);
  }

  @Test(expected = ConfigurationException.class)
  public void testIntValueException() throws Exception {
    Configuration conf = new Configuration();
    conf.setProperty(Key.BridgeDescriptorMappingsLimit.name(), "y7");
    conf.getInt(Key.BridgeDescriptorMappingsLimit);
  }
}
