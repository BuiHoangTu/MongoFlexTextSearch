package special.org.models;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Observable<T>{
    @Getter
    private T value;
    // the value changed but @link{notifyChange} does not activate
    private T oldValue = null;
    private int consumerId = -1;
    private final Map<Integer, Consumer<Change<T>>> consumerMap = new HashMap<>();


    public Observable(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        if (oldValue == null) oldValue = value;
        this.value = value;
    }

    public int addListener(Consumer<Change<T>> listener) {
        consumerId ++;
        consumerMap.put(consumerId, listener);
        return consumerId;
    }

    public void removeListener(int id) {
        consumerMap.remove(id);
    }
    public void removeAllListener(Consumer<T> consumer) {
        // remove all
        boolean thereAreMore;
        do {
            thereAreMore = consumerMap.values().remove(consumer);
        } while (thereAreMore);
    }

    public void notifyChange() {
        for (var consumer : consumerMap.values()) {
            consumer.accept(new Change<T>() {
                @Override
                public T getValue() {
                    return value;
                }

                @Override
                public T getValueBeforeChange() {
                    return oldValue;
                }
            });
        }

        oldValue = null;
    }

    public interface Change<T> {
        T getValue();
        T getValueBeforeChange();
    }
}
