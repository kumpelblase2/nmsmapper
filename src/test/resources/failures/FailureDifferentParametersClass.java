import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.NMSMethod;

@NMS
class FailureDifferentReturn extends Object {
    @NMSMethod("equals")
    boolean fail() {}
}