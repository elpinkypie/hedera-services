package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * Unit tests for the FeesCommand class.
 */
public class FeesCommandTest extends BaseCommandTest {

    private FeesCommand feesCommand;

    @Override
    protected void initializeCommand() {
        feesCommand = new FeesCommand();
        commandLine = new CommandLine(feesCommand);
    }

    @Test
    @DisplayName("FeesCommand should register all required subcommands")
    public void testSubcommandRegistration() {
        // Expected subcommands for fees command
        String[] expectedSubcommands = {
                "set", "get", "calculate", "update"  // Assuming these are the fees-related commands
        };

        for (String subcommand : expectedSubcommands) {
            assertNotNull(commandLine.getSubcommands().get(subcommand),
                    "Subcommand " + subcommand + " should be registered");
        }
    }

    @Test
    @DisplayName("FeesCommand should have description")
    public void testCommandDescription() {
        assertNotNull(commandLine.getCommandSpec().usageMessage().description());
        assertFalse(commandLine.getCommandSpec().usageMessage().description().length == 0,
                "Command should have a non-empty description");
    }

    @Test
    @DisplayName("FeesCommand should handle help flag")
    public void testHelpFlag() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("FeesCommand should require a subcommand")
    public void testRequireSubcommand() {
        // Set the CommandLine's spec to the instance
        feesCommand.spec = commandLine.getCommandSpec();

        // Call without a subcommand should throw an exception
        Exception exception = assertThrows(Exception.class, () -> {
            feesCommand.call();
        });

        assertTrue(exception.getMessage().contains("subcommand") ||
                        exception.getMessage().contains("command"),
                "Error should indicate a subcommand is required");
    }

    @Test
    @DisplayName("set subcommand should validate required arguments")
    public void testSetSubcommandRequiredArgs() {
        CommandLine setSubcommand = commandLine.getSubcommands().get("set");

        Exception exception = assertThrows(Exception.class, () -> {
            setSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing"),
                "Error should indicate missing required parameters");
    }

    @Test
    @DisplayName("get subcommand should require service type")
    public void testGetSubcommandRequiresServiceType() {
        CommandLine getSubcommand = commandLine.getSubcommands().get("get");

        Exception exception = assertThrows(Exception.class, () -> {
            getSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("service"),
                "Error should indicate missing service type parameter");
    }

    @Test
    @DisplayName("calculate subcommand should require service type")
    public void testCalculateSubcommandRequiresServiceType() {
        CommandLine calculateSubcommand = commandLine.getSubcommands().get("calculate");

        Exception exception = assertThrows(Exception.class, () -> {
            calculateSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("service"),
                "Error should indicate missing service type parameter");
    }

    @Test
    @DisplayName("update subcommand should validate required arguments")
    public void testUpdateSubcommandRequiredArgs() {
        CommandLine updateSubcommand = commandLine.getSubcommands().get("update");

        Exception exception = assertThrows(Exception.class, () -> {
            updateSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing"),
                "Error should indicate missing required parameters");
    }
}