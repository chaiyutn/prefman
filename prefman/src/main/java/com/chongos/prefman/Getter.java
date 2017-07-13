package com.chongos.prefman;

import io.reactivex.Observable;

/**
 * @author ChongOS
 * @since 12-Jul-2017
 */
public class Getter<T> {

  private final Preference preference;
  private final String key;
  private final Callable<T> callable;

  public Getter(Preference preference, String key, Callable<T> callable) {
    this.preference = preference;
    this.key = key;
    this.callable = callable;
  }

  public T asValue() {
    return callable.call();
  }

  public Observable<T> asObservable() {
    return preference.createObservable(key, callable);
  }

}
