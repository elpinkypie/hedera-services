package com.hedera.services.yahcli.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import com.hedera.services.yahcli.Yahcli;
import com.hedera.services.yahcli.commands.accounts.AccountsCommand;
import com.hedera.services.yahcli.commands.accounts.BalanceCommand;
import com.hedera.services.yahcli.commands.accounts.CreateCommand;
import com.hedera.services.yahcli.commands.accounts.InfoCommand;
import com.hedera.services.yahcli.commands.accounts.RekeyCommand;
import com.hedera.services.yahcli.commands.accounts.SendCommand;
import com.hedera.services.yahcli.commands.accounts.StakeCommand;
import com.hedera.services.yahcli.commands.accounts.UpdateCommand;

/**
 * Unit tests for the AccountsCommand class.
 */
public class AccountsCommandTest extends BaseCommandTest {

    private AccountsCommand accountsCommand;

    @Override
    protected void initializeCommand() {
        accountsCommand = new AccountsCommand();
        commandLine = new CommandLine(accountsCommand);

        // Set the parent Yahcli instance
        Yahcli yahcli = createYahcliWithCommandSpec();
        setField(accountsCommand, "yahcli", yahcli);
    }

    @Test
    @DisplayName("AccountsCommand should register all required subcommands")
    public void testSubcommandRegistration() {
        // Expected subcommands for accounts command based on the actual implementation
        String[] expectedSubcommands = {
                "help", "balance", "info", "rekey", "send", "create", "stake", "update"
        };

        for (String subcommand : expectedSubcommands) {
            assertNotNull(commandLine.getSubcommands().get(subcommand),
                    "Subcommand " + subcommand + " should be registered");
        }
    }

    @Test
    @DisplayName("AccountsCommand should register subcommands with correct classes")
    public void testSubcommandClasses() {
        assertEquals(CommandLine.HelpCommand.class,
                commandLine.getSubcommands().get("help").getCommandSpec().userObject().getClass());
        assertEquals(BalanceCommand.class,
                commandLine.getSubcommands().get("balance").getCommandSpec().userObject().getClass());
        assertEquals(InfoCommand.class,
                commandLine.getSubcommands().get("info").getCommandSpec().userObject().getClass());
        assertEquals(RekeyCommand.class,
                commandLine.getSubcommands().get("rekey").getCommandSpec().userObject().getClass());
        assertEquals(SendCommand.class,
                commandLine.getSubcommands().get("send").getCommandSpec().userObject().getClass());
        assertEquals(CreateCommand.class,
                commandLine.getSubcommands().get("create").getCommandSpec().userObject().getClass());
        assertEquals(StakeCommand.class,
                commandLine.getSubcommands().get("stake").getCommandSpec().userObject().getClass());
        assertEquals(UpdateCommand.class,
                commandLine.getSubcommands().get("update").getCommandSpec().userObject().getClass());
    }

    @Test
    @DisplayName("AccountsCommand should have description")
    public void testCommandDescription() {
        String description = commandLine.getCommandSpec().usageMessage().description()[0];
        assertEquals("Performs account operations", description,
                "Description should match expected value");
    }

    @Test
    @DisplayName("AccountsCommand should handle help flag")
    public void testHelpFlag() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("AccountsCommand should require a subcommand")
    public void testRequireSubcommand() {
        // Setup with mock Yahcli using the helper method
        Yahcli mockYahcli = createMockYahcliForErrors();
        setField(accountsCommand, "yahcli", mockYahcli);

        // Call without a subcommand should throw an exception
        Exception exception = assertThrows(CommandLine.ParameterException.class, () -> {
            accountsCommand.call();
        });

        assertTrue(exception.getMessage().contains("Please specify an accounts subcommand"),
                "Error should indicate a subcommand is required");
    }

    @Test
    @DisplayName("AccountsCommand should provide access to parent Yahcli instance")
    public void testYahcliAccess() {
        Yahcli mockYahcli = mock(Yahcli.class);
        setField(accountsCommand, "yahcli", mockYahcli);

        assertEquals(mockYahcli, accountsCommand.getYahcli(),
                "getYahcli should return the assigned Yahcli instance");
    }

