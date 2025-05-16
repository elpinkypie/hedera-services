package com.hedera.services.yahcli.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import com.hedera.services.yahcli.Yahcli;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import picocli.CommandLine;

import com.hedera.services.yahcli.commands.accounts.AccountsCommand;

/**
 * Base test class for Yahcli command tests.
 * Provides common setup and teardown for capturing console output.
 * Includes reflection helpers for accessing private fields.
 */
public abstract class BaseCommandTest {

    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;
    protected final PrintStream originalErr = System.err;

    protected CommandLine commandLine;

    /**
     * Sets up the test environment.
     * Redirects System.out and System.err to capture output.
     * Then initializes the command under test.
     */
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        initializeCommand();
    }

    /**
     * Abstract method to be implemented by subclasses to initialize
     * the specific command being tested.
     */
    protected abstract void initializeCommand();

    /**
     * Restores System.out and System.err to their original state.
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Helper method to verify that the output contains a specific string.
     */
    protected void assertOutputContains(String expected) {
        assertTrue(outContent.toString().contains(expected),
                "Output should contain: " + expected);
    }

    /**
     * Helper method to verify that the error output contains a specific string.
     */
    protected void assertErrorContains(String expected) {
        assertTrue(errContent.toString().contains(expected),
                "Error output should contain: " + expected);
    }

    /**
     * Helper method to clear the output and error streams.
     */
    protected void clearOutputs() {
        outContent.reset();
        errContent.reset();
    }

    /**
     * Gets the value of a field from an object using reflection.
     * Works with private and package-private fields.
     *
     * @param obj The object to get the field value from
     * @param fieldName The name of the field
     * @param <T> The expected type of the field
     * @return The value of the field
     * @throws RuntimeException if the field doesn't exist or can't be accessed
     */
    protected static <T> T getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            T value = (T) field.get(obj);
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field: " + fieldName, e);
        }
    }

    /**
     * Sets the value of a field on an object using reflection.
     * Works with private and package-private fields.
     *
     * @param obj The object to set the field value on
     * @param fieldName The name of the field
     * @param value The value to set
     * @throws RuntimeException if the field doesn't exist or can't be accessed
     */
    protected static void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    /**
     * Creates a Yahcli instance with CommandSpec initialized.
     * This is a common setup pattern in many tests.
     *
     * @return a Yahcli instance with its spec field initialized
     */
    protected Yahcli createYahcliWithCommandSpec() {
        Yahcli yahcli = new Yahcli();
        setField(yahcli, "spec", new CommandLine(yahcli).getCommandSpec());
        return yahcli;
    }

    /**
     * Sets up a mock Yahcli with spec and command line for error validation tests.
     *
     * @return a mocked Yahcli instance
     */
    protected Yahcli createMockYahcliForErrors() {
        Yahcli mockYahcli = Mockito.mock(Yahcli.class);
        CommandLine mockCommandLine = Mockito.mock(CommandLine.class);

        // Set up the CommandSpec directly using reflection
        setField(mockYahcli, "spec", Mockito.mock(CommandLine.class).getCommandSpec());

        return mockYahcli;
    }

    /**
     * Creates and initializes an AccountsCommand with a Yahcli parent.
     *
     * @return the initialized AccountsCommand
     */
    protected AccountsCommand createAccountsCommand() {
        AccountsCommand accountsCommand = new AccountsCommand();
        Yahcli yahcli = createYahcliWithCommandSpec();
        setField(accountsCommand, "yahcli", yahcli);
        return accountsCommand;
    }

    /**
     * Helper method to setup a subcommand of AccountsCommand
     *
     * @param <T> The type of the subcommand
     * @param subcommand The subcommand instance to set up
     * @param accountsCommand The parent AccountsCommand
     * @return The initialized subcommand
     */
    protected <T> T setupAccountsSubcommand(T subcommand, AccountsCommand accountsCommand) {
        setField(subcommand, "accountsCommand", accountsCommand);
        return subcommand;
    }
}