import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.NMSMethod;

@NMS("java.lang.Object")
interface FailuredifferentParameters {
    @NMSMethod("equals")
    boolean fail();
}