/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PersistUtilsTest {

  private static final String ANNO1 = "1@annotation\n";
  private static final String ANNO2 = "2@annotation\n";

  @Rule
  public TemporaryFolder tmpf = new TemporaryFolder();

  @Test()
  public void testCreateNew() throws Exception {
    Path out = tmpf.newFile().toPath();
    assertTrue("File should exist, but doesn't: " + out.toString(),
        Files.exists(out));
    assertFalse("Files shouldn't be created.",
        PersistenceUtils.storeToFileSystem(ANNO1.getBytes(),
        "some text".getBytes(), out, StandardOpenOption.CREATE_NEW));
    assertFalse("Files shouldn't be created.",
        PersistenceUtils.storeToFileSystem(ANNO1.getBytes(),
        "some text".getBytes(), out, StandardOpenOption.CREATE_NEW, true));
    List<String> text = Files.readAllLines(out, StandardCharsets.UTF_8);
    assertTrue("List should be empty: " + text, text.isEmpty());
  }

  @Test()
  public void testCreate() throws Exception {
    Path out = tmpf.newFolder().toPath();
    Path pathToCreate = Paths.get(out.toString(), "very-new-file");
    Path pathToCreateTmp = Paths
        .get(out.toString(), "very-new-file" + PersistenceUtils.TEMPFIX);
    String theText = "some text";
    assertTrue("Files should be created.",
        PersistenceUtils.storeToFileSystem(ANNO1.getBytes(),
        (theText + "\n").getBytes(), pathToCreate, StandardOpenOption.CREATE,
        true));
    assertTrue("File wasn't created.", Files.exists(pathToCreateTmp));
    PersistenceUtils.cleanDirectory(out);
    List<String> text = Files.readAllLines(pathToCreate,
        StandardCharsets.UTF_8);
    assertEquals("File contained: " + text, 2, text.size());
    assertEquals("File contained: " + text, theText, text.get(1));
  }

  @Test()
  public void testTruncateExisting() throws Exception {
    Path out = tmpf.newFolder().toPath();
    Path pathToCreate = Paths.get(out.toString(), "very-new-file");
    String theText = "some text";
    assertTrue("Files should be created.",
        PersistenceUtils.storeToFileSystem(ANNO1.getBytes(),
        (theText + "\n").getBytes(), pathToCreate, StandardOpenOption.CREATE));
    List<String> text = Files.readAllLines(pathToCreate,
        StandardCharsets.UTF_8);
    assertEquals("File contained: " + text, 2, text.size());
    assertEquals("File contained: " + text, theText, text.get(1));
    String theText2 = "other symbols";
    assertTrue("Files should be written.",
        PersistenceUtils.storeToFileSystem((ANNO2).getBytes(),
        (theText2 + "\n").getBytes(), pathToCreate,
        StandardOpenOption.TRUNCATE_EXISTING));
    text = Files.readAllLines(pathToCreate, StandardCharsets.UTF_8);
    assertEquals("File contained: " + text, 2, text.size());
    assertEquals("File contained: " + text, "2@annotation", text.get(0));
    assertEquals("File contained: " + text, theText2, text.get(1));
  }

  @Test()
  public void testAppend() throws Exception {
    Path out = tmpf.newFolder().toPath();
    Path pathToCreate = Paths.get(out.toString(), "very-new-file");
    String theText = "some text";
    assertTrue("Files should be created.",
        PersistenceUtils.storeToFileSystem(ANNO1.getBytes(),
        (theText + "\n").getBytes(), pathToCreate, StandardOpenOption.CREATE));
    List<String> text = Files.readAllLines(pathToCreate,
        StandardCharsets.UTF_8);
    assertEquals("File contained: " + text, 2, text.size());
    assertEquals("File contained: " + text, theText, text.get(1));
    String theText2 = "other symbols";
    assertTrue("Files should be created.",
        PersistenceUtils.storeToFileSystem((ANNO2).getBytes(),
        (theText2 + "\n").getBytes(), pathToCreate, StandardOpenOption.APPEND));
    text = Files.readAllLines(pathToCreate, StandardCharsets.UTF_8);
    assertEquals("File contained: " + text, 4, text.size());
    assertEquals("File contained: " + text, "1@annotation", text.get(0));
    assertEquals("File contained: " + text, theText, text.get(1));
    assertEquals("File contained: " + text, "2@annotation", text.get(2));
    assertEquals("File contained: " + text, theText2, text.get(3));
  }

  @Test()
  public void testCleanDirectory() throws Exception {
    /*
     * out/
     *   a/           # empty after deleting x
     *     x          # too old file, delete
     *   b/           # keep together with recent file y
     *     y.tmp      # recent enough, rename to y
     *   c/           # exclude (empty) subdirectory
     */
    Instant now = Instant.now();
    Path out = tmpf.newFolder().toPath();
    Path dirA = Files.createDirectory(out.resolve("a"));
    Path fileX = Files.createFile(dirA.resolve("x"));
    Files.setLastModifiedTime(fileX,
        FileTime.from(now.minus(9L, ChronoUnit.DAYS)));
    Path dirB = Files.createDirectory(out.resolve("b"));
    Path fileYTmp = Files.createFile(dirB.resolve("y.tmp"));
    Files.setLastModifiedTime(fileYTmp, FileTime.from(now));
    Path dirC = Files.createDirectory(out.resolve("c"));
    PersistenceUtils.cleanDirectory(out,
        now.minus(3L, ChronoUnit.DAYS).toEpochMilli(), dirC);
    assertFalse(Files.exists(dirA));
    assertFalse(Files.exists(fileX));
    assertTrue(Files.exists(dirB));
    assertFalse(Files.exists(fileYTmp));
    assertTrue(Files.exists(dirB.resolve("y")));
    assertTrue(Files.exists(dirC));
  }
}
