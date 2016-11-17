import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.NMSMethod;

@NMS("java.lang.Object")
interface FailureNoSuchMethod {
    @NMSMethod("blablabla")
    void fail();
}