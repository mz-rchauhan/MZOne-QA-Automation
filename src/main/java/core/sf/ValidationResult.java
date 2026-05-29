package core.sf;

public final class ValidationResult {

    private final String field;
    private final String action;
    private final String expectedValue;
    private final String actualValue;
    private final boolean passed;
    private final String failureReason;

    private ValidationResult(Builder b) {
        this.field         = b.field;
        this.action        = b.action;
        this.expectedValue = b.expectedValue;
        this.actualValue   = b.actualValue;
        this.passed        = b.passed;
        this.failureReason = b.failureReason;
    }

    // ── Static factories ──────────────────────────────────────────────────────

    public static ValidationResult pass(String field, String action, String actual) {
        return new Builder()
                .field(field).action(action)
                .actualValue(actual).passed(true)
                .build();
    }

    public static ValidationResult fail(String field, String action,
                                        String expected, String actual, String reason) {
        return new Builder()
                .field(field).action(action)
                .expectedValue(expected).actualValue(actual)
                .passed(false).failureReason(reason)
                .build();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String  getField()         { return field; }
    public String  getAction()        { return action; }
    public String  getExpectedValue() { return expectedValue; }
    public String  getActualValue()   { return actualValue; }
    public boolean isPassed()         { return passed; }
    public String  getFailureReason() { return failureReason; }

    @Override
    public String toString() {
        if (passed) {
            return String.format("[PASS] %-25s | %-30s | actual='%s'",
                    field, action, actualValue);
        }
        return String.format("[FAIL] %-25s | %-30s | expected='%s' | actual='%s' | %s",
                field, action, expectedValue, actualValue, failureReason);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static class Builder {
        private String  field         = "";
        private String  action        = "";
        private String  expectedValue = "";
        private String  actualValue   = "";
        private boolean passed        = false;
        private String  failureReason = "";

        public Builder field(String v)         { this.field         = v; return this; }
        public Builder action(String v)        { this.action        = v; return this; }
        public Builder expectedValue(String v) { this.expectedValue = v; return this; }
        public Builder actualValue(String v)   { this.actualValue   = v; return this; }
        public Builder passed(boolean v)       { this.passed        = v; return this; }
        public Builder failureReason(String v) { this.failureReason = v; return this; }
        public ValidationResult build()        { return new ValidationResult(this); }
    }
}

