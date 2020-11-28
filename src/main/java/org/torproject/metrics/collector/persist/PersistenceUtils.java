/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.collector.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class PersistenceUtils {

  private static final Logger logger = LoggerFactory.getLogger(
      PersistenceUtils.class);

  public static final String TEMPFIX = ".tmp";

  /** Stores a descriptor adding missing annotations with the given options. */
  public static boolean storeToFileSystem(byte[] typeAnnotation, byte[] data,
      Path outputPath, StandardOpenOption option) {
    return storeToFileSystem(typeAnnotation, data, outputPath, option, false);
  }

  /** Stores a descriptor adding missing annotations with the given options.
   * Uses a temporary file and requires a run of cleanDirectory for moving
   * files to the final location. */
  public static boolean storeToFileSystem(byte[] typeAnnotation,
      byte[] data, Path outputPath, StandardOpenOption option, boolean useTmp) {
    Path tmpPath = outputPath;
    try {
      if (useTmp) {
        tmpPath = new File(outputPath.toFile().getParent(),
            outputPath.toFile().getName() + TEMPFIX).toPath();
        if (Files.exists(outputPath)
            && StandardOpenOption.CREATE_NEW == option) {
          return false;
        }
        if (Files.exists(outputPath) && !Files.exists(tmpPath)
            && StandardOpenOption.APPEND == option) {
          Files.copy(outputPath, tmpPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
      return createOrAppend(typeAnnotation, data, tmpPath, option);
    } catch (FileAlreadyExistsException faee) {
      logger.debug("Already have descriptor(s) of type '{}': {}. Skipping.",
          new String(typeAnnotation), outputPath);
    } catch (IOException | SecurityException
          | UnsupportedOperationException e) {
      logger.warn("Could not store descriptor(s) {} of type '{}'",
          outputPath, new String(typeAnnotation), e);
    } catch (Throwable th) {  // anything else
      logger.warn("Problem storing descriptor(s) {} of type '{}'",
          outputPath, new String(typeAnnotation), th);
    }
    return false;
  }

  private static boolean createOrAppend(byte[] annotation, byte[] data,
      Path path, StandardOpenOption option) throws IOException {
    StandardOpenOption appendOption = option;
    Files.createDirectories(path.getParent());
    if (data.length > 0 && data[0] != '@') {
      Files.write(path, annotation, appendOption, StandardOpenOption.CREATE);
      appendOption = StandardOpenOption.APPEND;
    }
    Files.write(path, data, appendOption, StandardOpenOption.CREATE);
    return true;
  }

  /** Move temporary files to their final location. */
  public static void cleanDirectory(Path pathToClean) {
    PersistenceUtils.cleanDirectory(pathToClean, -1L);
  }

  /** Clean up the given directory by deleting files that are older than the
   * given cut-off timestamp, and by moving temporary files to their final
   * location. */
  public static void cleanDirectory(Path pathToClean, long cutOffMillis) {
    PersistenceUtils.cleanDirectory(pathToClean, cutOffMillis, null);
  }

  /** Clean up the given directory, excluding the given subdirectory, by
   * deleting files that are older than the given cut-off timestamp, and by
   * moving temporary files to their final location. */
  public static void cleanDirectory(Path pathToClean, long cutOffMillis,
      Path pathToExclude) {
    if (!Files.exists(pathToClean)) {
      return;
    }
    logger.info("Cleaning up directory {} with cut-off time {}.",
        pathToClean, Instant.ofEpochMilli(cutOffMillis));
    SimpleFileVisitor<Path> sfv = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir,
          BasicFileAttributes attrs) {
        if (null == pathToExclude || !pathToExclude.equals(dir)) {
          return FileVisitResult.CONTINUE;
        } else {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
          throws IOException {
        String tempName = file.toString();
        if (cutOffMillis >= 0L
            && attrs.lastModifiedTime().toMillis() < cutOffMillis) {
          Files.delete(file);
        } else if (tempName.endsWith(TEMPFIX)) {
          Path outputPath = Paths
              .get(tempName.substring(0, tempName.length() - TEMPFIX.length()));
          Files.deleteIfExists(outputPath);
          Files.move(file, outputPath);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc)
          throws IOException {
        if (!pathToClean.equals(dir)
            && !Files.list(dir).findFirst().isPresent()) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    };
    try {
      Files.walkFileTree(pathToClean, sfv);
    } catch (IOException e) {
      logger.warn("Caught I/O exception while cleaning up directory {} with "
          + "cut-off time {}. Continuing.",
          pathToClean, Instant.ofEpochMilli(cutOffMillis), e);
    }
  }

  /** Return all date-time parts as array. */
  public static String[] dateTimeParts(long dateTime) {
    return dateTimeParts(new Date(dateTime));
  }

  /** Return all date-time parts as array. */
  public static String[] dateTimeParts(Date dateTime) {
    SimpleDateFormat printFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    return printFormat.format(dateTime).split("-");
  }

  /** Return all date-time as string. */
  public static String dateTime(long dateTime) {
    return dateTime(new Date(dateTime));
  }

  /** Return all date-time as string. */
  public static String dateTime(Date dateTime) {
    SimpleDateFormat printFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    return printFormat.format(dateTime);
  }

}
