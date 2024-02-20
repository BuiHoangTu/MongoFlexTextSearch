package special.org.models;

import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;

public class ObservableSet<T> extends HashSet<T> implements Observable<Set<T>>{
    private final Map<Integer, Consumer<Change<Set<T>>>> consumerMap = new HashMap<>();
    private int consumerId = 0;
    private boolean isInBatchChange = false;
    private Set<T> oldValue = null;

    public ObservableSet(List<T> databases) {
        super(databases);
    }

    @Override
    public int addListener(@NonNull Consumer<Change<Set<T>>> consumer) {
        // try all next id from current
        // if current consumer not exist, put new consumer & exist
        // if current consumer == new consumer, exist
        // else try next consumer slot
        do {
            var current = consumerMap.get(consumerId);
            if (current == null) {
                consumerMap.put(consumerId, consumer);
                return consumerId;
            }

            if (current.equals(consumer)) return consumerId;
            consumerId ++;
        } while (true);
    }

    @Override
    public boolean removeListener(int id) {
        var res = consumerMap.remove(id);

        return res != null;
    }

    @Override
    public boolean removeListener(Consumer<Change<Set<T>>> consumer) {
        return consumerMap.values().remove(consumer);
    }

    @Override
    public boolean removeAllListener(Consumer<Change<Set<T>>> consumer) {
        return consumerMap.values().removeAll(Collections.singleton(consumer));
    }

    @Override
    public Batch startBatchChanges() {
        isInBatchChange = true;

        return new Batch(this);
    }

    @Override
    public void submitBatchChanges() {
        isInBatchChange = false;
        notifyChange();
    }

    // send changes to listener and remove oldValue
    private void notifyChange() {
        var outer = this;
        for (var consumer : consumerMap.values()) {
            consumer.accept(new Change<>() {
                @Override
                public Set<T> getValue() {
                    return outer;
                }

                @Override
                public Set<T> getValueBeforeChange() {
                    return oldValue;
                }
            });
        }

        oldValue = null;
    }

    @Override
    public boolean add(T t) {
        if (isInBatchChange && oldValue == null) oldValue = Set.copyOf(this);

        oldValue = Set.copyOf(this);
        var res = super.add(t);
        notifyChange();
        return res;
    }

    @Override
    public boolean remove(Object o) {
        if (isInBatchChange && oldValue == null) oldValue = Set.copyOf(this);

        oldValue = Set.copyOf(this);
        var res = super.remove(o);
        notifyChange();
        return res;
    }

    @Override
    public void clear() {
        if (isInBatchChange && oldValue == null) oldValue = Set.copyOf(this);

        oldValue = Set.copyOf(this);
        notifyChange();
        super.clear();
    }
}
