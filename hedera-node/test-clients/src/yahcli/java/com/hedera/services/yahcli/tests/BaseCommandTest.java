package com.hedera.services.yahcli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

/**
 * Base test class for Yahcli command tests.
 * Provides common setup and teardown for capturing console output.
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
     * Initializes the command and command line.
     */
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        initializeCommand();
    }

    /**
     * Restores System.out and System.err to their original state.
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Initialize the command under test and the picocli CommandLine object.
     * This method must be implemented by subclasses.
     */
    protected abstract void initializeCommand();

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
}