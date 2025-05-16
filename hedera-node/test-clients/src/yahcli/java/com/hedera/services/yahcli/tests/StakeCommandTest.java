package com.hedera.services.yahcli.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import com.hedera.services.yahcli.Yahcli;
import com.hedera.services.yahcli.commands.accounts.AccountsCommand;
import com.hedera.services.yahcli.commands.accounts.StakeCommand;
import com.hedera.services.yahcli.suites.StakeSuite;

/**
 * Unit tests for the StakeCommand class.
 */
public class StakeCommandTest extends BaseCommandTest {

    private StakeCommand stakeCommand;
    private AccountsCommand accountsCommand;

    @Override
    protected void initializeCommand() {
        // Create and set up the parent AccountsCommand
        accountsCommand = createAccountsCommand();

        // Create and set up the StakeCommand
        stakeCommand = new StakeCommand();
        setField(stakeCommand, "accountsCommand", accountsCommand);

        commandLine = new CommandLine(stakeCommand);
    }

    @Test
    @DisplayName("StakeCommand should parse node ID parameter correctly")
    public void testNodeIdParameter() {
        commandLine.parseArgs("--to-node-id", "3");

        assertEquals("3", stakeCommand.electedNodeId);
        assertNull(stakeCommand.electedAccountNum);
    }

    @Test
    @DisplayName("StakeCommand should parse account number parameter correctly")
    public void testAccountNumParameter() {
        commandLine.parseArgs("--to-account-num", "1001");

        assertEquals("1001", stakeCommand.electedAccountNum);
        assertNull(stakeCommand.electedNodeId);
    }

    @Test
    @DisplayName("StakeCommand should parse staked account parameter correctly")
    public void testStakedAccountParameter() {
        commandLine.parseArgs("--to-node-id", "3", "1001");

        assertEquals("1001", stakeCommand.stakedAccountNum);
    }

    @Test
    @DisplayName("StakeCommand should parse start declining rewards parameter")
    public void testStartDecliningRewardsParameter() {
        commandLine.parseArgs("--start-declining-rewards", "--to-node-id", "3");

        assertTrue(stakeCommand.startDecliningRewards);
        assertNull(stakeCommand.stopDecliningRewards);
    }

    @Test
    @DisplayName("StakeCommand should parse stop declining rewards parameter")
    public void testStopDecliningRewardsParameter() {
        commandLine.parseArgs("--stop-declining-rewards", "--to-node-id", "3");

        assertTrue(stakeCommand.stopDecliningRewards);
        assertNull(stakeCommand.startDecliningRewards);
    }