    @Test
    @DisplayName("AccountsCommand should pass parent options to subcommands")
    public void testParentOptionsPassThrough() {
        // Create a parent command with options
        Yahcli parentCommand = new Yahcli();
        setField(parentCommand, "net", "testnet");
        setField(parentCommand, "payer", "50");
        setField(parentCommand, "spec", new CommandLine(parentCommand).getCommandSpec());

        // Create a new command line with parent and child
        CommandLine parentCli = new CommandLine(parentCommand);
        parentCli.addSubcommand("accounts", accountsCommand);

        // Execute with a subcommand that outputs help
        parentCli.execute("accounts", "help");

        // Verify that the output contains the expected help information
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("AccountsCommand balance subcommand should require account parameter")
    public void testBalanceSubcommandRequiresAccount() {
        CommandLine balanceCmd = commandLine.getSubcommands().get("balance");

        Exception exception = assertThrows(Exception.class, () -> {
            balanceCmd.execute();
        });

        assertTrue(exception.getMessage().contains("<accounts>") ||
                        exception.getMessage().contains("Missing required parameter"),
                "Error should indicate missing account parameter");
    }

    @Test
    @DisplayName("AccountsCommand info subcommand should require account parameter")
    public void testInfoSubcommandRequiresAccount() {
        CommandLine infoCmd = commandLine.getSubcommands().get("info");

        Exception exception = assertThrows(Exception.class, () -> {
            infoCmd.execute();
        });

        assertTrue(exception.getMessage().contains("<accounts>") ||
                        exception.getMessage().contains("Missing required parameter"),
                "Error should indicate missing account parameter");
    }

    @Test
    @DisplayName("AccountsCommand create subcommand should have required parameters")
    public void testCreateSubcommandParameters() {
        CommandLine createCmd = commandLine.getSubcommands().get("create");

        assertTrue(createCmd.getCommandSpec().optionsMap().containsKey("-m"),
                "Create command should have -m/--memo option");
        assertTrue(createCmd.getCommandSpec().optionsMap().containsKey("-a"),
                "Create command should have -a/--amount option");
        assertTrue(createCmd.getCommandSpec().optionsMap().containsKey("-d"),
                "Create command should have -d/--denomination option");
        assertTrue(createCmd.getCommandSpec().optionsMap().containsKey("-k"),
                "Create command should have -k/--keyType option");
    }

    @Test
    @DisplayName("AccountsCommand rekey subcommand should have required options")
    public void testRekeySubcommandOptions() {
        CommandLine rekeyCmd = commandLine.getSubcommands().get("rekey");

        // Verify rekey has the right options
        assertTrue(rekeyCmd.getCommandSpec().optionsMap().containsKey("-k"),
                "Rekey command should have -k/--replacement-key option");
        assertTrue(rekeyCmd.getCommandSpec().optionsMap().containsKey("-g"),
                "Rekey command should have -g/--gen-new-key option");
    }

    @Test
    @DisplayName("AccountsCommand stake subcommand should have required options")
    public void testStakeSubcommandOptions() {
        CommandLine stakeCmd = commandLine.getSubcommands().get("stake");

        // Verify stake has the right options
        assertTrue(stakeCmd.getCommandSpec().optionsMap().containsKey("-n"),
                "Stake command should have -n/--to-node-id option");
        assertTrue(stakeCmd.getCommandSpec().optionsMap().containsKey("-a"),
                "Stake command should have -a/--to-account-num option");
    }

    @Test
    @DisplayName("AccountsCommand send subcommand should have required options")
    public void testSendSubcommandOptions() {
        CommandLine sendCmd = commandLine.getSubcommands().get("send");

        // Verify send has the right options
        assertTrue(sendCmd.getCommandSpec().optionsMap().containsKey("--to"),
                "Send command should have --to option");
        assertTrue(sendCmd.getCommandSpec().optionsMap().containsKey("--memo"),
                "Send command should have --memo option");
        assertTrue(sendCmd.getCommandSpec().optionsMap().containsKey("-d"),
                "Send command should have -d/--denomination option");
    }

    @Test
    @DisplayName("AccountsCommand update subcommand should have required options")
    public void testUpdateSubcommandOptions() {
        CommandLine updateCmd = commandLine.getSubcommands().get("update");

        // Verify update has the right options
        assertTrue(updateCmd.getCommandSpec().optionsMap().containsKey("--pathKeys"),
                "Update command should have --pathKeys option");
        assertTrue(updateCmd.getCommandSpec().optionsMap().containsKey("--memo"),
                "Update command should have --memo option");
        assertTrue(updateCmd.getCommandSpec().optionsMap().containsKey("--targetAccount"),
                "Update command should have --targetAccount option");
    }
}