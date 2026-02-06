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

package app.krista.extensions.essentials.collaboration.gmail.impl.util;

import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.model.base.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Implementation class for handling image processing operations in Gmail emails.
 * Provides functionality to convert HTML img tags to Base64 inline images
 * for email content processing.
 */
public class InlineImageConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InlineImageConverter.class);
    private final KristaMediaClient mediaClient;

    /**
     * Constructor for ImageImpl.
     *
     * @param mediaClient the KristaMediaClient for handling media operations
     */
    public InlineImageConverter(KristaMediaClient mediaClient) {
        this.mediaClient = mediaClient;
    }

    /**
     * This method finds all <img> tags in the given HTML content,
     * fetches the images from KristaMediaClient, converts them to Base64,
     * and replaces the original <img> tags with inline Base64 image tags.
     *
     * @param htmlContent the HTML content containing img tags
     * @return processed HTML content with Base64 inline images
     */
    public String convertImagesToBase64Html(String htmlContent) {
        Matcher matcher = Constants.IMG_PATTERN.matcher(htmlContent);
        StringBuilder finalHtml = new StringBuilder();
        int imageNumber = 1;
        while (matcher.find()) {
            String originalImgTag = matcher.group(0);
            String imageUrl = matcher.group(1);
            String replacementTag;
            try {
                if (mediaClient != null) {
                    replacementTag = generateInlineImage(imageUrl, originalImgTag, imageNumber);
                } else {
                    replacementTag = "[TEST: Image " + imageNumber + " placeholder]";
                }
            } catch (Exception e) {
                LOGGER.error("Failed to process image: {}", e.getMessage());
                replacementTag = "[IMAGE ERROR: Image " + imageNumber + "]";
            }
            matcher.appendReplacement(finalHtml, replacementTag);
            imageNumber++;
        }
        matcher.appendTail(finalHtml);
        return finalHtml.toString();
    }

    /**
     * Reads the image file from KristaMediaClient and builds an inline Base64 image tag.
     *
     * @param imageUrl the URL of the image
     * @param originalTag the original img tag
     * @param imageNumber the sequential number of the image
     * @return the Base64 inline image tag
     * @throws IOException if image processing fails
     */
    private String generateInlineImage(String imageUrl, String originalTag, int imageNumber) throws IOException {
        String mediaId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        File kristaFile = new File(0, "temp_" + UUID.randomUUID(), null, mediaId);
        java.io.File localFile = mediaClient.toJavaFile(kristaFile);
        if (localFile == null || !localFile.exists()) {
            return "[MISSING IMAGE: Image " + imageNumber + "]";
        }
        byte[] imageBytes = java.nio.file.Files.readAllBytes(localFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String altText = extractAltText(originalTag);
        if (altText == null) {
            altText = "Image";
        }
        return buildImgTag(base64Image, altText);
    }

    /**
     * Builds the <img> HTML tag with Base64 image data.
     *
     * @param base64Image the Base64 encoded image data
     * @param altText the alt text for the image
     * @return the complete img tag with Base64 data
     */
    private String buildImgTag(String base64Image, String altText) {
        return "<img src=\"data:image/*;base64," + base64Image +
                "\" alt=\"" + altText +
                "\" style=\"max-width:100%;height:auto;\"/>";
    }

    /**
     * Extracts alt text from an img tag.
     *
     * @param imgTag the img tag to extract alt text from
     * @return the alt text or "Image" as default
     */
    private String extractAltText(String imgTag) {
        try {
            Matcher altMatcher = Constants.ALT_PATTERN.matcher(imgTag);
            return altMatcher.find() ? altMatcher.group(1) : "Image";
        } catch (Exception e) {
            return null;
        }
    }
}
