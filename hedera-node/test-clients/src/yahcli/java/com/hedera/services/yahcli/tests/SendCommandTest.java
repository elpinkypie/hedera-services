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
import com.hedera.services.yahcli.commands.accounts.SendCommand;

/**
 * Unit tests for the SendCommand class.
 */
public class SendCommandTest extends BaseCommandTest {

    private SendCommand sendCommand;
    private AccountsCommand accountsCommand;

    @Override
    protected void initializeCommand() {
        // Create the parent command with necessary mocks
        accountsCommand = createAccountsCommand();

        // Create and link the send command
        sendCommand = new SendCommand();
        setField(sendCommand, "accountsCommand", accountsCommand);

        commandLine = new CommandLine(sendCommand);
    }

    @Test
    @DisplayName("SendCommand should parse beneficiary parameter correctly")
    public void testBeneficiaryParameter() {
        commandLine.parseArgs("--to", "1001", "100");

        assertEquals("1001", getField(sendCommand, "beneficiary"));
    }

    @Test
    @DisplayName("SendCommand should parse memo parameter correctly")
    public void testMemoParameter() {
        String testMemo = "Test transfer memo";
        commandLine.parseArgs("--memo", testMemo, "--to", "1001", "100");

        assertEquals(testMemo, getField(sendCommand, "memo"));
    }

    @Test
    @DisplayName("SendCommand should require beneficiary parameter")
    public void testRequireBeneficiaryParameter() {
        Exception exception = assertThrows(ParameterException.class, () -> {
            commandLine.parseArgs("100");
        });

        assertTrue(exception.getMessage().contains("--to"),
                "Error should indicate missing beneficiary parameter");
    }

    @Test
    @DisplayName("SendCommand should require amount parameter")
    public void testRequireAmountParameter() {
        Exception exception = assertThrows(ParameterException.class, () -> {
            commandLine.parseArgs("--to", "1001");
        });

        assertTrue(exception.getMessage().contains("<amount_to_send>"),
                "Error should indicate missing amount parameter");
    }

    @ParameterizedTest
    @DisplayName("SendCommand should parse amount and denomination correctly")
    @CsvSource({
            "100, hbar",
            "1000, tinybar",
            "10, kilobar"
    })
    public void testAmountAndDenominationParameters(String amount, String denomination) {
        commandLine.parseArgs("--to", "1001", "--denomination", denomination, amount);

        assertEquals(amount, getField(sendCommand, "amountRepr"));
        assertEquals(denomination, getField(sendCommand, "denomination"));
    }

    @Test
    @DisplayName("SendCommand should use default denomination (hbar) if not specified")
    public void testDefaultDenomination() {
        commandLine.parseArgs("--to", "1001", "100");

        assertEquals("hbar", getField(sendCommand, "denomination"));
    }

    @Test
    @DisplayName("SendCommand should handle decimals parameter for token transfers")
    public void testDecimalsParameter() {
        commandLine.parseArgs("--to", "1001", "--denomination", "0.0.12345", "--decimals", "8", "100");

        assertEquals(Integer.valueOf(8), getField(sendCommand, "decimals"));
    }

