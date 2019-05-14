package io.slingr.endpoints.autotaskuser.polling;

import io.slingr.endpoints.utils.Json;

public interface EventSender {
    void sendEvent(String eventName, Json data, String userId);
}
