package org.syncloud.platform.ssh;

import com.google.common.base.Optional;

import org.syncloud.platform.ssh.model.ConnectionPoint;
import org.syncloud.platform.ssh.model.Device;

public class EndpointSelector {

    private EndpointPreference preference;

    public EndpointSelector(EndpointPreference preference) {
        this.preference = preference;
    }

    public Optional<ConnectionPoint> select(Device device, boolean first) {
        boolean valid = first ? preference.isRemote() : !preference.isRemote();
        if (valid){
            return Optional.of(new ConnectionPoint(device.remoteEndpoint(), device.credentials()));
        } else
            return Optional.of(new ConnectionPoint(device.localEndpoint(), device.credentials()));

    }
}