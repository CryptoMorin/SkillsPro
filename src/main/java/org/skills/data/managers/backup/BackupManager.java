/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.skills.data.managers.backup;

import org.skills.main.locale.MessageHandler;
import org.skills.utils.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * <b>BackupManager - v1.0</b> - A powerful concurrent file compression using NIO.2.
 * Java 8 NIO.2: https://docs.oracle.com/javase/tutorial/essential/io/fileio.html
 * <p>
 * The simplest way of doing this is to use {@link #takeBackup()}.
 * You can also override the method and handle it yourself.
 * @author Crypto Morin
 * @version 1.0.0
 */
public abstract class BackupManager {
    protected static final String TIME_PATTERN = "yyyy-MM-dd";
    protected static boolean useMultiBackups = true;
    protected final Path backups;
    protected final Path toBackup;

    public BackupManager(File backups, File toBackup) {
        Objects.requireNonNull(backups, "Backups directory cannot be null");
        Objects.requireNonNull(toBackup, "Cannot backup null directory");

        this.backups = backups.toPath();
        validateDir();
        this.toBackup = toBackup.toPath();
    }

    /**
     * Same as {@link File#mkdirs()} but NIO.
     * @since 1.0.0
     */
    private void validateDir() {
        try {
            Files.createDirectories(this.backups);
        } catch (IOException e) {
            MessageHandler.sendConsolePluginMessage("&4Failed to create backups directory.");
            e.printStackTrace();
        }
    }

    /**
     * Gets the concurrent date using {@link #TIME_PATTERN} format.
     * @return today's date.
     * @since 1.0.0
     */
    public String getDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    /**
     * Uncompresses all the files from the given path to the given directory asynchronous.
     * @param zip     the ZIP file path.
     * @param unzipTo the directory to copy the uncompressed files to.
     * @see #zipFiles()
     * @since 1.0.0
     */
    public CompletableFuture<Void> unzipFiles(Path zip, Path unzipTo) {
        Objects.requireNonNull(zip, "Cannot unzip null directory.");
        Objects.requireNonNull(unzipTo, "Cannot unzip to null directory");
        if (!zip.toString().toLowerCase().endsWith(".zip"))
            throw new IllegalArgumentException("ZIP path must refer to a ZIP file");
        if (Files.exists(unzipTo) && !Files.isDirectory(unzipTo))
            throw new IllegalArgumentException("Cannot unzip to a non-directory");

        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(unzipTo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (ZipFile zipFile = new ZipFile(zip.toString())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        Path path = unzipTo.resolve(entry.getName());

                        Files.createDirectories(path);
                        Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * The default method to take a clean backup.
     * You can override for a custom handler.
     * @since 1.0.0
     */
    public void takeBackup() {
        if (!useMultiBackups && hasBackupToday()) return;
        deleteOldBackups(30, TimeUnit.DAYS);
        zipFiles();
    }

    /**
     * Compresses all the files in the given directory of {@link #toBackup} asynchronous.
     * @return the amount of files that were successfully backed up.
     * @see #unzipFiles(Path, Path)
     * @since 1.0.0
     */
    public CompletableFuture<Integer> zipFiles() {
        validateDir();
        AtomicInteger backedUp = new AtomicInteger();

        Path sourcePath = getZip();
        try {
            Files.createFile(sourcePath);
        } catch (IOException ex) {
            MessageHandler.sendConsolePluginMessage("&4Error while attempting to create ZIP file.");
            ex.printStackTrace();
        }

        return CompletableFuture.supplyAsync(() -> {
            try (ZipOutputStream zs = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(sourcePath)))) {
                zs.setLevel(Deflater.BEST_COMPRESSION);
                zs.setComment("A backup file for Skills minecraft plugin data.\n" +
                        "These backups contain language file, config.yml and players data\n" +
                        "depending on the options specified in the config.\n\n" +
                        "Note that you have to stop the server before restoring one of these backups.\n" +
                        "Backup taken at: " + StringUtils.getFullTime());

                Files.walkFileTree(toBackup, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return isWhitelistedDirectory(dir) || dir == toBackup ?
                                FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (!isWhitelistedFile(file)) return FileVisitResult.SKIP_SUBTREE;
                        ZipEntry zipEntry = new ZipEntry(toBackup.relativize(file).toString());

                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(file, zs);
                            zs.closeEntry();
                            backedUp.getAndIncrement();
                        } catch (IOException e) {
                            MessageHandler.sendConsolePluginMessage(
                                    "&4Error while attempting to backup a file&8: &e" + file.getFileName());
                            e.printStackTrace();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                MessageHandler.sendConsolePluginMessage("&4Error while attempting to take a backup...");
                e.printStackTrace();
            }

            return backedUp.get();
        });
    }

    /**
     * Gets a new path with the format of {@link #getZipPath()} including an additional<br>
     * number with the format of <b>(x)</b> where <i>x</i> is starts from 1 only if<br>
     * {@link #getZipPath()} already exists.
     * @return a new path if {@link #getZipPath()} already exists, otherwise {@link #getZipPath()} with no additional number.
     * @since 1.0.0
     */
    public Path getMultiZipName() {
        int count = 1;
        Path file = getZipPath();
        Path parent = file.getParent();
        String name = file.getFileName().toString();
        name = name.substring(0, name.lastIndexOf('.'));
        String extension = ".zip";

        while (Files.exists(file))
            file = parent.resolve(name + " (" + count++ + ")" + extension);
        //Paths.get(parent.toString(), name + " (" + count++ + ")" + extension);

        return file;
    }

    /**
     * When taking a backup from the files in the directory, only the contents inside these<br>
     * directories will be checked and passed to {@link #isWhitelistedFile(Path)}
     * @param file the directory that we're about to enter.
     * @return true if the contents inside this directory should be accepted.
     * @since 1.0.0
     */
    public abstract boolean isWhitelistedDirectory(Path file);

    /**
     * When taking a backup from the files in the directory, only these files are accepted.
     * @param file the file that we're about to backup.
     * @return true if the specified path should be accepted.
     * @since 1.0.0
     */
    public abstract boolean isWhitelistedFile(Path file);

    /**
     * Checks if the specified backup file should be deleted if it's an old backup.
     * @param path     the backup file's path.
     * @param time     any files older than this time.
     * @param timeUnit the time unit for time parameter..
     * @return true if this backup should be deleted, otherwise false.
     * @since 1.0.0
     */
    public boolean shouldBeDeleted(Path path, int time, TimeUnit timeUnit) {
        long created;
        try {
            created = Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        long diff = System.currentTimeMillis() - created;
        return diff >= timeUnit.toMillis(time);
    }

    /**
     * Deletes all the backups that are older than the specified time.<br><br>
     * Note that {@link #getMultiZipName()} will replace the deleted files when<br>
     * checking through the files. It's recommended to change {@link #TIME_PATTERN} to use<br>
     * "yyyy-MM-dd hh" to display the hours as well. This task is asynchronous.
     * @param time     any files older than this time.
     * @param timeUnit the time unit for time parameter.
     * @since 1.0.0
     */
    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> deleteOldBackups(int time, TimeUnit timeUnit) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.walk(backups).filter(Files::isRegularFile).forEach(f -> {
                    if (shouldBeDeleted(f, time, timeUnit)) {
                        try {
                            MessageHandler.sendConsolePluginMessage("&2Deleting old backup... &6" + f.getFileName());
                            Files.delete(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gets the backup ZIP path for today.
     * @return a path including today's date with ZIP file extension.
     * @since 1.0.0
     */
    public Path getZipPath() {
        return backups.resolve(getDate() + ".zip");
    }

    /**
     * Gets the final ZIP path.
     * @return default ZIP path or multi ZIP path if enabled.
     * @see #getZipPath()
     * @see #getMultiZipName()
     * @since 1.0.0
     */
    public Path getZip() {
        return useMultiBackups ? getMultiZipName() : getZipPath();
    }

    /**
     * Checks if the backup file for today exists considering<br>
     * multi backup files if enabled. So you shouldn't use this method<br>
     * when taking a backup if you enabled  multi backups.
     * @return true if the backup file exists, otherwise false.
     * @since 1.0.0
     */
    public boolean hasBackupToday() {
        return Files.exists(getZip());
    }
}