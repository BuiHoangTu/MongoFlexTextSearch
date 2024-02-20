package special.org.models;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleObservable<T> implements Observable<T>{
    @Getter
    private T value;
    // the value changed but @link{notifyChange} does not activate
    private T oldValue = null;
    private int consumerId = -1;
    private final Map<Integer, Consumer<Observable.Change<T>>> consumerMap = new HashMap<>();


    public SimpleObservable(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        if (oldValue == null) oldValue = value;
        this.value = value;
    }

    @Override
    public int addListener(Consumer<Observable.Change<T>> listener) {
        consumerId ++;
        consumerMap.put(consumerId, listener);
        return consumerId;
    }

    @Override
    public boolean removeListener(int id) {
        var res = consumerMap.remove(id);
        return res != null;
    }

    @Override
    public boolean removeListener(Consumer<Change<T>> consumer) {
        return consumerMap.values().remove(consumer);
    }

    @Override
    public boolean removeAllListener(Consumer<Observable.Change<T>> consumer) {
        return consumerMap.values().removeAll(Collections.singleton(consumer));
    }

    @Override
    public Batch startBatchChanges() {
        return null;
    }

    @Override
    public void submitBatchChanges() {

    }

    public void notifyChange() {
        for (var consumer : consumerMap.values()) {
            consumer.accept(new Observable.Change<T>() {
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

}
