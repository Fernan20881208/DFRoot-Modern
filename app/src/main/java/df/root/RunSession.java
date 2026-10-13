package df.root;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** UI state survives rotation and never retains an Activity or starts on launch. */
final class RunSession implements IReporter {
    static final RunSession INSTANCE = new RunSession();
    interface Observer { void onSessionChanged(); }
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private WeakReference<Observer> observer = new WeakReference<>(null);
    private final StringBuilder log = new StringBuilder();
    private boolean running;
    private Integer result;
    private RunSession() {}
    // State and observer methods are called on the main thread only.
    void attach(Observer next) { observer = new WeakReference<>(next); next.onSessionChanged(); }
    void detach(Observer previous) { if (observer.get() == previous) observer.clear(); }
    boolean isRunning() { return running; }
    Integer result() { return result; }
    String logs() { return log.toString(); }
    void clear() { log.setLength(0); notifyObserver(); }
    private void notifyObserver() {
        Observer current = observer.get();
        if (current != null) current.onSessionChanged();
    }
    @Override public void report(String message) {
        Log.i("dfroot", message.trim());
        main.post(() -> {
            log.append(message);
            if (log.length() > 65536) log.delete(0, log.length() - 65536);
            notifyObserver();
        });
    }
    void start(Context context) {
        if (running) return;
        Context device = context.getApplicationContext().createDeviceProtectedStorageContext();
        running = true;
        result = null;
        log.setLength(0);
        notifyObserver();
        worker.execute(() -> {
            int exit = -1;
            try {
                exit = ExploitRunner.run(device, this);
                report("\nDFRoot v4.0 · exit=" + exit + "\n");
            } catch (Exception error) {
                report("\n" + error + "\n");
                Log.e("dfroot", "Runner failed", error);
            }
            final int completed = exit;
            main.post(() -> { result = completed; running = false; notifyObserver(); });
        });
    }
}
