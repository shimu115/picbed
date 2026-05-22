package com.picbed.config;

import com.picbed.util.TokenUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SetupTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SetupTokenManager.class);
    private static final Path SECURITY_FILE = Paths.get("data", "security.txt");

    private String setupToken;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(SECURITY_FILE.getParent());
            if (Files.exists(SECURITY_FILE)) {
                String content = Files.readString(SECURITY_FILE).trim();
                for (String line : content.split("\n")) {
                    line = line.trim();
                    if (line.startsWith("accessKey=")) {
                        setupToken = line.substring("accessKey=".length()).trim();
                        break;
                    }
                }
                if (setupToken != null && !setupToken.isBlank()) {
                    log.info("Setup token loaded from {}", SECURITY_FILE.toAbsolutePath());
                    return;
                }
            }
            setupToken = TokenUtil.generateRawToken();
            String fileContent = "# PicBed Master Setup Token\n"
                    + "# This file was auto-generated. Keep it secret.\n"
                    + "accessKey=" + setupToken + "\n";
            Files.writeString(SECURITY_FILE, fileContent);
            log.info("==============================================");
            log.info("  Master setup token generated:");
            log.info("  {}", setupToken);
            log.info("  Saved to: {}", SECURITY_FILE.toAbsolutePath());
            log.info("  Use this token with X-Setup-Token header");
            log.info("  to create your first auth token via /api/setup/token");
            log.info("==============================================");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize setup token", e);
        }
    }

    public boolean validate(String token) {
        return setupToken != null && setupToken.equals(token);
    }
}
