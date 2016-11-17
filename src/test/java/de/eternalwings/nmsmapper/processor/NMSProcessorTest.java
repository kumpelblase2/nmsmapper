package de.eternalwings.nmsmapper.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.tools.JavaFileObject;

import java.io.IOException;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@RunWith(BlockJUnit4ClassRunner.class)
public class NMSProcessorTest {

    public static final String MAPPED_METHOD_DOES_NOT_EXIST = "Mapped method blablabla does not exist.";
    public static final String MAPPED_METHOD_DOES_NOT_EXIST2 = "Mapped method toString does not exist.";
    public static final String MAPPED_METHOD_DOES_NOT_EXIST3 = "Mapped method equals does not exist.";
    private Compiler compiler;

    @Before
    public void setup() {
        this.compiler = javac().withProcessors(new NMSProcessor());
    }

    @Test
    public void testFailsUnknownMethod() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureNoSuchMethod.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST).inFile(fileObject).onLine(6);
    }

    @Test
    public void testFailsUnknownMethodClass() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureNoSuchMethodClass.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST).inFile(fileObject).onLine(6);
    }

    @Test
    public void testSignatureMismatch() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentReturn.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST2).inFile(fileObject).onLine(6);
    }

    @Test
    public void testSignatureMismatchClass() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentReturnClass.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST2).inFile(fileObject).onLine(6);
    }

    @Test
    public void testParametersMismatch() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentParameters.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST3).inFile(fileObject).onLine(6);
    }

    @Test
    public void testParameterMismatchClass() {
        JavaFileObject fileObject = JavaFileObjects.forResource("failures/FailureDifferentParametersClass.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(MAPPED_METHOD_DOES_NOT_EXIST3).inFile(fileObject).onLine(6);
    }

    @Test
    public void testSuperMethodCallGen() throws IOException {
        JavaFileObject fileObject = JavaFileObjects.forResource("successes/SuccessMethodSuper.java");
        Compilation compilation = compiler.compile(fileObject);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("SuccessMethodSuper$NMS").contentsAsUtf8String().isEqualTo(JavaFileObjects.forResource("success_output/SuccessMethodSuper.java").getCharContent(false));
    }
}
