package org.shaneking.aspectj.helper;

import java.lang.annotation.*;

/**
 * Use this annotation on your test class to configure AspectJ.
 * <p>
 * For example you can control which aop.xml files are found on the classpath.
 * <p>
 * If you do not use this annotation please make sure you have a
 * {@code META-INF/aop.xml} on the classpath.
 *
 * @author David Zhang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AspectJConfig {

  /**
   * Use this to append additional classpath entries.
   * This is especially useful if you want your tests to use different
   * aop.xml files each.
   * If your aop.xml file is located for example in
   * {@code /home/joe/test/META-INF/aop.xml} then add {@code /home/joe/test}
   * as classpathAddition.
   * Depending on your project setup this might also work with relative
   * parts.
   */
  String[] classpathAdditions() default "";
}
