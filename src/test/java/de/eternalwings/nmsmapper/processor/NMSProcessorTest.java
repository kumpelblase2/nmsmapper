package de.eternalwings.nmsmapper.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

@RunWith(BlockJUnit4ClassRunner.class)
public class NMSProcessorTest {
    @Test
    public void testFailsUnknownMethod() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureNoSuchMethod.java");
        String errorMessage = "Mapped method blablabla does not exist.";
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);

        fileObject = JavaFileObjects.forResource("failures/FailureNoSuchMethodClass.java");
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);
    }

    @Test
    public void testSignatureMismatch() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentReturn.java");
        String errorMessage = "Mapped method toString does not exist.";
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);

        fileObject = JavaFileObjects.forResource("failures/FailureDifferentReturnClass.java");
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);
    }

    @Test
    public void testParametersMismatch() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentParameters.java");
        String errorMessage = "Mapped method equals does not exist.";
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);

        fileObject = JavaFileObjects.forResource("failures/FailureDifferentParametersClass.java");
        ASSERT.about(javaSource()).that(fileObject).processedWith(new NMSProcessor())
                .failsToCompile().withErrorContaining(errorMessage).in(fileObject).onLine(6);
    }
}
