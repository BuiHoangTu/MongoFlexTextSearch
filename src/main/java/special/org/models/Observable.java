package special.org.models;

import java.util.function.Consumer;

public interface Observable<T> {
    /**
     * Add a listener which will be executed after changes.
     * @param consumer listener
     * @return id of this listener in this observable;
     */
    int addListener(Consumer<Change<T>> consumer);

    /**
     * Remove listener by the returned id
     * @param id id returned when {@link #addListener(Consumer)}
     * @return - true if there is a listener removed
     */
    boolean removeListener(int id);
    /**
     * Remove listener
     * @param consumer that need removing
     * @return - true if there is a listener removed
     */
    boolean removeListener(Consumer<Change<T>> consumer);

    /**
     * Remove all listeners that equal to consumer
     * @param consumer that need removing
     * @return true if there is at least one listener removed
     */
    default boolean removeAllListener(Consumer<Change<T>> consumer) {
        boolean thereIsAtLeastOne = false;
        while (removeListener(consumer)) {
            thereIsAtLeastOne = true;
        }
        return thereIsAtLeastOne;
    }


    /**
     * Hold all next changes until {@link #submitBatchChanges()} to merge into one change.
     */
    Batch startBatchChanges();

    /**
     * Merge all changes from previous {@link #startBatchChanges()} into one change and submit to all listener.
     */
    void submitBatchChanges();

    interface Change<T> {
        T getValue();
        T getValueBeforeChange();
    }

    class Batch implements AutoCloseable {
        private final Observable<?> host;

        public Batch(Observable<?> host) {
            this.host = host;
        }


        @Override
        public void close() {
            host.submitBatchChanges();
        }
    }
}
