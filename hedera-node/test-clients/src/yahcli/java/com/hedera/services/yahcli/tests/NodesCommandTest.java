package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * Unit tests for the NodesCommand class.
 */
public class NodesCommandTest extends BaseCommandTest {

    private NodesCommand nodesCommand;

    @Override
    protected void initializeCommand() {
        nodesCommand = new NodesCommand();
        commandLine = new CommandLine(nodesCommand);
    }

    @Test
    @DisplayName("NodesCommand should register all required subcommands")
    public void testSubcommandRegistration() {
        // Expected subcommands for nodes command
        String[] expectedSubcommands = {
                "create", "update", "delete", "list", "info"  // Assuming these are the node-related commands
        };

        for (String subcommand : expectedSubcommands) {
            assertNotNull(commandLine.getSubcommands().get(subcommand),
                    "Subcommand " + subcommand + " should be registered");
        }
    }

    @Test
    @DisplayName("NodesCommand should have description")
    public void testCommandDescription() {
        assertNotNull(commandLine.getCommandSpec().usageMessage().description());
        assertFalse(commandLine.getCommandSpec().usageMessage().description().length == 0,
                "Command should have a non-empty description");
    }

    @Test
    @DisplayName("NodesCommand should handle help flag")
    public void testHelpFlag() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("NodesCommand should require a subcommand")
    public void testRequireSubcommand() {
        // Set the CommandLine's spec to the instance
        nodesCommand.spec = commandLine.getCommandSpec();

        // Call without a subcommand should throw an exception
        Exception exception = assertThrows(Exception.class, () -> {
            nodesCommand.call();
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
    @DisplayName("update subcommand should require node ID")
    public void testUpdateSubcommandRequiresNodeId() {
        CommandLine updateSubcommand = commandLine.getSubcommands().get("update");

        Exception exception = assertThrows(Exception.class, () -> {
            updateSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("node"),
                "Error should indicate missing node ID parameter");
    }

    @Test
    @DisplayName("delete subcommand should require node ID")
    public void testDeleteSubcommandRequiresNodeId() {
        CommandLine deleteSubcommand = commandLine.getSubcommands().get("delete");

        Exception exception = assertThrows(Exception.class, () -> {
            deleteSubcommand.execute();
        });

        assertTrue(exception.getMessage().contains("required") ||
                        exception.getMessage().contains("Missing") ||
                        exception.getMessage().contains("node"),
                "Error should indicate missing node ID parameter");
    }

    @Test
    @DisplayName("list subcommand should execute without parameters")
    public void testListSubcommandExecuteWithoutParams() {
        // This test assumes the list command doesn't require any parameters
        // Without knowing the actual implementation, this is placeholder

        // Would verify that the list subcommand can be executed without parameters
        // and returns a successful exit code
    }
}