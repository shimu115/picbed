package com.picbed.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class OssUtil {

    private OssUtil() {}

    public enum ImageType {
        PNG("image/png", "png"),
        JPEG("image/jpeg", "jpg", "jpeg"),
        GIF("image/gif", "gif"),
        WEBP("image/webp", "webp"),
        SVG("image/svg+xml", "svg"),
        BMP("image/bmp", "bmp"),
        TIFF("image/tiff", "tiff", "tif");

        private final String contentType;
        private final String[] extensions;

        ImageType(String contentType, String... extensions) {
            this.contentType = contentType;
            this.extensions = extensions;
        }

        public String getContentType() { return contentType; }
        public String[] getExtensions() { return extensions; }

        public static boolean isAllowedContentType(String contentType) {
            for (ImageType t : values()) {
                if (t.contentType.equalsIgnoreCase(contentType)) {
                    return true;
                }
            }
            return false;
        }

        public static Set<String> allowedContentTypes() {
            return Arrays.stream(values())
                    .map(ImageType::getContentType)
                    .collect(Collectors.toSet());
        }
    }

    public static String generateOssKey(String originalFilename) {
        String safeName = sanitizeFilename(originalFilename);
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "picbed/" + datePrefix + "/" + uuid + "-" + safeName;
    }

    public static String getPublicUrl(String bucketName, String endpoint, String customDomain, String ossKey) {
        if (customDomain != null && !customDomain.isBlank()) {
            String domain = customDomain.replaceFirst("^https?://", "").replaceAll("/+$", "");
            return "https://" + domain + "/" + ossKey;
        }
        return "https://" + bucketName + "." + endpoint + "/" + ossKey;
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "untitled";
        }
        String name = filename.replace("\\", "/");
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        name = name.replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_");
        if (name.length() > 255) {
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                String ext = name.substring(dotIndex);
                String base = name.substring(0, dotIndex);
                name = base.substring(0, Math.min(250, base.length())) + ext;
            } else {
                name = name.substring(0, 255);
            }
        }
        return name.isEmpty() ? "untitled" : name;
    }
}
