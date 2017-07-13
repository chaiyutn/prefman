package com.chongos.prefman;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ChongOS
 * @since 11-Jul-2017
 */
public final class Util {

  public static Set<String> fromArray(String[] strings) {
    return strings != null ? new HashSet<>(Arrays.asList(strings)) : null;
  }

  public static String[] toArray(Set<String> stringSet) {
    return stringSet != null ? stringSet.toArray(new String[stringSet.size()]) : null;
  }

}
