package special.org.models;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

public class ExecuteSet<T> implements Set<T> {
    private final long waitTime;

    private final Map<T, LocalDateTime> itemMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Set<T> itemSet = itemMap.keySet();

    public ExecuteSet(long waitTime, Consumer<T> consumer) {
        this.waitTime = waitTime;

        // buffer wait to ensure less Thread operation
        Thread consumingThread = new Thread(() -> {
            while (true) {
                synchronized (this) {
                    var firstEntry = itemMap.entrySet().stream().findFirst().orElse(null);
                    if (firstEntry == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        var waitTimeLeft = ChronoUnit.MILLIS.between(LocalDateTime.now(), firstEntry.getValue());
                        if (waitTimeLeft > 0) {
                            try {
                                waitTimeLeft += waitTime; // buffer wait to ensure less Thread operation
                                Thread.sleep(waitTimeLeft);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            var item2Consume = firstEntry.getKey();
                            itemMap.remove(item2Consume);
                            consumer.accept(item2Consume);
                        }

                    }
                }
            }
        });

        consumingThread.start();
    }

    private LocalDateTime getExecTime() {
        return LocalDateTime.now().plus(waitTime, ChronoUnit.MILLIS);
    }

    @Override
    public int size() {
        return itemMap.size();
    }

    @Override
    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return itemSet.contains(o);
    }

    @Override
    @NonNull
    public Iterator<T> iterator() {
        return itemSet.iterator();
    }

    @Override
    @NonNull
    public Object[] toArray() {
        return itemSet.toArray();
    }

    @Override
    @NonNull
    public <T1> T1[] toArray(T1[] a) {
        return itemSet.toArray(a);
    }

    /**
     * Add new element to consume
     * @param t element whose presence in this collection is to be ensured
     * @return if item is not in set yet
     */
    @Override
    public boolean add(T t) {
        var wasExisting = itemMap.remove(t) != null;
        itemMap.put(t, getExecTime());
        synchronized (this) {
            notify();
        }
        return !wasExisting;
    }

    @Override
    public boolean remove(Object o) {
        return itemSet.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return itemSet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return c.stream().anyMatch(this::add);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return itemSet.retainAll(c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return itemSet.removeAll(c);
    }

    @Override
    public void clear() {
        itemMap.clear();
    }
}
