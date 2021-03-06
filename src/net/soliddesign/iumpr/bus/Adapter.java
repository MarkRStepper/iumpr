/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus;

/**
 * Represents a vehicle communications adapter
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Adapter {

    /**
     * The ID of the Device from the INI file
     */
    private final short deviceId;

    /**
     * The name of the DLL/INI file for the adapter
     */
    private final String dllName;

    /**
     * The display name for the adapter
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name
     *            the display name for the adapter
     * @param dllName
     *            the DLL/INI file for the adapter
     * @param deviceId
     *            the device ID
     */
    public Adapter(String name, String dllName, short deviceId) {
        this.name = name;
        this.dllName = dllName;
        this.deviceId = deviceId;
    }

    /**
     * The ID of the Device
     *
     * @return the deviceId
     */
    public short getDeviceId() {
        return deviceId;
    }

    /**
     * The Name of the DLL
     *
     * @return the dllName
     */
    public String getDLLName() {
        return dllName;
    }

    /**
     * The display name for the Adapter
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

}
