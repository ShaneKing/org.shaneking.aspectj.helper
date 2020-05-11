package sktest.aspectj.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import sktest.aspectj.test.example.HelloTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({HelloTest.class, DummyTest.class, JUnitLifeCycleTest.class})
public class AspectJTestSuites {
}
