// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.test.exec.systemcontracts.hts.tokeninfo;

import static com.hedera.hapi.node.base.ResponseCodeEnum.INVALID_TOKEN_ID;
import static com.hedera.hapi.node.base.ResponseCodeEnum.SUCCESS;
import static com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.ReturnTypes.ZERO_ACCOUNT_ID;
import static com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.tokeninfo.address_0x167.TokenInfoTranslator.TOKEN_INFO_167;
import static com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.tokeninfo.address_0x16c.TokenInfoTranslator.TOKEN_INFO_16C;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTED_FIXED_CUSTOM_FEES;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTED_FRACTIONAL_CUSTOM_FEES;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTED_KEYLIST_V2;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTED_ROYALTY_CUSTOM_FEES;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTE_DEFAULT_KEYLIST;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.EXPECTE_KEYLIST;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.FUNGIBLE_EVERYTHING_TOKEN;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.FUNGIBLE_EVERYTHING_TOKEN_16C;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.LEDGER_ID;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.SENDER_ID;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.revertOutputFor;
import static com.hedera.node.app.service.contract.impl.utils.ConversionUtils.headlongAddressOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.esaulpaugh.headlong.abi.Tuple;
import com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.tokeninfo.TokenInfoCall;
import com.hedera.node.app.service.contract.impl.test.exec.systemcontracts.common.CallTestBase;
import com.hedera.node.config.data.LedgerConfig;
import com.swirlds.config.api.Configuration;
import java.util.Collections;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenInfoCallTest extends CallTestBase {
    @Mock
    private Configuration config;

    @Mock
    private LedgerConfig ledgerConfig;

    @Test
    void returnsTokenInfoStatusForPresentToken() {
        when(config.getConfigData(LedgerConfig.class)).thenReturn(ledgerConfig);
        final var expectedLedgerId = com.hedera.pbj.runtime.io.buffer.Bytes.fromHex(LEDGER_ID);
        when(ledgerConfig.id()).thenReturn(expectedLedgerId);

        final var subject = new TokenInfoCall(
                gasCalculator, mockEnhancement(), false, FUNGIBLE_EVERYTHING_TOKEN, config, TOKEN_INFO_167.function());

        final var result = subject.execute().fullResult().result();

        assertEquals(MessageFrame.State.COMPLETED_SUCCESS, result.getState());
        assertEquals(
                Bytes.wrap(TOKEN_INFO_167
                        .getOutputs()
                        .encode(Tuple.of(
                                SUCCESS.protoOrdinal(),
                                Tuple.from(
                                        Tuple.from(
                                                "Fungible Everything Token",
                                                "FET",
                                                headlongAddressOf(SENDER_ID),
                                                "The memo",
                                                true,
                                                88888888L,
                                                true,
                                                EXPECTE_KEYLIST.toArray(new Tuple[0]),
                                                Tuple.of(100L, headlongAddressOf(SENDER_ID), 200L)),
                                        7777777L,
                                        false,
                                        true,
                                        true,
                                        EXPECTED_FIXED_CUSTOM_FEES.toArray(new Tuple[0]),
                                        EXPECTED_FRACTIONAL_CUSTOM_FEES.toArray(new Tuple[0]),
                                        EXPECTED_ROYALTY_CUSTOM_FEES.toArray(new Tuple[0]),
                                        Bytes.wrap(expectedLedgerId.toByteArray())
                                                .toString())))
                        .array()),
                result.getOutput());
    }

    @Test
    void returnsTokenInfoStatusForPresentTokenV2() {
        when(config.getConfigData(LedgerConfig.class)).thenReturn(ledgerConfig);
        final var expectedLedgerId = com.hedera.pbj.runtime.io.buffer.Bytes.fromHex(LEDGER_ID);
        when(ledgerConfig.id()).thenReturn(expectedLedgerId);

        final var subject = new TokenInfoCall(
                gasCalculator,
                mockEnhancement(),
                false,
                FUNGIBLE_EVERYTHING_TOKEN_16C,
                config,
                TOKEN_INFO_16C.function());

        final var result = subject.execute().fullResult().result();

        assertEquals(MessageFrame.State.COMPLETED_SUCCESS, result.getState());
        assertEquals(
                Bytes.wrap(TOKEN_INFO_16C
                        .getOutputs()
                        .encode(Tuple.of(
                                SUCCESS.protoOrdinal(),
                                Tuple.from(
                                        Tuple.from(
                                                "Fungible Everything Token",
                                                "FET",
                                                headlongAddressOf(SENDER_ID),
                                                "The memo",
                                                true,
                                                88888888L,
                                                true,
                                                EXPECTED_KEYLIST_V2.toArray(new Tuple[0]),
                                                Tuple.of(100L, headlongAddressOf(SENDER_ID), 200L),
                                                com.hedera.pbj.runtime.io.buffer.Bytes.wrap("SOLD")
                                                        .toByteArray()),
                                        7777777L,
                                        false,
                                        true,
                                        true,
                                        EXPECTED_FIXED_CUSTOM_FEES.toArray(new Tuple[0]),
                                        EXPECTED_FRACTIONAL_CUSTOM_FEES.toArray(new Tuple[0]),
                                        EXPECTED_ROYALTY_CUSTOM_FEES.toArray(new Tuple[0]),
                                        Bytes.wrap(expectedLedgerId.toByteArray())
                                                .toString())))
                        .array()),
                result.getOutput());
    }

    @Test
    void returnsTokenInfoStatusForMissingToken() {
        when(config.getConfigData(LedgerConfig.class)).thenReturn(ledgerConfig);
        final var expectedLedgerId = com.hedera.pbj.runtime.io.buffer.Bytes.fromHex("01");
        when(ledgerConfig.id()).thenReturn(expectedLedgerId);

        final var subject =
                new TokenInfoCall(gasCalculator, mockEnhancement(), false, null, config, TOKEN_INFO_167.function());

        final var result = subject.execute().fullResult().result();

        assertEquals(MessageFrame.State.COMPLETED_SUCCESS, result.getState());
        assertEquals(
                Bytes.wrap(TOKEN_INFO_167
                        .getOutputs()
                        .encode(Tuple.of(
                                INVALID_TOKEN_ID.protoOrdinal(),
                                Tuple.from(
                                        Tuple.from(
                                                "",
                                                "",
                                                headlongAddressOf(ZERO_ACCOUNT_ID),
                                                "",
                                                false,
                                                0L,
                                                false,
                                                EXPECTE_DEFAULT_KEYLIST.toArray(new Tuple[0]),
                                                Tuple.of(0L, headlongAddressOf(ZERO_ACCOUNT_ID), 0L)),
                                        0L,
                                        false,
                                        false,
                                        false,
                                        Collections.emptyList().toArray(new Tuple[0]),
                                        Collections.emptyList().toArray(new Tuple[0]),
                                        Collections.emptyList().toArray(new Tuple[0]),
                                        Bytes.wrap(expectedLedgerId.toByteArray())
                                                .toString())))
                        .array()),
                result.getOutput());
    }

    @Test
    void returnsTokenInfoStatusForMissingTokenStaticCall() {
        when(config.getConfigData(LedgerConfig.class)).thenReturn(ledgerConfig);
        when(ledgerConfig.id()).thenReturn(com.hedera.pbj.runtime.io.buffer.Bytes.fromHex("01"));

        final var subject =
                new TokenInfoCall(gasCalculator, mockEnhancement(), true, null, config, TOKEN_INFO_167.function());

        final var result = subject.execute().fullResult().result();

        assertEquals(MessageFrame.State.REVERT, result.getState());
        assertEquals(revertOutputFor(INVALID_TOKEN_ID), result.getOutput());
    }

    @Test
    void returnsTokenInfoStatusForMissingTokenStaticCallV2() {
        when(config.getConfigData(LedgerConfig.class)).thenReturn(ledgerConfig);
        when(ledgerConfig.id()).thenReturn(com.hedera.pbj.runtime.io.buffer.Bytes.fromHex("01"));

        final var subject =
                new TokenInfoCall(gasCalculator, mockEnhancement(), true, null, config, TOKEN_INFO_16C.function());

        final var result = subject.execute().fullResult().result();

        assertEquals(MessageFrame.State.REVERT, result.getState());
        assertEquals(revertOutputFor(INVALID_TOKEN_ID), result.getOutput());
    }
}
