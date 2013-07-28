package me.riking.bungeemmo.common.messaging;

import java.io.IOException;

/**
 * A PluginMessage came in the wrong format.
 */
public class MalformedMessageException extends IOException {
    private static final long serialVersionUID = 2121800848158974115L;

    public MalformedMessageException() {
        super();
    }

    public MalformedMessageException(String message) {
        super(message);
    }

    public MalformedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedMessageException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "A PluginMessage was in an unexpected format. Description: " + super.getMessage();
    }
}
