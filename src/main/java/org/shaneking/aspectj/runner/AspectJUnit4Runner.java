package org.shaneking.aspectj.runner;

import org.aspectj.weaver.loadtime.WeavingURLClassLoader;
import org.junit.*;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Use this JUnit Runner if you want to enable AspectJ load time weaving in
 * your test. To use this runner place this annotation on your test class:</p>
 * <p>{@code @RunWith(AspectJUnit4Runner.class)}</p>
 *
 * @author David Zhang
 * @see AspectJConfig
 */
public class AspectJUnit4Runner extends BlockJUnit4ClassRunner {
  private WeavingURLClassLoader cl;
  private TestClass testClass;

  public AspectJUnit4Runner(Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> loadClassFromClassLoader(Class<T> clazz, ClassLoader cl) {
    Class<T> loaded;
    try {
      loaded = (Class<T>) Class.forName(clazz.getName(), true, cl);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return loaded;
  }

  protected TestClass createTestClass(Class<?> clazz) {
    URL[] classpath = computeClasspath(clazz);
    cl = new WeavingURLClassLoader(classpath, null);
    clazz = loadClassFromClassLoader(clazz, cl);
    testClass = new TestClass(clazz);
    return testClass;
  }

  private URL[] computeClasspath(Class<?> clazz) {
    URLClassLoader originalClassLoader = (URLClassLoader) clazz.getClassLoader();
    URL[] classpath = originalClassLoader.getURLs();
    AspectJConfig config = clazz.getAnnotation(AspectJConfig.class);
    if (config != null) {
      classpath = appendToClasspath(classpath, config.classpathAdditions());
    }
    return classpath;
  }

  private URL[] appendToClasspath(URL[] classpath, String[] urls) {
    URL[] extended = Arrays.copyOf(classpath, classpath.length + urls.length);
    for (int i = 0; i < urls.length; i++) {
      URL url;
      try {
        url = Paths.get(urls[i]).toAbsolutePath().toUri().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      extended[classpath.length + i] = url;
    }
    return extended;
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    Class<? extends Annotation> test = loadClassFromClassLoader(Test.class, cl);
    return getTestClass().getAnnotatedMethods(test);
  }

  /**
   * Perform the same logic as
   * {@link BlockJUnit4ClassRunner#possiblyExpectingExceptions(FrameworkMethod, Object, Statement)}
   * except that the <em>expected exception</em> is retrieved using
   * {@link #getExpectedException(FrameworkMethod)}.
   */
  @Override
  protected Statement possiblyExpectingExceptions(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
    Class<? extends Throwable> expectedException = getExpectedException(frameworkMethod);
    if (expectedException == null) {
      Annotation annotation = frameworkMethod.getAnnotation(loadClassFromClassLoader(Test.class, cl));
      String[] members = annotation.toString().split("expected=class ");
      String exceptionClass = members[members.length - 1];
      exceptionClass = exceptionClass.endsWith(")") ? exceptionClass.substring(0, exceptionClass.length() - 1) : exceptionClass;
      try {
        if (!"org.junit.Test$None".equals(exceptionClass)) {
          expectedException = (Class<Throwable>) Class.forName(exceptionClass, true, cl);
        }
      } catch (Throwable e) {
        //ignore
      }
    }
    return (expectedException != null ? new ExpectException(next, expectedException) : next);
  }

  /**
   * Get the {@code exception} that the supplied {@linkplain FrameworkMethod
   * test method} is expected to throw.
   * <p>Supports JUnit's {@link Test#expected() @Test(expected=...)} annotation.
   * <p>Can be overridden by subclasses.
   *
   * @return the expected exception, or {@code null} if none was specified
   */
  protected Class<? extends Throwable> getExpectedException(FrameworkMethod frameworkMethod) {
    Test test = null;
    try {
      test = frameworkMethod.getAnnotation(loadClassFromClassLoader(Test.class, cl));
    } catch (Throwable e) {
      //ignore
    }
    return (test != null && test.expected() != Test.None.class ? test.expected() : null);
  }

  @Override
  public void run(final RunNotifier notifier) {
    Throwable firstException = null;
    try {
      super.run(notifier);
    } catch (Exception e) {
      firstException = e;
      throw e;
    } finally {
      try {
        cl.close();
      } catch (IOException e) {
        RuntimeException rte = new RuntimeException("Failed to close AspectJ classloader.", e);
        if (firstException != null) {
          rte.addSuppressed(firstException);
        }
        throw rte;
      }
    }
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    Class<? extends Annotation> beforeClass = loadClassFromClassLoader(BeforeClass.class, cl);
    List<FrameworkMethod> befores = testClass.getAnnotatedMethods(beforeClass);
    return befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    Class<? extends Annotation> afterClass = loadClassFromClassLoader(AfterClass.class, cl);
    List<FrameworkMethod> afters = testClass.getAnnotatedMethods(afterClass);
    return afters.isEmpty() ? statement : new RunAfters(statement, afters, null);
  }

  @Override
  protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
    Class<? extends Annotation> before = loadClassFromClassLoader(Before.class, cl);
    List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(before);
    return befores.isEmpty() ? statement : new RunBefores(statement, befores, target);
  }

  @Override
  protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
    Class<? extends Annotation> after = loadClassFromClassLoader(After.class, cl);
    List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(after);
    return afters.isEmpty() ? statement : new RunAfters(statement, afters, target);
  }

}
