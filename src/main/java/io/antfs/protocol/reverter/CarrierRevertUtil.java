package io.antfs.protocol.reverter;

import io.antfs.protocol.Packet;
import io.antfs.protocol.carriers.Carrier;

/**
 * @author gris.wang
 * @since 2018/3/30
 **/
public class CarrierRevertUtil {

    private static final CarrierReverter REVERTER = DefaultCarrierReverter.getInstance();

    public static Carrier revert(Packet packet) {
        return REVERTER.revert(packet);
    }

}
