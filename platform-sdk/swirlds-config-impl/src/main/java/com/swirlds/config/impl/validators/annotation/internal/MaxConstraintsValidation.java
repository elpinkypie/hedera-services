// SPDX-License-Identifier: Apache-2.0
package com.swirlds.config.impl.validators.annotation.internal;

import com.swirlds.config.api.Configuration;
import com.swirlds.config.api.validation.ConfigValidator;
import com.swirlds.config.api.validation.ConfigViolation;
import com.swirlds.config.api.validation.annotation.Max;
import com.swirlds.config.extensions.reflection.ConfigReflectionUtils;
import com.swirlds.config.impl.internal.ConfigNumberUtils;
import com.swirlds.config.impl.validators.DefaultConfigViolation;
import java.util.stream.Stream;

/**
 * A {@link ConfigValidator} implementation which checks all config data objects for the constraints annotation
 * {@link Max}.
 */
public class MaxConstraintsValidation implements ConfigValidator {

    @Override
    public Stream<ConfigViolation> validate(final Configuration configuration) {
        return ConfigReflectionUtils.getAllMatchingPropertiesForConstraintAnnotation(Max.class, configuration).stream()
                .filter(property -> ConfigNumberUtils.isNumber(property.propertyType()))
                .filter(property ->
                        property.annotation().value() < ConfigNumberUtils.getLongValue(property.propertyValue()))
                .map(property -> new DefaultConfigViolation(
                        property.propertyName(),
                        property.propertyValue() + "",
                        true,
                        "Value must be <= " + property.annotation().value()));
    }
}
