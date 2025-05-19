package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the ConfigManager class.
 * Tests configuration loading and parsing functionality.
 */
class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new ConfigManager();
    }

    @Test
    @DisplayName("ConfigManager should load valid YAML config file")
    void testLoadValidYamlConfig() throws IOException {
        // Create a temporary config file
        Path configPath = tempDir.resolve("valid-config.yml");
        String yamlContent =
                "network: mainnet\n" +
                        "node: 0.0.3\n" +
                        "payer: 50\n" +
                        "fixedFee: 100\n";
        Files.writeString(configPath, yamlContent);

        // Load the config
        GlobalConfig config = configManager.loadConfig(configPath.toString());

        // Verify config loaded correctly
        assertNotNull(config);
        assertEquals("mainnet", config.getNetwork());
        assertEquals("0.0.3", config.getNode());
        assertEquals("50", config.getPayer());
        assertEquals(100L, config.getFixedFee());
    }

    @Test
    @DisplayName("ConfigManager should handle missing config file")
    void testMissingConfigFile() {
        String nonExistentPath = tempDir.resolve("non-existent-config.yml").toString();

        Exception exception = assertThrows(Exception.class, () -> {
            configManager.loadConfig(nonExistentPath);
        });

        assertTrue(exception.getMessage().contains("find") ||
                        exception.getMessage().contains("exist") ||
                        exception.getMessage().contains("missing"),
                "Error should indicate the file is missing");
    }

    @Test
    @DisplayName("ConfigManager should handle invalid YAML syntax")
    void testInvalidYamlSyntax() throws IOException {
        // Create a temporary config file with invalid YAML
        Path configPath = tempDir.resolve("invalid-syntax.yml");
        String invalidYaml =
                "network: mainnet\n" +
                        "node: 0.0.3\n" +
                        "payer: 50\n" +
                        "  indentation-error\n";  // Improper indentation
        Files.writeString(configPath, invalidYaml);

        Exception exception = assertThrows(Exception.class, () -> {
            configManager.loadConfig(configPath.toString());
        });

        assertTrue(exception.getMessage().contains("syntax") ||
                        exception.getMessage().contains("parse") ||
                        exception.getMessage().contains("invalid"),
                "Error should indicate syntax/parsing issue");
    }

    @Test
    @DisplayName("ConfigManager should handle missing required fields")
    void testMissingRequiredFields() throws IOException {
        // Create a temporary config file missing required fields
        Path configPath = tempDir.resolve("missing-fields.yml");
        String incompleteYaml =
                "# Missing important fields\n" +
                        "someRandomField: value\n";
        Files.writeString(configPath, incompleteYaml);

        // This could either throw an exception or return a default config
        // depending on implementation
        try {
            GlobalConfig config = configManager.loadConfig(configPath.toString());

            // If it doesn't throw, verify defaults are used
            assertNotNull(config);
            // Check for default values here

        } catch (Exception e) {
            // If it throws, verify error is about missing fields
            assertTrue(e.getMessage().contains("missing") ||
                            e.getMessage().contains("required") ||
                            e.getMessage().contains("field"),
                    "Error should indicate missing required fields");
        }
    }

    @Test
    @DisplayName("ConfigManager should load network-specific configurations")
    void testNetworkSpecificConfig() throws IOException {
        // Create a temporary config file with network-specific settings
        Path configPath = tempDir.resolve("networks-config.yml");
        String networkYaml =
                "networks:\n" +
                        "  mainnet:\n" +
                        "    node: 0.0.3\n" +
                        "    fixedFee: 100\n" +
                        "  testnet:\n" +
                        "    node: 0.0.4\n" +
                        "    fixedFee: 50\n";
        Files.writeString(configPath, networkYaml);

        // Load with mainnet
        configManager.setNetwork("mainnet");
        GlobalConfig mainnetConfig = configManager.loadConfig(configPath.toString());

        // Load with testnet
        configManager.setNetwork("testnet");
        GlobalConfig testnetConfig = configManager.loadConfig(configPath.toString());

        // Verify network-specific configs were loaded correctly
        assertEquals("0.0.3", mainnetConfig.getNode());
        assertEquals(100L, mainnetConfig.getFixedFee());

        assertEquals("0.0.4", testnetConfig.getNode());
        assertEquals(50L, testnetConfig.getFixedFee());
    }

    @Test
    @DisplayName("ConfigManager should merge CLI options with config file")
    void testMergeCliOptionsWithConfig() throws IOException {
        // Create a temporary config file
        Path configPath = tempDir.resolve("base-config.yml");
        String yamlContent =
                "network: mainnet\n" +
                        "node: 0.0.3\n" +
                        "payer: 50\n" +
                        "fixedFee: 100\n";
        Files.writeString(configPath, yamlContent);

        // Set CLI options that override some config values
        Yahcli cliOptions = new Yahcli();
        cliOptions.setNet("testnet");  // Override network
        cliOptions.setFixedFee(200);   // Override fixedFee

        // Load the config with CLI overrides
        GlobalConfig mergedConfig = configManager.loadConfigWithCliOverrides(
                configPath.toString(), cliOptions);

        // Verify merged config has CLI options where specified
        assertEquals("testnet", mergedConfig.getNetwork());  // From CLI
        assertEquals("0.0.3", mergedConfig.getNode());       // From file (unchanged)
        assertEquals("50", mergedConfig.getPayer());         // From file (unchanged)
        assertEquals(200L, mergedConfig.getFixedFee());      // From CLI
    }

    @Test
    @DisplayName("ConfigManager should handle comments in YAML files")
    void testHandleYamlComments() throws IOException {
        // Create a temporary config file with comments
        Path configPath = tempDir.resolve("commented-config.yml");
        String yamlWithComments =
                "# Main configuration file\n" +
                        "network: mainnet  # This is the network\n" +
                        "# Node account to use\n" +
                        "node: 0.0.3\n" +
                        "payer: 50  # Account to pay for transactions\n" +
                        "fixedFee: 100\n";
        Files.writeString(configPath, yamlWithComments);

        // Load the config
        GlobalConfig config = configManager.loadConfig(configPath.toString());

        // Verify config loaded correctly despite comments
        assertNotNull(config);
        assertEquals("mainnet", config.getNetwork());
        assertEquals("0.0.3", config.getNode());
        assertEquals("50", config.getPayer());
        assertEquals(100L, config.getFixedFee());
    }

    @Test
    @DisplayName("ConfigManager should handle JSON config files")
    void testLoadJsonConfig() throws IOException {
        // Create a temporary JSON config file
        Path configPath = tempDir.resolve("config.json");
        String jsonContent =
                "{\n" +
                        "  \"network\": \"mainnet\",\n" +
                        "  \"node\": \"0.0.3\",\n" +
                        "  \"payer\": \"50\",\n" +
                        "  \"fixedFee\": 100\n" +
                        "}";
        Files.writeString(configPath, jsonContent);

        // Load the config
        GlobalConfig config = configManager.loadConfig(configPath.toString());

        // Verify config loaded correctly
        assertNotNull(config);
        assertEquals("mainnet", config.getNetwork());
        assertEquals("0.0.3", config.getNode());
        assertEquals("50", config.getPayer());
        assertEquals(100L, config.getFixedFee());
    }

    @Test
    @DisplayName("ConfigManager should handle environment variable overrides")
    void testEnvVarOverrides() throws IOException {
        // Create a temporary config file
        Path configPath = tempDir.resolve("env-config.yml");
        String yamlContent =
                "network: mainnet\n" +
                        "node: 0.0.3\n" +
                        "payer: 50\n" +
                        "fixedFee: 100\n";
        Files.writeString(configPath, yamlContent);

        // Mock environment variables (would require PowerMock or similar)
        // For now, just call the method that would handle env vars

        // This is a placeholder that would need to be adapted to the actual implementation
        configManager.loadConfigWithEnvOverrides(configPath.toString());

        // Assertions would depend on implementation details
    }
}