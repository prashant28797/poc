
package com.services;

import aero.sita.lab.hostintegration.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * This abstract class contains functionality common to dummy host services.
 * 
 * 
 * @author kevino
 */
public abstract class AbstractGhostHostService extends AbstractService {
    /** variable declaration. */
    private static final Logger logger = LoggerFactory.getLogger(AbstractGhostHostService.class);

    /**
     * Airline code to use in auto generated responses.
     * <p>
     * Override in spring.config.xml
     * </p>
     */
    private String airlineCode = "MH";

    /**
     * Stores the amount if time in milliseconds to delay - set in spring.config.xml
     */
    private long delay = 0;
    /**
     * Stores the min duration in milliseconds to delay - set in spring.config.xml
     */
    private long randomDelayMin = 0;
    /**
     * Stores the max duration in milliseconds to delay - set in spring.config.xml
     */
    private long randomDelayMax = 0;
    /** variable declaration. */
    Random randomGenerator = new Random();

    /**
     * This is a list of predefined flight responses for a given route that can be
     * used to generate more realistic canned responses.
     * 
     * The key is the dep and arrival cities concatinated (e.g. ATLMIA)
     * 
     * This element is configured in spring.config.xml
     */
    private HashMap<String, List<GhostRouteData>> preDefinedRoutes;

    /**
     * If non null, the operating airline for bookings loaded will be set to this
     * value. Can be used to override the airline in XML data captured.
     * 
     * @see #operatingAirlineChangeFrom
     */
    private String operatingAirlineChangeTo;
    /**
     * If non null, specifies the airline that will be changed to the
     * <code>operatingAirlineChangeTo</code> value. Use this to control which
     * airlines codes are modified
     * 
     * @see #operatingAirlineChangeTo
     */
    private String operatingAirlineChangeFrom;
    /**
     * If non null, the origin for canned data loaded will be set to this value. Can
     * be used to override the airline in XML data captured.
     * 
     * @see #originChangeFrom
     */
    private String originChangeTo;
    /**
     * If non null, specifies the origin that will be changed to the
     * <code>originChangeTo</code> value. Use this to control which origin codes are
     * modified
     * 
     * @see #originChangeTo
     */
    private String originChangeFrom;

    /**
     * If non null, the destination for bookings loaded will be set to this value.
     * Can be used to override the airline in XML data captured.
     * 
     * @see #destinationChangeFrom;
     */
    private String destinationChangeTo;
    /**
     * If non null, specifies the destination that will be changed to the
     * <code>destinationChangeTo</code> value. Use this to control which destination
     * codes are modified
     * 
     * @see #destinationChangeTo
     */
    private String destinationChangeFrom;

    /**
     * If non null, the flightNum for bookings loaded will be set to this value. Can
     * be used to override the airline in XML data captured.
     * 
     * @see #flightNumberChangeFrom;
     */
    private String flightNumberChangeTo;
    /**
     * If non null, specifies the flightNum that will be changed to the
     * <code>flightNumberChangeTo</code> value. Use this to control which flightNum
     * codes are modified
     * 
     * @see #flightNumberChangeTo
     */
    private String flightNumberChangeFrom;

    protected List<GhostRouteData> getPredefinedRoutes(final String route) {
        if (preDefinedRoutes == null) {
            return null;
        }
        return preDefinedRoutes.get(route);
    }

    /**
     * This method will delay for a period of time. The period of time can be the
     * same every time or can be a random delay.
     * 
     * <p>
     * If delay is greater than zero, this value will be used.
     * </p>
     * <p>
     * If randomDelayMin & randomDelayMax are set, these values will be used.
     * </p>
     * 
     * <p>
     * Note: delay takes preecendence over the random delay values if both are set
     * </p>
     * 
     * <p>
     * These values should be set in
     */
    protected void simulateDelay() {

        long delayValue = delay;

        // Only
        if (delayValue == 0 && randomDelayMax > 0 && randomDelayMax > randomDelayMin) {
            delayValue = randomDelayMin + randomGenerator.nextInt((int) (randomDelayMax - randomDelayMin));
        }

        try {
            logger.info("simulateDelay(" + delayValue + "ms)");
            Thread.sleep(delayValue);
        } catch (InterruptedException e) {
            logger.error("simulateDelay interrupted ", e);
        }
    }

