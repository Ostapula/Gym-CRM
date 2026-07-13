package gym.crm.logging;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public final class TransactionContext {
    public static final String HEADER_NAME = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    private TransactionContext() {
    }

    public static String newTransactionId() {
        return UUID.randomUUID().toString();
    }

    public static void setTransactionId(String transactionId) {
        MDC.put(MDC_KEY, transactionId);
    }

    public static Optional<String> currentTransactionId() {
        return Optional.ofNullable(MDC.get(MDC_KEY));
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}

