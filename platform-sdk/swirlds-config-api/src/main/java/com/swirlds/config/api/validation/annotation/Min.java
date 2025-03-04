// SPDX-License-Identifier: Apache-2.0
package com.swirlds.config.api.validation.annotation;

import com.swirlds.config.api.ConfigurationBuilder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A constraint annotation that can be used define a min value for a config data property (see
 * {@link com.swirlds.config.api.ConfigProperty}). The validation of the annotation is automatically executed at the
 * initialization of the configuration (see {@link ConfigurationBuilder#build()}). The annotated property should not be
 * used for floating point values.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Min {

    /**
     * Defines the minimum value that is allowed for the annotated number.
     *
     * @return the minimum value that is allowed
     */
    long value();
}