    public void setDelay(final long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public void setRandomDelayMin(final long randomDelayMin) {
        this.randomDelayMin = randomDelayMin;
    }

    public long getRandomDelayMin() {
        return randomDelayMin;
    }

    public void setRandomDelayMax(final long randomDelayMax) {
        this.randomDelayMax = randomDelayMax;
    }

    public long getRandomDelayMax() {
        return randomDelayMax;
    }

    public HashMap<String, List<GhostRouteData>> getPreDefinedRoutes() {
        return preDefinedRoutes;
    }

    public void setPreDefinedRoutes(final HashMap<String, List<GhostRouteData>> preDefinedRoutes) {
        this.preDefinedRoutes = preDefinedRoutes;
    }

    public void setAirlineCode(final String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    /**
     * Util function to return the file url.
     * 
     * @param fname
     * @return
     */
    protected static URL getURL(final String fname) {
        URL url = null;
        try {
            // if we already have a resolved file URL, just construct the URL and return it
            if (fname.startsWith("file:")) {
                // if this is a special jar, then prefix with jar:
                if (fname.contains("!/")) {
                    return new URL("jar:" + fname);
                } else {
                    return new URL(fname);
                }
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            // This loader works on Tomcat
            url = classLoader.getResource(fname);
            if (url == null) {
                // And this one works for junit tests
                url = AbstractGhostHostService.class.getClassLoader().getClass().getResource(fname);
            }
            // if we can't find it yet, strip off the initial / and do a find
            if (url == null && fname.startsWith("/")) {
                url = classLoader.getResource(fname.substring(1));
            }
            if (url == null) {
                File file = new File(fname);
                if (file.exists()) {
                    url = file.toURI().toURL();
                }
            }
        } catch (Exception e) {
            logger.error("Unable to get URL for file " + fname, e);
            return null;
        }
        return url;
    }

    /**
     * Util function to load XML data files used during dev cycle.
     * 
     * @param fname
     * @return
     * @throws IOException
     */
    protected String loadFile(final String fname) throws IOException {
        URL url = this.getClass().getResource(fname);
        if (url == null) {
            url = new URL("file://" + fname);
        }
        InputStreamReader ins = null;
        StringBuffer fileData = new StringBuffer();
        InputStream stream = null;
        BufferedReader bufferedReader = null;
        try {
            String thisLine;
            ins = new InputStreamReader(url.openStream());
            bufferedReader = new BufferedReader(ins);
            while ((thisLine = bufferedReader.readLine()) != null) { // while loop
                fileData.append(thisLine);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                }
            }

        }
        return fileData.toString();
    }

    /**
     * Load the XML file and return a org.w3c.dom.Document.
     * 
     * @param fname
     * @return
     * @throws Exception
     */
    protected Document loadFileAsDocument(final String fname) throws Exception {
        return XMLUtils.XMLStringToDocument(loadFile(fname));
    }

    public String getOperatingAirlineChangeTo() {
        return operatingAirlineChangeTo;
    }

    public void setOperatingAirlineChangeTo(final String operatingAirline) {
        this.operatingAirlineChangeTo = operatingAirline;
    }

    public void setOperatingAirlineChangeFrom(final String operatingAirlineChangeFrom) {
        this.operatingAirlineChangeFrom = operatingAirlineChangeFrom;
    }

    public String getOperatingAirlineChangeFrom() {
        return operatingAirlineChangeFrom;
    }

    public String getOriginChangeFrom() {
        return originChangeFrom;
    }

    public void setOriginChangeFrom(final String originChangeFrom) {
        this.originChangeFrom = originChangeFrom;
    }

    public String getDestinationChangeTo() {
        return destinationChangeTo;
    }

    public void setDestinationChangeTo(final String destinationChangeTo) {
        this.destinationChangeTo = destinationChangeTo;
    }

    public String getDestinationChangeFrom() {
        return destinationChangeFrom;
    }

    public void setDestinationChangeFrom(final String destinationChangeFrom) {
        this.destinationChangeFrom = destinationChangeFrom;
    }

    public String getOriginChangeTo() {
        return originChangeTo;
    }

    public void setOriginChangeTo(final String originChangeTo) {
        this.originChangeTo = originChangeTo;
    }

    public void setFlightNumberChangeTo(final String flightNumberChangeTo) {
        this.flightNumberChangeTo = flightNumberChangeTo;
    }

    public String getFlightNumberChangeTo() {
        return flightNumberChangeTo;
    }

    public void setFlightNumberChangeFrom(final String flightNumberChangeFrom) {
        this.flightNumberChangeFrom = flightNumberChangeFrom;
    }

    public String getFlightNumberChangeFrom() {
        return flightNumberChangeFrom;
    }

}
