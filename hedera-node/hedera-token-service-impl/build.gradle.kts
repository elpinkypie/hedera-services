// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.module.library") }

description = "Default Hedera Token Service Implementation"

mainModuleInfo { annotationProcessor("dagger.compiler") }

testModuleInfo {
    requires("com.hedera.node.app")
    requires("com.hedera.node.app.service.token.impl")
    requires("com.hedera.node.app.spi.test.fixtures")
    requires("com.hedera.node.config.test.fixtures")
    requires("com.hedera.node.app.service.token.test.fixtures")
    requires("org.hiero.base.crypto")
    requires("org.hiero.base.utility")
    requires("com.swirlds.state.api.test.fixtures")
    requires("com.swirlds.config.extensions.test.fixtures")
    requires("net.i2p.crypto.eddsa")
    requires("org.assertj.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")
    requires("org.mockito.junit.jupiter")
    requires("com.google.protobuf")

    opensTo("com.hedera.node.app.spi.test.fixtures") // log captor injection
}
