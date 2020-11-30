package co.cargoai.sqs.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

class TestMessage {

    @JsonProperty("message")
    private String message;

    public TestMessage() {
    }

    public TestMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