    @Test
    @DisplayName("validatedTinyBars should convert hbar to tinybars correctly")
    public void testValidatedTinybarsHbar() {
        Yahcli yahcli = createYahcliWithCommandSpec();

        long convertedAmount = SendCommand.validatedTinybars(yahcli, "100", "hbar");

        // 100 hbar = 100 * 100_000_000 tinybars
        assertEquals(10_000_000_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedTinyBars should convert tinybar to tinybars correctly")
    public void testValidatedTinybarsTinybar() {
        Yahcli yahcli = createYahcliWithCommandSpec();

        long convertedAmount = SendCommand.validatedTinybars(yahcli, "100", "tinybar");

        // 100 tinybars = 100 tinybars
        assertEquals(100L, convertedAmount);
    }

    @Test
    @DisplayName("validatedTinyBars should convert kilobar to tinybars correctly")
    public void testValidatedTinybarsKilobar() {
        Yahcli yahcli = createYahcliWithCommandSpec();

        long convertedAmount = SendCommand.validatedTinybars(yahcli, "1", "kilobar");

        // 1 kilobar = 1 * 1000 * 100_000_000 tinybars
        assertEquals(100_000_000_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedTinyBars should handle underscore-separated numbers")
    public void testValidatedTinybarsWithUnderscores() {
        Yahcli yahcli = createYahcliWithCommandSpec();

        long convertedAmount = SendCommand.validatedTinybars(yahcli, "1_000", "hbar");

        // 1_000 hbar = 1000 * 100_000_000 tinybars
        assertEquals(100_000_000_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedTinyBars should reject invalid denominations")
    public void testValidatedTinybarsInvalidDenomination() {
        Yahcli yahcli = createYahcliWithCommandSpec();

        Exception exception = assertThrows(ParameterException.class, () -> {
            SendCommand.validatedTinybars(yahcli, "100", "invalidDenomination");
        });

        assertTrue(exception.getMessage().contains("Denomination must be one of"),
                "Error should indicate invalid denomination");
    }

    @Test
    @DisplayName("validatedUnits should handle whole numbers correctly")
    public void testValidatedUnitsWholeNumbers() {
        long convertedAmount = SendCommand.validatedUnits("100", 6);

        // 100 with 6 decimals = 100_000_000
        assertEquals(100_000_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedUnits should handle decimal numbers correctly")
    public void testValidatedUnitsDecimalNumbers() {
        long convertedAmount = SendCommand.validatedUnits("100.5", 6);

        // 100.5 with 6 decimals = 100_500_000
        assertEquals(100_500_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedUnits should handle decimal numbers with fewer decimal places than specified")
    public void testValidatedUnitsDecimalNumbersWithFewerDecimals() {
        long convertedAmount = SendCommand.validatedUnits("100.5", 8);

        // 100.5 with 8 decimals = 10_050_000_000
        assertEquals(10_050_000_000L, convertedAmount);
    }

    @Test
    @DisplayName("validatedUnits should handle decimal numbers with more decimal places than specified")
    public void testValidatedUnitsDecimalNumbersWithMoreDecimals() {
        long convertedAmount = SendCommand.validatedUnits("100.12345", 3);

        // 100.123 with 3 decimals = 100_123
        assertEquals(100_123L, convertedAmount);
    }

    @Test
    @DisplayName("validatedUnits should round correctly when there are more decimals than specified")
    public void testValidatedUnitsRoundingWithMoreDecimals() {
        // The implementation would determine rounding behavior
        // This test assumes truncation, not rounding
        long convertedAmount = SendCommand.validatedUnits("100.999", 2);

        // 100.99 with 2 decimals = 10_099
        assertEquals(10_099L, convertedAmount);
    }

    @Test
    @DisplayName("SendCommand should handle token denominations")
    public void testTokenDenomination() {
        String tokenId = "0.0.12345";
        commandLine.parseArgs("--to", "1001", "--denomination", tokenId, "100");

        assertEquals(tokenId, getField(sendCommand, "denomination"));
    }

    @Test
    @DisplayName("SendCommand should handle complex combination of parameters")
    public void testComplexParameterCombination() {
        String testMemo = "Test transfer";
        String tokenId = "0.0.12345";
        String amount = "123.456";
        int decimals = 4;

        commandLine.parseArgs(
                "--to", "1001",
                "--denomination", tokenId,
                "--memo", testMemo,
                "--decimals", String.valueOf(decimals),
                amount
        );

        assertEquals("1001", getField(sendCommand, "beneficiary"));
        assertEquals(tokenId, getField(sendCommand, "denomination"));
        assertEquals(testMemo, getField(sendCommand, "memo"));
        assertEquals(Integer.valueOf(decimals), getField(sendCommand, "decimals"));
        assertEquals(amount, getField(sendCommand, "amountRepr"));
    }

    @Test
    @DisplayName("SendCommand should default decimals to 6 for token transfers")
    public void testDefaultDecimalsForTokenTransfers() {
        String tokenId = "0.0.12345";
        commandLine.parseArgs("--to", "1001", "--denomination", tokenId, "100");

        assertEquals(Integer.valueOf(6), getField(sendCommand, "decimals"));
    }

    @Test
    @DisplayName("SendCommand should validate amount format")
    public void testAmountFormatValidation() {
        Exception exception = assertThrows(Exception.class, () -> {
            // Try to execute with an invalid amount format
            // This assumes the call method does validation
            commandLine.parseArgs("--to", "1001", "invalid-amount");
            sendCommand.call();
        });

        assertTrue(exception.getMessage().contains("amount") ||
                        exception.getMessage().contains("format") ||
                        exception.getMessage().contains("invalid"),
                "Error should indicate invalid amount format");
    }

    @Test
    @DisplayName("SendCommand should validate beneficiary format")
    public void testBeneficiaryFormatValidation() {
        // Setup mock ConfigManager to control allowlist validation
        // This test requires mocking components that validate the beneficiary format
        // during the call() method execution

        Exception exception = assertThrows(Exception.class, () -> {
            // Try to execute with an invalid beneficiary format
            commandLine.parseArgs("--to", "invalid-account-format", "100");
            sendCommand.call();
        });

        assertTrue(exception.getMessage().contains("beneficiary") ||
                        exception.getMessage().contains("account") ||
                        exception.getMessage().contains("invalid"),
                "Error should indicate invalid beneficiary format");
    }
}