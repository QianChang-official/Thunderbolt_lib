package com.qianchang.ae2lt_api.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an AE2 Lightning Tech addon plugin.
 *
 * <p>Apply this annotation to a class that implements {@link IAE2LTPlugin}. The
 * {@link com.qianchang.ae2lt_api.internal.PluginLoader} will discover all annotated
 * classes on the classpath and call their lifecycle methods during mod initialization.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @AE2LTPlugin
 * public class MyAddonPlugin implements IAE2LTPlugin {
 *
 *     @Override
 *     public void onInitialize(AE2LTApiContext ctx) {
 *         if (ctx.isAE2LTLoaded()) {
 *             MyAddon.LOGGER.info("AE2LT is present — enabling integration.");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>The annotated class must have a public no-argument constructor.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AE2LTPlugin {
}
