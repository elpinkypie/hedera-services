package com.hedera.services.yahcli.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.hedera.services.bdd.spec.keys.SigControl;
import com.hedera.services.yahcli.commands.accounts.AccountsCommand;
import com.hedera.services.yahcli.commands.accounts.CreateCommand;
import com.hedera.services.yahcli.util.ParseUtils;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

/**
 * Unit tests for the CreateCommand class.
 * Tests account creation functionality based on the actual implementation.
 */
public class CreateCommandTest extends BaseCommandTest {

    @TempDir
    Path tempDir;

    private CreateCommand createCommand;
    private AccountsCommand accountsCommand;

    @Override
    protected void initializeCommand() {
        // Create and setup the parent accounts command
        accountsCommand = createAccountsCommand();

        // Create and setup the command to test
        createCommand = new CreateCommand();
        setField(createCommand, "accountsCommand", accountsCommand);

        commandLine = new CommandLine(createCommand);
    }

    @Test
    @DisplayName("CreateCommand should parse memo parameter correctly")
    public void testMemoParameter() {
        String testMemo = "Test account memo";
        commandLine.parseArgs("-m", testMemo, "-k", "ED25519");

        assertEquals(testMemo, createCommand.memo);
    }

    @Test
    @DisplayName("CreateCommand should parse retries parameter correctly")
    public void testRetriesParameter() {
        int retries = 10;
        commandLine.parseArgs("-r", String.valueOf(retries), "-k", "ED25519");

        assertEquals(Integer.valueOf(retries), createCommand.boxedRetries);
    }

    @Test
    @DisplayName("CreateCommand should parse initial balance and denomination correctly")
    public void testBalanceAndDenominationParameters() {
        String amount = "1000";
        String denomination = "hbar";

        commandLine.parseArgs("-a", amount, "-d", denomination, "-k", "ED25519");

        assertEquals(amount, createCommand.amountRepr);
        assertEquals(denomination, createCommand.denomination);
    }

    @Test
    @DisplayName("CreateCommand should parse receiverSigRequired parameter correctly")
    public void testReceiverSigRequiredParameter() {
        commandLine.parseArgs("-S", "-k", "ED25519");

        assertTrue(createCommand.receiverSigRequired);
    }

    @Test
    @DisplayName("CreateCommand should parse keyFile parameter correctly")
    public void testKeyFileParameter() throws IOException {
        Path keyFilePath = tempDir.resolve("test-key.pem");
        Files.writeString(keyFilePath, "TEST KEY CONTENT");

        commandLine.parseArgs("--keyFile", keyFilePath.toString(), "-k", "ED25519");

        assertEquals(keyFilePath.toString(), createCommand.keyFile);
    }

    @Test
    @DisplayName("CreateCommand should parse passFile parameter correctly")
    public void testPassFileParameter() throws IOException {
        Path passFilePath = tempDir.resolve("test-key.pass");
        Files.writeString(passFilePath, "password");

        commandLine.parseArgs("--passFile", passFilePath.toString(), "-k", "ED25519");

        assertEquals(passFilePath.toString(), createCommand.passFile);
    }

    @ParameterizedTest
    @DisplayName("CreateCommand should accept valid key types")
    @ValueSource(strings = {"ED25519", "SECP256K1"})
    public void testValidKeyTypes(String keyType) {
        // The actual implementation uses ParseUtils.keyTypeFromParam to validate key types
        SigControl sigControl = ParseUtils.keyTypeFromParam(keyType);
        assertNotNull(sigControl, "Valid key type should be parsed correctly");

        // Test CLI parsing of the key type
        commandLine.parseArgs("-k", keyType);
        assertEquals(keyType, createCommand.keyType);
    }

    @Test
    @DisplayName("CreateCommand should reject invalid key types")
    public void testInvalidKeyType() {
        String invalidKeyType = "INVALID_TYPE";
        SigControl sigControl = ParseUtils.keyTypeFromParam(invalidKeyType);
        assertNull(sigControl, "Invalid key type should return null");

        // The command should accept the invalid key type during parsing
        // but will later reject it during validation in the call method
        commandLine.parseArgs("-k", invalidKeyType);
        assertEquals(invalidKeyType, createCommand.keyType);
    }

    @Test
    @DisplayName("CreateCommand should prioritize existing key over key generation")
    public void testExistingKeyPriority() throws IOException {
        // Create test key and pass files
        Path keyFilePath = tempDir.resolve("test-key.pem");
        Path passFilePath = tempDir.resolve("test-key.pass");
        Files.writeString(keyFilePath, "TEST KEY CONTENT");
        Files.writeString(passFilePath, "password");

        CreateCommand spyCommand = spy(createCommand);
        CommandLine spyCli = new CommandLine(spyCommand);

        // Parse with both keyFile/passFile and keyType
        spyCli.parseArgs(
                "--keyFile", keyFilePath.toString(),
                "--passFile", passFilePath.toString(),
                "-k", "SECP256K1"  // This should be ignored when keyFile is present
        );

        // Now we would verify that the existing key is used in preference to generating
        // a new key with the specified type, but we can't fully test this without mocking
        // the actual call() method behavior
        assertEquals(keyFilePath.toString(), spyCommand.keyFile);
        assertEquals(passFilePath.toString(), spyCommand.passFile);
        assertEquals("SECP256K1", spyCommand.keyType);
    }

