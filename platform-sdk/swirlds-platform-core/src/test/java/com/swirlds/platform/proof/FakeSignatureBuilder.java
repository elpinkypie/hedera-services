// SPDX-License-Identifier: Apache-2.0
package com.swirlds.platform.proof;

import static org.hiero.base.crypto.SignatureType.RSA;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.hiero.base.crypto.Signature;

/**
 * Builds fake signatures and validates fake signatures. Fake signatures do not need private keys.
 */
public class FakeSignatureBuilder implements SignatureVerifier {

    private final Random random;

    /**
     * Represents data that has been signed with a fake signatures.
     *
     * @param data      the data that was "signed"
     * @param publicKey the public key corresponding to the imaginary private key that "signed" the data
     */
    private record FakeSignedData(@NonNull byte[] data, @NonNull PublicKey publicKey) {}

    /**
     * All fake signatures created by this object.
     */
    private final Map<Signature, FakeSignedData> fakeSignatures = new HashMap<>();

    /**
     * Create a new fake signature builder.
     *
     * @param random the random number generator to use when creating fake signatures
     */
    public FakeSignatureBuilder(@NonNull final Random random) {
        this.random = random;
    }

    /**
     * Create a fake signature. After this method has been called,
     * {@link #verifySignature(Signature, byte[], PublicKey)} will describe this signature as valid.
     *
     * @param data      the data to "sign"
     * @param publicKey the public key corresponding to the imaginary private key that will be used to "sign" the data
     * @return the fake signature
     */
    @NonNull
    public Signature fakeSign(@NonNull final byte[] data, @NonNull final PublicKey publicKey) {
        final byte[] signatureBytes = new byte[RSA.signatureLength()];
        random.nextBytes(signatureBytes);
        final var signature = new Signature(RSA, signatureBytes);
        fakeSignatures.put(signature, new FakeSignedData(data, publicKey));
        return signature;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will return true if the signature was generated by this class on the given data with the given public key.
     */
    @Override
    public boolean verifySignature(
            @NonNull final Signature signature, @NonNull final byte[] bytes, @NonNull final PublicKey publicKey) {

        final FakeSignedData data = fakeSignatures.get(signature);
        if (data == null) {
            return false;
        }

        return Arrays.equals(data.data, bytes) && data.publicKey.equals(publicKey);
    }
}
