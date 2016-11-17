import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.NMSMethod;

@NMS("java.lang.Object")
interface FailureDifferentReturn {
    @NMSMethod("toString")
    void fail();

    @NMSMethod("equals")
    boolean fail2();
}