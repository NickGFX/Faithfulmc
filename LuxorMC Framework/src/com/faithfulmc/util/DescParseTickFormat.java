package com.faithfulmc.util;

import java.text.SimpleDateFormat;
import java.util.*;

public final class DescParseTickFormat
{
    public static final Map<String, Integer> nameToTicks;
    public static final Map<Integer, String> ticksToName;
    public static final Set<String> resetAliases;
    public static final int ticksAtMidnight = 18000;
    public static final int ticksPerDay = 24000;
    public static final int ticksPerHour = 1000;
    public static final double ticksPerMinute = 16.666666666666668;
    public static final double ticksPerSecond = 0.2777777777777778;
    private static final SimpleDateFormat SDFTwentyFour;
    private static final SimpleDateFormat SDFTwelve;
    
    public static long parse(String desc) throws NumberFormatException {
        desc = desc.toLowerCase(Locale.ENGLISH).replaceAll("[^A-Za-z0-9:]", "");
        try {
            return parseTicks(desc);
        }
        catch (NumberFormatException e1) {
            try {
                return parse24(desc);
            }
            catch (NumberFormatException e2) {
                try {
                    return parse12(desc);
                }
                catch (NumberFormatException e3) {
                    try {
                        return parseAlias(desc);
                    }
                    catch (NumberFormatException e4) {
                        throw new NumberFormatException();
                    }
                }
            }
        }
    }
    
    public static long parseTicks(String desc) throws NumberFormatException {
        if (!desc.matches("^[0-9]+ti?c?k?s?$")) {
            throw new NumberFormatException();
        }
        desc = desc.replaceAll("[^0-9]", "");
        return Long.parseLong(desc) % 24000L;
    }
    
    public static long parse24(String desc) throws NumberFormatException {
        if (!desc.matches("^[0-9]{2}[^0-9]?[0-9]{2}$")) {
            throw new NumberFormatException();
        }
        desc = desc.toLowerCase(Locale.ENGLISH).replaceAll("[^0-9]", "");
        if (desc.length() != 4) {
            throw new NumberFormatException();
        }
        final int hours = Integer.parseInt(desc.substring(0, 2));
        final int minutes = Integer.parseInt(desc.substring(2, 4));
        return hoursMinutesToTicks(hours, minutes);
    }
    
    public static long parse12(String desc) throws NumberFormatException {
        if (!desc.matches("^[0-9]{1,2}([^0-9]?[0-9]{2})?(pm|am)$")) {
            throw new NumberFormatException();
        }
        int hours = 0;
        int minutes = 0;
        desc = desc.toLowerCase(Locale.ENGLISH);
        final String parsetime = desc.replaceAll("[^0-9]", "");
        if (parsetime.length() > 4) {
            throw new NumberFormatException();
        }
        if (parsetime.length() == 4) {
            hours += Integer.parseInt(parsetime.substring(0, 2));
            minutes += Integer.parseInt(parsetime.substring(2, 4));
        }
        else if (parsetime.length() == 3) {
            hours += Integer.parseInt(parsetime.substring(0, 1));
            minutes += Integer.parseInt(parsetime.substring(1, 3));
        }
        else if (parsetime.length() == 2) {
            hours += Integer.parseInt(parsetime.substring(0, 2));
        }
        else {
            if (parsetime.length() != 1) {
                throw new NumberFormatException();
            }
            hours += Integer.parseInt(parsetime.substring(0, 1));
        }
        if (desc.endsWith("pm") && hours != 12) {
            hours += 12;
        }
        if (desc.endsWith("am") && hours == 12) {
            hours -= 12;
        }
        return hoursMinutesToTicks(hours, minutes);
    }
    
    public static long hoursMinutesToTicks(final int hours, final int minutes) {
        long ret = 18000L;
        ret += hours * 1000;
        ret += (long)(minutes / 60.0 * 1000.0);
        ret %= 24000L;
        return ret;
    }
    
    public static long parseAlias(final String desc) throws NumberFormatException {
        final Integer ret = DescParseTickFormat.nameToTicks.get(desc);
        if (ret == null) {
            throw new NumberFormatException();
        }
        return ret;
    }
    
    public static boolean meansReset(final String desc) {
        return DescParseTickFormat.resetAliases.contains(desc);
    }
    
    public static String formatTicks(final long ticks) {
        return ticks % 24000L + "ticks";
    }
    
    public static String format24(final long ticks) {
        synchronized (DescParseTickFormat.SDFTwentyFour) {
            return formatDateFormat(ticks, DescParseTickFormat.SDFTwentyFour);
        }
    }
    
    public static String format12(final long ticks) {
        synchronized (DescParseTickFormat.SDFTwelve) {
            return formatDateFormat(ticks, DescParseTickFormat.SDFTwelve);
        }
    }
    
    public static String formatDateFormat(final long ticks, final SimpleDateFormat format) {
        String name = ticksToName.get((int)ticks);
        if(name != null){
            return name;
        }
        final Date date = ticksToDate(ticks);
        return format.format(date);
    }
    
    public static Date ticksToDate(long ticks) {
        ticks = ticks - 18000L + 24000L;
        final long days = ticks / 24000L;
        ticks -= days * 24000L;
        final long hours = ticks / 1000L;
        ticks -= hours * 1000L;
        final long minutes = (long)Math.floor(ticks / 16.666666666666668);
        final double dticks = ticks - minutes * 16.666666666666668;
        final long seconds = (long)Math.floor(dticks / 0.2777777777777778);
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        cal.setLenient(true);
        cal.set(0, 0, 1, 0, 0, 0);
        cal.add(6, (int)days);
        cal.add(11, (int)hours);
        cal.add(12, (int)minutes);
        cal.add(13, (int)seconds + 1);
        return cal.getTime();
    }
    
    static {
        nameToTicks = new LinkedHashMap<String, Integer>();
        ticksToName = new LinkedHashMap<>();
        resetAliases = new HashSet<String>();
        SDFTwentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SDFTwelve = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);
        SDFTwentyFour.setTimeZone(TimeZone.getTimeZone("GMT"));
        SDFTwelve.setTimeZone(TimeZone.getTimeZone("GMT"));
        nameToTicks.put("sunrise", 23000);
        nameToTicks.put("dawn", 23000);
        nameToTicks.put("daystart", 0);
        nameToTicks.put("day", 0);
        nameToTicks.put("morning", 1000);
        nameToTicks.put("midday", 6000);
        nameToTicks.put("noon", 6000);
        nameToTicks.put("afternoon", 9000);
        nameToTicks.put("sunset", 12000);
        nameToTicks.put("dusk", 12000);
        nameToTicks.put("sundown", 12000);
        nameToTicks.put("nightfall", 12000);
        nameToTicks.put("nightstart", 14000);
        nameToTicks.put("night", 14000);
        nameToTicks.put("midnight", 18000);
        ticksToName.put(23000, "dawn");
        ticksToName.put(0, "day");
        ticksToName.put(14000, "night");
        ticksToName.put(18000, "midnight");
        ticksToName.put(6000, "noon");
        ticksToName.put(9000, "sunset");
        resetAliases.add("reset");
        resetAliases.add("normal");
        resetAliases.add("default");
    }
}