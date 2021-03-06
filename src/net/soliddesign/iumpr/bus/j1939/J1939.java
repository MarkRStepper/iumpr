/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.soliddesign.iumpr.IUMPR;
import net.soliddesign.iumpr.bus.Bus;
import net.soliddesign.iumpr.bus.BusException;
import net.soliddesign.iumpr.bus.Packet;
import net.soliddesign.iumpr.bus.j1939.packets.AddressClaimPacket;
import net.soliddesign.iumpr.bus.j1939.packets.ComponentIdentificationPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM19CalibrationInformationPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM24SPNSupportPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM30ScaledTestResultsPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import net.soliddesign.iumpr.bus.j1939.packets.DM7CommandTestsPacket;
import net.soliddesign.iumpr.bus.j1939.packets.EngineHoursPacket;
import net.soliddesign.iumpr.bus.j1939.packets.EngineSpeedPacket;
import net.soliddesign.iumpr.bus.j1939.packets.HighResVehicleDistancePacket;
import net.soliddesign.iumpr.bus.j1939.packets.ParsedPacket;
import net.soliddesign.iumpr.bus.j1939.packets.TotalVehicleDistancePacket;
import net.soliddesign.iumpr.bus.j1939.packets.VehicleIdentificationPacket;

/**
 * A Wrapper around a {@link Bus} that provides functionality specific to SAE
 * J1939
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class J1939 {

    private static final int ACKNOWLEDGEMENT_PGN = 0xE800;

    /**
     * The default time to wait for a response
     */
    private static final int DEFAULT_TIMEOUT = 2500;

    /**
     * The default time unit for responses
     */
    private static final TimeUnit DEFAULT_TIMEOUT_UNITS = TimeUnit.MILLISECONDS;

    /**
     * The source address of the engine
     */
    public static final int ENGINE_ADDR = 0x00;

    /**
     * The 'other' address for an engine
     */
    public static final int ENGINE_ADDR_1 = 0x01;

    /**
     * The global source address for broadcast
     */
    public static final int GLOBAL_ADDR = 0xFF;

    private static final int REQUEST_PGN = 0xEA00;

    /** J1939-21 */
    private static final int SUCCESS = 0x00;

    private static <T extends ParsedPacket> int getPgn(Class<T> cls)
            throws IllegalAccessException, NoSuchFieldException {
        return cls.getField("PGN").getInt(null);
    }

    private static Predicate<Packet> pgnFilter(int pgn) {
        return response -> (response.getId() == pgn) || ((response.getId() & 0xFF00) == pgn);
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the id
     *
     * @param id
     *            the id to match
     * @param packet
     *            the {@link Packet} to process
     * @return a subclass of {@link ParsedPacket}
     */
    private static ParsedPacket process(int id, Packet packet) {
        switch (id) {

        case DM5DiagnosticReadinessPacket.PGN:
            return new DM5DiagnosticReadinessPacket(packet);

        case DM6PendingEmissionDTCPacket.PGN:
            return new DM6PendingEmissionDTCPacket(packet);

        case DM7CommandTestsPacket.PGN:
            return new DM7CommandTestsPacket(packet);

        case DM11ClearActiveDTCsPacket.PGN:
            return new DM11ClearActiveDTCsPacket(packet);

        case DM12MILOnEmissionDTCPacket.PGN:
            return new DM12MILOnEmissionDTCPacket(packet);

        case DM19CalibrationInformationPacket.PGN:
            return new DM19CalibrationInformationPacket(packet);

        case DM20MonitorPerformanceRatioPacket.PGN:
            return new DM20MonitorPerformanceRatioPacket(packet);

        case DM21DiagnosticReadinessPacket.PGN:
            return new DM21DiagnosticReadinessPacket(packet);

        case DM23PreviouslyMILOnEmissionDTCPacket.PGN:
            return new DM23PreviouslyMILOnEmissionDTCPacket(packet);

        case DM24SPNSupportPacket.PGN:
            return new DM24SPNSupportPacket(packet);

        case DM26TripDiagnosticReadinessPacket.PGN:
            return new DM26TripDiagnosticReadinessPacket(packet);

        case DM28PermanentEmissionDTCPacket.PGN:
            return new DM28PermanentEmissionDTCPacket(packet);

        case DM30ScaledTestResultsPacket.PGN:
            return new DM30ScaledTestResultsPacket(packet);

        case ComponentIdentificationPacket.PGN:
            return new ComponentIdentificationPacket(packet);

        case EngineSpeedPacket.PGN:
            return new EngineSpeedPacket(packet);

        case EngineHoursPacket.PGN:
            return new EngineHoursPacket(packet);

        case HighResVehicleDistancePacket.PGN:
            return new HighResVehicleDistancePacket(packet);

        case TotalVehicleDistancePacket.PGN:
            return new TotalVehicleDistancePacket(packet);

        case VehicleIdentificationPacket.PGN:
            return new VehicleIdentificationPacket(packet);

        case AddressClaimPacket.PGN:
            return new AddressClaimPacket(packet);

        case REQUEST_PGN:
            // Request; just return a wrapped packet
            return new ParsedPacket(packet);

        default:
            int maskedId = id & 0xFF00;

            switch (maskedId) {
            case ACKNOWLEDGEMENT_PGN:
                // Acknowledgement, return the packet that was requested
                return process(packet.get24(5), packet);

            case DM7CommandTestsPacket.PGN:
            case DM19CalibrationInformationPacket.PGN:
            case DM20MonitorPerformanceRatioPacket.PGN:
            case DM21DiagnosticReadinessPacket.PGN:
            case DM30ScaledTestResultsPacket.PGN:
            case AddressClaimPacket.PGN:
                return process(maskedId, packet);

            default:
                // IDK
                return new ParsedPacket(packet);
            }
        }
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the given
     * {@link Packet}
     *
     * @param packet
     *            the {@link Packet} to process
     * @return a subclass of {@link ParsedPacket}
     */
    @SuppressWarnings("unchecked")
    private static <T extends ParsedPacket> T process(Packet packet) {
        return (T) process(packet.getId(), packet);
    }

    private static Predicate<Packet> sourceFilter(int addr) {
        return response -> response.getSource() == addr;
    }

    /**
     * The bus used to communicate with the vehicle
     */
    private final Bus bus;

    /** when true, all open streams will be closed. */
    private boolean interrupt;

    /** mechanism to interrupt all open streams when interrupt is true. */
    private final Predicate<Packet> interruptFn = Bus.interruptFilter(t -> {
        if (interrupt) {
            interrupt = false;
            return true;
        }
        return false;
    });

    /**
     * Constructor
     *
     * @param bus
     *            the {@link Bus} used to communicate with the vehicle
     */
    public J1939(Bus bus) {
        this.bus = bus;
    }

    /**
     * Filter to find acknowledgement packets
     *
     * @param pgn
     *            the pgn that's being requested
     * @return true if the message is an Acknowledgement for the given pgn
     */
    private Predicate<Packet> ackFilter(int pgn) {
        return response -> {
            final boolean b =
                    // ID is Acknowledgment
                    (response.getId() & 0xFF00) == 0xE800
                            // There are enough bytes
                            && response.getLength() == 8
            // The response is Success
                            && response.get(0) == SUCCESS
            // Accepting 0xFF as "Address Acknowledged" is to handle Cummins
                            && (response.get(4) == getBusAddress() || response.get(4) == 0xFF)
            // The Acknowledged PGN matches
                            && response.get24(5) == pgn;
            return b;
        };
    }

    /**
     * Filter to find acknowledgement/nack packets
     *
     * @param pgn
     *            the pgn that's being requested
     * @return true if the message is an Acknowledgement/Nack for the given pgn
     */
    private Predicate<Packet> ackNackFilter(int pgn) {
        return response -> {
            final boolean b =
                    // ID is Acknowledgment
                    (response.getId() & 0xFF00) == 0xE800
                            // There are enough bytes
                            && response.getLength() == 8
            // Accepting 0xFF as "Address Acknowledged" is to handle Cummins
                            && (response.get(4) == getBusAddress() || response.get(4) == 0xFF)
            // The Acknowledged PGN matches
                            && response.get24(5) == pgn;
            return b;
        };
    }

    /**
     * Helper to create a packet to request a packet with the given PGN be sent
     * by modules on the bus that support it
     *
     * @param pgn
     *            the PGN of the packet that's being request
     * @param addr
     *            the address the request is being directed at
     * @return a {@link Packet}
     */
    public Packet createRequestPacket(int pgn, int addr) {
        return Packet.create(0xEA00 | addr, getBusAddress(), true, pgn, pgn >> 8, pgn >> 16);
    }

    /**
     * Destination Specific PGN Filter
     *
     * @param pgn
     *            the PGN to filter
     * @return {@link Predicate}
     */
    private Predicate<Packet> dsPgnFilter(int pgn) {
        return response -> ((response.getId() & 0xFF00) == pgn)
                && (((response.getId() & 0xFF) == getBusAddress()) || ((response.getId() & 0xFF) == GLOBAL_ADDR));
    }

    /**
     * Exposed for system testing purposes. Calling classes should interact
     * directly with the bus
     *
     * @return the {@link Bus} that backs this class
     */
    public Bus getBus() {
        return bus;
    }

    /**
     * Returns the address of this tool on the bus
     *
     * @return the address of the tool
     */
    public int getBusAddress() {
        return getBus().getAddress();
    }

    /**
     * Returns the destination based upon the request
     *
     * @param requestPacket
     *            the request
     * @return the destination specific address or GLOBAL_ADDR
     */
    private int getDestination(Packet requestPacket) {
        return requestPacket.getId() < 0xF000 ? requestPacket.getId() & 0xFF : GLOBAL_ADDR;
    }

    private Logger getLogger() {
        return IUMPR.getLogger();
    }

    /**
     * Interrupts the communications to end a read early
     */
    public void interrupt() {
        interrupt = true;
    }

    /**
     * Reads the bus indefinitely
     *
     * @return {@link Stream} of {@link ParsedPacket} s
     * @throws BusException
     *             if there is a problem reading the bus
     */
    public Stream<ParsedPacket> read() throws BusException {
        return getBus().read(365, TimeUnit.DAYS).map(t -> process(t));
    }

    /**
     * Watches the bus for up to the timeout for the first packet that matches
     * the PGN in the given class
     *
     * @param <T>
     *            the Type of Packet to expect back
     *
     * @param T
     *            the class of interest
     * @param addr
     *            the source address the packet should come from. NOTE do not
     *            use the Global Address (0xFF) here
     * @param timeout
     *            the maximum time to wait for a message
     * @param unit
     *            the {@link TimeUnit} for the timeout
     * @return the resulting packet
     */
    public <T extends ParsedPacket> Optional<T> read(Class<T> T, int addr, long timeout, TimeUnit unit) {
        try {
            int pgn = getPgn(T);
            Stream<Packet> stream = read(timeout, unit);
            return stream
                    .filter(interruptFn)
                    .filter(sourceFilter(addr))
                    .filter(pgnFilter(pgn))
                    .findFirst()
                    .map(t -> process(t));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reading packets", e);
        }
        return Optional.empty();
    }

    /**
     * Watches the bus for up to the timeout for all the packets that match the
     * PGN in the given class
     *
     * @param <T>
     *            the Type of Packet to expect back
     * @param T
     *            the class of interest
     * @param timeout
     *            the maximum time to wait for a message
     * @param unit
     *            the {@link TimeUnit} for the timeout
     * @return the resulting packets in a Stream
     */
    public <T extends ParsedPacket> Stream<T> read(Class<T> T, long timeout, TimeUnit unit) {
        try {
            int pgn = getPgn(T);
            Stream<Packet> stream = read(timeout, unit);
            return stream
                    .filter(interruptFn)
                    .filter(pgnFilter(pgn))
                    .distinct()
                    .map(t -> process(t));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reading packets", e);
        }
        return Stream.empty();
    }

    private Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return getBus().read(timeout, unit)
                .peek(packet -> getLogger().log(Level.FINE, "P->" + packet.toString()));
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally. The request will wait for up to the timeout period.
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<T> requestMultiple(Class<T> T) {
        try {
            Packet requestPacket = createRequestPacket(getPgn(T), GLOBAL_ADDR);
            return requestMultiple(T, requestPacket);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
        }
        return Stream.empty();
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally. The request will wait for up to 1.25 seconds
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @param requestPacket
     *            the {@link Packet} to send that will generate the responses
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<T> requestMultiple(Class<T> T, Packet requestPacket) {
        return requestMultiple(T, requestPacket, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNITS);
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally. NACKs will be ignored.
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @param requestPacket
     *            the {@link Packet} to send that will generate the responses
     * @param timeout
     *            the maximum time to wait for responses
     * @param unit
     *            the {@link TimeUnit} of the timeout
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    private <T extends ParsedPacket> Stream<T> requestMultiple(Class<T> T, Packet requestPacket, long timeout,
            TimeUnit unit) {
        List<T> results = Collections.emptyList();
        for (int i = 0; i < 3; i++) {
            results = requestMultipleOnce(T, requestPacket, timeout, unit).collect(Collectors.toList());
            if (!results.isEmpty()) {
                break;
            }
        }
        return results.stream();
    }

    private <T extends ParsedPacket> Stream<T> requestMultipleOnce(Class<T> T, Packet requestPacket, long timeout,
            TimeUnit unit) {
        Stream<T> result = Stream.of();
        try {
            int pgn = getPgn(T);
            Stream<Packet> stream = read(timeout, unit);
            getBus().send(requestPacket);
            int destination = getDestination(requestPacket);
            result = stream
                    .filter(interruptFn)
                    .filter(sourceFilter(destination).or(p -> destination == GLOBAL_ADDR))
                    .filter(pgnFilter(pgn).or(ackFilter(pgn)))
                    .distinct()
                    .map(rawPacket -> process(rawPacket));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
        }
        return result;
    }

    /**
     * Sends a Request for the given packet. The request will repeat the given
     * number of tries.
     *
     * @param <T>
     *            the Type of Packet that will be returned
     * @param packetToSend
     *            the packet that will be sent
     * @param T
     *            the Class of packet that's expected to be returned
     * @param destination
     *            the address response should come from
     * @param tries
     *            the number of times to try the request
     * @return {@link Optional} {@link Packet} This may not contain a value if
     *         there was an exception
     */
    public <T extends ParsedPacket> Optional<T> requestPacket(Packet packetToSend, Class<T> T, int destination,
            int tries) {
        return requestPacket(packetToSend, T, destination, tries, DEFAULT_TIMEOUT);
    }

    /**
     * Sends a Request for the given packet. The request will repeat the given
     * number of tries.
     *
     * @param <T>
     *            the Type of Packet that will be returned
     * @param packetToSend
     *            the packet that will be sent
     * @param T
     *            the Class of packet that's expected to be returned
     * @param destination
     *            the address response should come from
     * @param tries
     *            the number of times to try the request
     * @param timeout
     *            the maximum time, in milliseconds, to wait for a response
     * @return {@link Optional} {@link Packet} This may not contain a value if
     *         there was an exception
     */
    public <T extends ParsedPacket> Optional<T> requestPacket(Packet packetToSend, Class<T> T, int destination,
            int tries, long timeout) {
        Optional<T> result = Optional.empty();
        if (tries <= 0) {
            // Give up
            return result;
        }

        try {
            int expectedResponsePGN = getPgn(T);

            Stream<Packet> stream = read(timeout, DEFAULT_TIMEOUT_UNITS);
            getBus().send(packetToSend);

            Stream<Packet> packets = stream
                    .filter(interruptFn)
                    .filter(sourceFilter(destination))
                    .filter(pgnFilter(expectedResponsePGN).or(dsPgnFilter(expectedResponsePGN)));
            result = packets.findFirst().map(p -> process(p));
            if (result.isPresent()) {
                return result;
            } else {
                return requestPacket(packetToSend, T, destination, tries - 1, timeout);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
            return result;
        }
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally. NACKs will NOT be ignored.
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @param requestPacket
     *            the {@link Packet} to send that will generate the responses
     * @param timeout
     *            the maximum time to wait for responses
     * @param unit
     *            the {@link TimeUnit} of the timeout
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<T> requestRaw(Class<T> T, Packet requestPacket, long timeout,
            TimeUnit unit) {
        Stream<T> result = Stream.of();
        try {
            int pgn = getPgn(T);
            Stream<Packet> stream = read(timeout, unit);
            getBus().send(requestPacket);
            int destination = getDestination(requestPacket);
            result = stream
                    .filter(interruptFn)
                    .filter(sourceFilter(destination).or(p -> destination == GLOBAL_ADDR))
                    .filter(pgnFilter(pgn).or(ackNackFilter(pgn)))
                    .distinct()
                    .map(rawPacket -> process(rawPacket));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
        }
        return result;
    }

}
