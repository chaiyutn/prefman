package com.chongos.prefman;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import java.util.Map;
import java.util.Set;


/**
 * @author ChongOS
 * @since 08-Jul-2017
 */
public abstract class Preference {

  private static final boolean DEBUG = true;
  private SharedPreferences sharedPreferences;

  public Preference(SharedPreferences preferences) {
    this.sharedPreferences = preferences;
  }

  protected <T> Observable<T> createObservable(final String key, final Callable<T> callable) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(final ObservableEmitter<T> emitter) throws Exception {
        final OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
          @Override
          public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (!emitter.isDisposed() && s.equals(key)) {
              emitter.onNext(callable.call());
            }
          }
        };

        if (!emitter.isDisposed()) {
          sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
          emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
              sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
            }
          });
        }

        if (!emitter.isDisposed()) {
          emitter.onNext(callable.call());
        }
      }
    });
  }

  protected Editor editor() {
    return sharedPreferences.edit();
  }

  protected int getInt(String key, int defaultValue) {
    return sharedPreferences.getInt(key, defaultValue);
  }

  protected float getFloat(String key, float defaultValue) {
    return sharedPreferences.getFloat(key, defaultValue);
  }

  protected boolean getBoolean(String key, boolean defaultValue) {
    return sharedPreferences.getBoolean(key, defaultValue);
  }

  protected long getLong(String key, long defaultValue) {
    return sharedPreferences.getLong(key, defaultValue);
  }

  protected String getString(String key, String defaultValue) {
    return sharedPreferences.getString(key, defaultValue);
  }

  protected Set<String> getStringSet(String key, Set<String> defaultValue) {
    return sharedPreferences.getStringSet(key, defaultValue);
  }

  public void clear() {
    sharedPreferences.edit().clear().apply();
  }

  public Map<String, ?> getAll() {
    return sharedPreferences.getAll();
  }

  private void log(String msg) {
    if (DEBUG) {
      Log.d(Preference.class.getSimpleName(), msg);
    }
  }
}
