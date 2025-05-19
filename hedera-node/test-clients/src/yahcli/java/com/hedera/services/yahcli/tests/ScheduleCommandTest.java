package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * Unit tests for the ScheduleCommand class.
 */
public class ScheduleCommandTest extends BaseCommandTest {

    private ScheduleCommand scheduleCommand;

    @Override
    protected void initializeCommand() {
        scheduleCommand = new ScheduleCommand();
        commandLine = new CommandLine(scheduleCommand);
    }

    @Test
    @DisplayName("ScheduleCommand should register all required subcommands")
    public void testSubcommandRegistration() {
        // Expected subcommands for schedule command
        String[] expectedSubcommands = {
                "create", "sign", "delete", "info"  // Assuming these are the schedule-related commands
        };

        for (String subcommand : expectedSubcommands) {
            assertNotNull(commandLine.getSubcommands().get(subcommand),
                    "Subcommand " + subcommand + " should be registered");
        }
    }

    @Test
    @DisplayName("ScheduleCommand should have description")
    public void testCommandDescription() {
        assertNotNull(commandLine.getCommandSpec().usageMessage().description());
        assertFalse(commandLine.getCommandSpec().usageMessage().description().length == 0,
                "Command should have a non-empty description");
    }

    @Test
    @DisplayName("ScheduleCommand should handle help flag")
    public void testHelpFlag() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("ScheduleCommand should require a subcommand")
    public void testRequireSubcommand() {
        // Set the CommandLine's spec to the instance
        scheduleCommand.spec = commandLine.getCommandSpec();

        // Call without a subcommand should throw an exception
        Exception exception = assertThrows(Exception.class, () -> {
            scheduleCommand.call();
        });

        assertTrue(exception.getMessage().contains("subcommand") ||
                        exception.getMessage().contains("command"),
                "Error should indicate a subcommand is required");
    }

    @Test
    @DisplayName("create subcommand should validate required arguments")
    public void testCreateSubcommandRequiredArgs() {
        CommandLine createSubcommand = commandLine.getSubcommands().get("create");

        Exception exception = assertThrows(Exception.class, () -> {
            createSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing"),
                "Error should indicate missing required parameters");
    }

    @Test
    @DisplayName("sign subcommand should require schedule ID")
    public void testSignSubcommandRequiresScheduleId() {
        CommandLine signSubcommand = commandLine.getSubcommands().get("sign");

        Exception exception = assertThrows(Exception.class, () -> {
            signSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("schedule"),
                "Error should indicate missing schedule ID parameter");
    }

    @Test
    @DisplayName("delete subcommand should require schedule ID")
    public void testDeleteSubcommandRequiresScheduleId() {
        CommandLine deleteSubcommand = commandLine.getSubcommands().get("delete");

        Exception exception = assertThrows(Exception.class, () -> {
            deleteSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("schedule"),
                "Error should indicate missing schedule ID parameter");
    }

    @Test
    @DisplayName("info subcommand should require schedule ID")
    public void testInfoSubcommandRequiresScheduleId() {
        CommandLine infoSubcommand = commandLine.getSubcommands().get("info");

        Exception exception = assertThrows(Exception.class, () -> {
            infoSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("schedule"),
                "Error should indicate missing schedule ID parameter");
    }
}