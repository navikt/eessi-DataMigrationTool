package eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Longs;

public class DelayedObject implements Delayed {
    public final String key;
    public final long expiryTime;

    public DelayedObject(final String key, final long delay) {
        this.key = key;
        this.expiryTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(Delayed o) {
        return Longs.compare(expiryTime, ((DelayedObject) o).expiryTime);
    }
}
