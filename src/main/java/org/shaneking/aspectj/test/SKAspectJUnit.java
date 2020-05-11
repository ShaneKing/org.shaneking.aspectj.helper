package org.shaneking.aspectj.test;

import org.junit.runner.RunWith;
import org.shaneking.test.SKUnit;

@AspectJConfig(classpathAdditions = "src/test/aspectj-config")
@RunWith(AspectJUnit4Runner.class)
public class SKAspectJUnit extends SKUnit {
}
