package io.antfs.protocol.reverter;

import io.antfs.protocol.Packet;
import io.antfs.protocol.carriers.Carrier;

/**
 * @author gris.wang
 * @since 2018/3/28
 **/
public interface CarrierReverter {

    /**
     * revert the Packet to Carrier
     * @param packet the Packet
     * @return the Carrier
     */
    Carrier revert(Packet packet);

}