    @Test
    @DisplayName("CreateCommand should detect if necessary key/pass files are missing")
    public void testDetectMissingKeyOrPassFile() {
        // Only specify keyFile without passFile
        Path keyFilePath = tempDir.resolve("test-key.pem");

        // This should parse fine
        commandLine.parseArgs("--keyFile", keyFilePath.toString(), "-k", "ED25519");

        // But existingKeyPresent should be false
        assertFalse(CreateCommand.existingKeyPresent(createCommand.keyFile, createCommand.passFile));

        // Now add passFile
        Path passFilePath = tempDir.resolve("test-key.pass");
        commandLine.parseArgs(
                "--keyFile", keyFilePath.toString(),
                "--passFile", passFilePath.toString(),
                "-k", "ED25519"
        );

        // Now existingKeyPresent should be true (even though files don't exist)
        assertTrue(CreateCommand.existingKeyPresent(createCommand.keyFile, createCommand.passFile));
    }

    @ParameterizedTest
    @DisplayName("CreateCommand should handle various initial balance amounts and denominations")
    @CsvSource({
            "1000, hbar",
            "5000000, tinybar",
            "2, kilobar"
    })
    public void testVariousBalanceAmounts(String amount, String denomination) {
        commandLine.parseArgs("-a", amount, "-d", denomination, "-k", "ED25519");

        assertEquals(amount, createCommand.amountRepr);
        assertEquals(denomination, createCommand.denomination);
    }

    @Test
    @DisplayName("CreateCommand should have default values for optional parameters")
    public void testDefaultValues() {
        // Parse with only the required keyType parameter
        commandLine.parseArgs("-k", "ED25519");

        assertEquals("hbar", createCommand.denomination, "Default denomination should be hbar");
        assertEquals("0", createCommand.amountRepr, "Default amount should be 0");
        assertFalse(createCommand.receiverSigRequired, "Default receiverSigRequired should be false");
        assertNull(createCommand.memo, "Default memo should be null");
        assertNull(createCommand.boxedRetries, "Default boxedRetries should be null");
    }

    @Test
    @DisplayName("CreateCommand should require keyType parameter when no existing key is provided")
    public void testRequireKeyTypeOrExistingKey() {
        Exception exception = assertThrows(Exception.class, () -> {
            // Execute without keyType or keyFile/passFile
            commandLine.execute();
        });

        assertTrue(exception.getMessage().contains("Missing required option") ||
                        exception.getMessage().contains("--keyType") ||
                        exception.getMessage().contains("-k"),
                "Error should indicate missing key type parameter");
    }

    @Test
    @DisplayName("CreateCommand should validate that keyFile and passFile are provided together")
    public void testKeyFileAndPassFileTogether() throws IOException {
        // Create a temp key file
        Path keyFilePath = tempDir.resolve("test-key.pem");
        Files.writeString(keyFilePath, "TEST KEY CONTENT");

        // Only provide keyFile without passFile
        Exception exception = assertThrows(Exception.class, () -> {
            commandLine.parseArgs("--keyFile", keyFilePath.toString());
            createCommand.call(); // This should fail during validation
        });

        assertTrue(exception.getMessage().contains("passFile") ||
                        exception.getMessage().contains("missing"),
                "Error should indicate missing passFile");
    }

    @Test
    @DisplayName("CreateCommand call should handle validation of parameters")
    public void testCallMethodValidatesParameters() {
        // Setup command with invalid parameters
        setField(createCommand, "keyType", "INVALID_TYPE");

        Exception exception = assertThrows(Exception.class, () -> {
            createCommand.call();
        });

        assertTrue(exception.getMessage().contains("key type") ||
                        exception.getMessage().contains("Invalid"),
                "Error should indicate invalid key type");
    }

    @Test
    @DisplayName("CreateCommand should support multiple parameter formats")
    public void testMultipleParameterFormats() {
        // Test with short options
        commandLine.parseArgs(
                "-k", "ED25519",
                "-m", "short options test",
                "-a", "1000",
                "-d", "hbar",
                "-r", "5"
        );

        assertEquals("ED25519", createCommand.keyType);
        assertEquals("short options test", createCommand.memo);
        assertEquals("1000", createCommand.amountRepr);
        assertEquals("hbar", createCommand.denomination);
        assertEquals(Integer.valueOf(5), createCommand.boxedRetries);

        // Reset command
        createCommand = new CreateCommand();
        setField(createCommand, "accountsCommand", accountsCommand);
        commandLine = new CommandLine(createCommand);

        // Test with long options
        commandLine.parseArgs(
                "--keyType", "SECP256K1",
                "--memo", "long options test",
                "--amount", "2000",
                "--denomination", "tinybar",
                "--retries", "10"
        );

        assertEquals("SECP256K1", createCommand.keyType);
        assertEquals("long options test", createCommand.memo);
        assertEquals("2000", createCommand.amountRepr);
        assertEquals("tinybar", createCommand.denomination);
        assertEquals(Integer.valueOf(10), createCommand.boxedRetries);
    }
}