/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Class used for testing that will not communicate with an actual vehicle.
 * Rather all {@link Packet}s sent will be echoed back in the queue
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class EchoBus implements Bus {
    private final int address;

    private final MultiQueue<Packet> queue;

    /**
     * Constructor
     *
     * @param address
     *            the address for this connector on the bus
     */
    public EchoBus(int address) {
        this(address, new MultiQueue<>());
    }

    /**
     * Constructor exposed for testing
     *
     * @param address
     *            the address for this connector on the bus
     * @param queue
     *            the {@link MultiQueue} to use to back the bus
     */
    public EchoBus(int address, MultiQueue<Packet> queue) {
        this.address = address;
        this.queue = queue;
    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public int getConnectionSpeed() throws BusException {
        throw new BusException("Could not be determined");
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) {
        return queue.stream(timeout, unit);
    }

    @Override
    public void send(Packet p) {
        queue.add(p);
    }
}
