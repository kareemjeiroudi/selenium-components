package at.porscheinformatik.seleniumcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Global settings for Selenium. The settings can be set by System properties.
 * <table>
 * <tr>
 * <th>{@value #DEBUG_KEY}</th>
 * <td>If set to true, timeouts are set to {@link Double#POSITIVE_INFINITY}.</td>
 * </tr>
 * <tr>
 * <th>{@value #TIME_MULTIPLIER_KEY}</th>
 * <td>A multiplier for timeouts. A value &gt; 1 increases timeouts. 0 or NaN disables timeouts.</td>
 * </tr>
 * </table>
 *
 * @author ham
 */
public final class SeleniumGlobals
{

    public static final String DEBUG_KEY = "selenium-components.debug";
    public static final String TIME_MULTIPLIER_KEY = "selenium-components.timeMultiplier";
    public static final String SHORT_TIMEOUT_IN_SECONDS_KEY = "selenium-components.shortTimeoutInSeconds";
    public static final String LONG_TIMEOUT_IN_SECONDS_KEY = "selenium-components.longTimeoutInSeconds";

    private static final List<Pattern> IGNORABLE_CALL_ELEMENTS = new ArrayList<>();

    private static boolean debug = false;
    private static double timeMultiplier = 1;
    private static double shortTimeoutInSeconds = 1.0;
    private static double longTimeoutInSeconds = 10;

    static
    {
        String debug = System.getProperty(DEBUG_KEY);

        if ("".equals(debug) || "true".equals(debug))
        {
            setDebug(true);
        }

        String timeMultiplier = System.getProperty(TIME_MULTIPLIER_KEY);

        if (timeMultiplier != null)
        {
            setTimeMultiplier(Double.parseDouble(timeMultiplier));
        }

        String shortTimeoutInSeconds = System.getProperty(SHORT_TIMEOUT_IN_SECONDS_KEY);

        if (timeMultiplier != null)
        {
            setTimeMultiplier(Double.parseDouble(shortTimeoutInSeconds));
        }

        String longTimeoutInSeconds = System.getProperty(LONG_TIMEOUT_IN_SECONDS_KEY);

        if (timeMultiplier != null)
        {
            try
            {
                setTimeMultiplier(Double.parseDouble(longTimeoutInSeconds));
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Failed to parse property", e);
            }
        }

        addIgnorableCallElement(Pattern.compile("^java\\.lang\\..*"));
        addIgnorableCallElement(Pattern.compile("^"
            + SeleniumUtils.class.getName().substring(0, SeleniumUtils.class.getName().lastIndexOf(".")).replace(".",
                "\\.")
            + ".*"));
    }

    private SeleniumGlobals()
    {
        super();
    }

    /**
     * Returns true if debugging is enabled
     *
     * @return true if debugging
     */
    public static boolean isDebug()
    {
        return debug;
    }

    /**
     * Sets the debug mode.
     *
     * @param debug the debug mode
     */
    public static void setDebug(boolean debug)
    {
        SeleniumGlobals.debug = debug;
    }

    /**
     * Some functions rely on timeouts, like the isInvisible call. It not good, but it's a fact. The debug mode breaks
     * these functions. Use this method to disable the debug mode.
     *
     * @param runnable the code to execute
     */
    public static void ignoreDebug(Runnable runnable)
    {
        boolean debug = isDebug();

        try
        {
            setDebug(false);

            runnable.run();
        }
        finally
        {
            setDebug(debug);
        }
    }

    /**
     * Some functions rely on timeouts, like the isInvisible call. It not good, but it's a fact. The debug mode breaks
     * these functions. Use this method to disable the debug mode.
     *
     * @param <Any> the type of return value
     * @param callable the code to execute
     * @return the result of the call
     */
    public static <Any> Any ignoreDebug(Callable<Any> callable)
    {
        boolean debug = isDebug();

        try
        {
            setDebug(false);

            return callable.call();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SeleniumException(String.format("Call failed: %s", Utils.describeCallLine()), e);
        }
        finally
        {
            setDebug(debug);
        }
    }

    /**
     * Returns the multiplier, that will be applied to any timeout or delay passed to this class.
     *
     * @return the multiplier
     */
    public static double getTimeMultiplier()
    {
        return timeMultiplier;
    }

    /**
     * Any timeout or delay passed to this class, will be multiplied by the specified multiplier. The default value is
     * 1.
     *
     * @param timeMultiplier the multiplier
     */
    public static void setTimeMultiplier(double timeMultiplier)
    {
        SeleniumGlobals.timeMultiplier = timeMultiplier;
    }

    /**
     * Returns the timeout used for short user interactions like looking at some text or clicking a link. The returned
     * was not yet multiplied by the time multiplier, this is usually done by the timeout methods in the
     * {@link SeleniumUtils}.
     *
     * @return the timeout in seconds
     */
    public static double getShortTimeoutInSeconds()
    {
        return shortTimeoutInSeconds;
    }

    /**
     * Sets the timeout used for short user interactions like looking at some text or clicking a link.
     *
     * @param shortTimeoutInSeconds the timeout in seconds
     */
    public static void setShortTimeoutInSeconds(double shortTimeoutInSeconds)
    {
        SeleniumGlobals.shortTimeoutInSeconds = shortTimeoutInSeconds;
    }

    /**
     * Returns the timeout used for long user interactions like loading a page. The returned was not yet multiplied by
     * the time multiplier, this is usually done by the timeout methods in the {@link SeleniumUtils}.
     *
     * @return the timeout in seconds
     */
    public static double getLongTimeoutInSeconds()
    {
        return longTimeoutInSeconds;
    }

    /**
     * Sets the timeout used for long user interactions like loading a page.
     *
     * @param longTimeoutInSeconds the timeout in seconds
     */
    public static void setLongTimeoutInSeconds(double longTimeoutInSeconds)
    {
        SeleniumGlobals.longTimeoutInSeconds = longTimeoutInSeconds;
    }

    /**
     * Adds an item to the list of {@link Pattern}s for method names, that should be ignored in call stacks. This
     * patterns are used in the {@link Utils#describeCallLine()} methods to find the caller excluding the framework
     * itself.
     *
     * @param pattern the pattern
     */
    public static void addIgnorableCallElement(Pattern pattern)
    {
        if (IGNORABLE_CALL_ELEMENTS.contains(pattern))
        {
            return;
        }

        IGNORABLE_CALL_ELEMENTS.add(Objects.requireNonNull(pattern));
    }

    /**
     * Returns a list of {@link Pattern}s for method names, that should be ignored in call stacks. This patterns are
     * used in the {@link Utils#describeCallLine()} methods to find the caller excluding the framework itself.
     *
     * @return the patterns
     */
    public static List<Pattern> getIgnorableCallElements()
    {
        return IGNORABLE_CALL_ELEMENTS;
    }

}
