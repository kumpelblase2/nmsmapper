import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.NMSMethod;

@NMS
class SuccessMethodSuper extends Object {
    @NMSMethod("toString")
    protected String works() {
        return "";
    }

    protected String _works() {
        return "";
    }
}