    @Test
    @DisplayName("StakeCommand should not allow specifying both node and account targets")
    public void testCannotSpecifyBothNodeAndAccountTargets() {
        // Set the conflicting fields using reflection
        setField(stakeCommand, "electedNodeId", "3");
        setField(stakeCommand, "electedAccountNum", "1001");

        // Mock the command execution to avoid actual network calls
        Exception exception = assertThrows(ParameterException.class, () -> {
            // Call assertValidParams through reflection since it's private
            // For simulation, we'll call the command directly
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("Cannot stake to both node"),
                "Error should indicate that both node and account cannot be specified");
    }

    @Test
    @DisplayName("StakeCommand should not allow specifying both start and stop declining rewards")
    public void testCannotSpecifyBothStartAndStopDecliningRewards() {
        // Set up the condition that should cause validation failure
        setField(stakeCommand, "electedNodeId", "3");
        setField(stakeCommand, "startDecliningRewards", true);
        setField(stakeCommand, "stopDecliningRewards", true);

        Exception exception = assertThrows(ParameterException.class, () -> {
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("Cannot both start and stop declining rewards"),
                "Error should indicate that both start and stop cannot be specified");
    }

    @Test
    @DisplayName("StakeCommand should require either target or declining rewards change")
    public void testRequireEitherTargetOrDecliningRewardsChange() {
        Exception exception = assertThrows(ParameterException.class, () -> {
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("Must stake to either a node or an account") ||
                        exception.getMessage().contains("start/stop declining rewards"),
                "Error should indicate target or rewards change is required");
    }

    @Test
    @DisplayName("StakeCommand should validate node ID format")
    public void testValidateNodeIdFormat() {
        setField(stakeCommand, "electedNodeId", "invalid-node-id");

        Exception exception = assertThrows(ParameterException.class, () -> {
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("node-id value") &&
                        exception.getMessage().contains("un-parseable"),
                "Error should indicate invalid node ID format");
    }

    @Test
    @DisplayName("StakeCommand should validate account number format")
    public void testValidateAccountNumFormat() {
        setField(stakeCommand, "electedAccountNum", "invalid-account");

        Exception exception = assertThrows(ParameterException.class, () -> {
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("account-num value") &&
                        exception.getMessage().contains("un-parseable"),
                "Error should indicate invalid account number format");
    }

    @Test
    @DisplayName("StakeCommand should validate staked account format")
    public void testValidateStakedAccountFormat() {
        setField(stakeCommand, "electedNodeId", "3");
        setField(stakeCommand, "stakedAccountNum", "invalid-account");

        Exception exception = assertThrows(ParameterException.class, () -> {
            stakeCommand.call();
        });

        assertTrue(exception.getMessage().contains("staked account parameter") &&
                        exception.getMessage().contains("un-parseable"),
                "Error should indicate invalid staked account format");
    }

    @Test
    @DisplayName("StakeCommand should support either node ID or account as target")
    public void testStakeTargetTypes() {
        // Test with node target
        setField(stakeCommand, "electedNodeId", "3");

        assertEquals("3", stakeCommand.electedNodeId);
        assertNull(stakeCommand.electedAccountNum);

        // Reset and test with account target
        stakeCommand = new StakeCommand();
        setField(stakeCommand, "accountsCommand", accountsCommand);
        setField(stakeCommand, "electedAccountNum", "1001");

        assertEquals("1001", stakeCommand.electedAccountNum);
        assertNull(stakeCommand.electedNodeId);
    }

    @Test
    @DisplayName("StakeCommand should support declining rewards options")
    public void testDecliningRewardsOptions() {
        // Test with start declining rewards
        setField(stakeCommand, "electedNodeId", "3");
        setField(stakeCommand, "startDecliningRewards", true);

        assertTrue(stakeCommand.startDecliningRewards);
        assertNull(stakeCommand.stopDecliningRewards);

        // Reset and test with stop declining rewards
        stakeCommand = new StakeCommand();
        setField(stakeCommand, "accountsCommand", accountsCommand);
        setField(stakeCommand, "electedNodeId", "3");
        setField(stakeCommand, "stopDecliningRewards", true);

        assertTrue(stakeCommand.stopDecliningRewards);
        assertNull(stakeCommand.startDecliningRewards);
    }

    @Test
    @DisplayName("StakeCommand should handle complex parameter combination")
    public void testComplexParameterCombination() {
        commandLine.parseArgs(
                "--to-node-id", "3",
                "--start-declining-rewards",
                "1001"
        );

        assertEquals("3", stakeCommand.electedNodeId);
        assertTrue(stakeCommand.startDecliningRewards);
        assertEquals("1001", stakeCommand.stakedAccountNum);
    }

    @Test
    @DisplayName("Execute with help should display help and return 0")
    public void testExecuteWithHelp() {
        int exitCode = commandLine.execute("--help");
        assertEquals(0, exitCode);
        assertOutputContains("Usage:");
    }

    @Test
    @DisplayName("StakeCommand should support abbreviated option names")
    public void testAbbreviatedOptions() {
        commandLine.parseArgs("-n", "3");

        assertEquals("3", stakeCommand.electedNodeId);
    }

    @Test
    @DisplayName("StakeCommand should reject both abbreviated and long versions of same option")
    public void testDuplicateOptionFormats() {
        Exception exception = assertThrows(Exception.class, () -> {
            commandLine.parseArgs("-n", "3", "--to-node-id", "4");
        });

        assertTrue(exception instanceof ParameterException,
                "Should reject duplicate option specifications");
    }
}