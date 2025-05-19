package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * Unit tests for the SystemCommand class.
 */
public class SystemCommandTest extends BaseCommandTest {

    private SystemCommand systemCommand;

    @Override
    protected void initializeCommand() {
        systemCommand = new SystemCommand();
        commandLine = new CommandLine(systemCommand);
    }

    @Test
    @DisplayName("SystemCommand should register all required subcommands")
    public void testSubcommandRegistration() {
        // Expected subcommands for system command
        String[] expectedSubcommands = {
                "freeze-abort", "freeze-only", "prepare-upgrade", "freeze-upgrade",
                "upgrade-telemetry", "activate-staking"
        };

        for (String subcommand : expectedSubcommands) {
            assertNotNull(commandLine.getSubcommands().get(subcommand),
                    "Subcommand " + subcommand + " should be registered");
        }
    }

    @Test
    @DisplayName("SystemCommand should have description")
    public void testCommandDescription() {
        assertNotNull(commandLine.getCommandSpec().usageMessage().description());
        assertFalse(commandLine.getCommandSpec().usageMessage().description().length == 0,
                "Command should have a non-empty description");
    }

    @Test
    @DisplayName("SystemCommand should handle help flag")
    public void testHelpFlag() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("SystemCommand should require a subcommand")
    public void testRequireSubcommand() {
        // Set the CommandLine's spec to the instance
        systemCommand.spec = commandLine.getCommandSpec();

        // Call without a subcommand should throw an exception
        Exception exception = assertThrows(Exception.class, () -> {
            systemCommand.call();
        });

        assertTrue(exception.getMessage().contains("subcommand") ||
                        exception.getMessage().contains("command"),
                "Error should indicate a subcommand is required");
    }

    @Test
    @DisplayName("System commands should require admin privileges or confirmation")
    public void testSystemCommandsRequireConfirmation() {
        // This would require knowledge of how the confirmation mechanism works
        // Just providing a template here

        // Example: Test that freeze commands require confirmation
        CommandLine freezeCmd = commandLine.getSubcommands().get("freeze-only");

        Exception exception = assertThrows(Exception.class, () -> {
            freezeCmd.execute();
        });

        assertTrue(exception.getMessage().contains("confirmation") ||
                        exception.getMessage().contains("admin") ||
                        exception.getMessage().contains("required"),
                "Error should indicate confirmation or privileges required");
    }

    @Test
    @DisplayName("prepare-upgrade should validate required file arguments")
    public void testPrepareUpgradeRequiredArgs() {
        CommandLine prepareUpgradeCmd = commandLine.getSubcommands().get("prepare-upgrade");

        Exception exception = assertThrows(Exception.class, () -> {
            prepareUpgradeCmd.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("file"),
                "Error should indicate missing file parameters");
    }

    @Test
    @DisplayName("freeze-upgrade should validate required file arguments")
    public void testFreezeUpgradeRequiredArgs() {
        CommandLine freezeUpgradeCmd = commandLine.getSubcommands().get("freeze-upgrade");

        Exception exception = assertThrows(Exception.class, () -> {
            freezeUpgradeCmd.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("file"),
                "Error should indicate missing file parameters");
    }

    @Test
    @DisplayName("upgrade-telemetry should validate required arguments")
    public void testUpgradeTelemetryRequiredArgs() {
        CommandLine telemetryCmd = commandLine.getSubcommands().get("upgrade-telemetry");

        Exception exception = assertThrows(Exception.class, () -> {
            telemetryCmd.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing"),
                "Error should indicate missing required parameters");
    }

    @Test
    @DisplayName("SystemCommand should provide meaningful error for invalid subcommand")
    public void testInvalidSubcommand() {
        Exception exception = assertThrows(Exception.class, () -> {
            commandLine.execute("nonexistent-command");
        });

        assertTrue(exception.getMessage().contains("Unknown") ||
                        exception.getMessage().contains("not recognized") ||
                        exception.getMessage().contains("invalid"),
                "Error should indicate unknown command");
    }
}