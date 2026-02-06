/*
 * Gmail Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.essentials.collaboration.gmail.impl.stores;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Client for managing file operations between Krista's media server and local file system.
 * Handles file uploads, downloads, and format conversions including automatic compression
 * of unsupported file formats. Provides bidirectional conversion between Krista File objects
 * and Java File objects for seamless integration with Gmail attachment handling.
 */
@Service
public class KristaMediaClient {

    private static final String TMP = "/tmp/";
    private static final Logger LOGGER = LoggerFactory.getLogger(KristaMediaClient.class);
    private final List<String> unSupportedFileFormats = Arrays.asList("html", "php5", "pht", "phtml", "shtml", "asa", "cer", "asax", "swf", "xap", "jsp", "exe", "js");
    private final FileRepository fileRepository;

    /**
     * Constructor for dependency injection.
     * Initializes the client with the required file repository for media operations.
     *
     * @param fileRepository the Krista file repository for media server operations
     */
    @Inject
    public KristaMediaClient(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Compresses a file into a zip archive.
     * Creates a zip file containing the specified file, useful for packaging
     * unsupported file formats or reducing file size.
     *
     * @param zipFilePath  the path where the zip file will be created
     * @param dirPathToZip the path of the file to be zipped
     * @throws IOException if compression fails or file operations encounter errors
     */
    public static void compressFile(String zipFilePath, String dirPathToZip) throws IOException {
        FileInputStream fis = null;
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            File fileToZip = new File(dirPathToZip);
            fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            zipOut.closeEntry();
        } finally {
            Objects.requireNonNull(fis).close();
        }
    }

    /**
     * Uploads a Java File to Krista's media server.
     * Automatically compresses unsupported file formats before upload.
     * Returns a Krista File object that can be used in the platform.
     *
     * @param file the Java File object to upload
     * @return Krista File object representing the uploaded file
     * @throws IOException if upload fails or file operations encounter errors
     */
    public app.krista.model.base.File toKristaFile(File file) throws IOException {
        if (isUnsupportedFileFormat(file.getName())) {
            String zipFilePath = TMP + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip";
            compressFile(zipFilePath, file.getAbsolutePath());
            file = new File(zipFilePath);
        }
        try (final FileHandle fileHandle = fileRepository.createNewFileByName(file.getName())) {
            fileHandle.setContent(new FileInputStream(file));
            return fileHandle.getFile();
        }
    }

    /**
     * Downloads a Krista File from the media server to local file system.
     * Converts a Krista File object to a Java File object for local processing.
     *
     * @param file the Krista File object to download
     * @return Java File object containing the downloaded content
     * @throws IOException if download fails or file operations encounter errors
     */
    public File toJavaFile(app.krista.model.base.File file) throws IOException {
        try (FileHandle fileHandle = fileRepository.getFile(file)) {
            InputStream content = fileHandle.getContent();
            final File input = new File(file.getFileName());
            return convertInputStreamToFile(content, input);
        }
    }

    /**
     * Converts an InputStream to a Java File object.
     * Writes the stream content to a temporary file for local access.
     *
     * @param fileName The name of the file.
     * @return True if the file format is unsupported, otherwise false.
     */
    private boolean isUnsupportedFileFormat(String fileName) {
        if (fileName.lastIndexOf(".") == -1) {
            return false;
        }
        String fileExtension = getFileExtension(fileName);
        return unSupportedFileFormats.contains(fileExtension);
    }

    /**
     * Converts an input stream to a file.
     *
     * @param inputStream The input stream to be converted.
     * @param input       The file to write the input stream content to.
     * @return The file with the content of the input stream.
     * @throws IOException If an I/O error occurs.
     */
    private File convertInputStreamToFile(InputStream inputStream, File input) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(input)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return input;
    }

    /**
     * Retrieves the file extension from the file name.
     *
     * @param fileName The name of the file.
     * @return The file extension.
     * @throws IllegalArgumentException If the file format is unsupported.
     */
    private String getFileExtension(String fileName) {
        try {
            LOGGER.info("File extension Name :: {} ", fileName);
            if (fileName.contains(".")) {
                return fileName.substring((fileName.lastIndexOf(".") + 1));
            }
            return fileName;
        } catch (IllegalArgumentException cause) {
            LOGGER.error("Unsupported file format");
            throw new IllegalArgumentException("Unsupported file format");
        }
    }
}