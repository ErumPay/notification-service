package com.erumpay.notification.client.push;

import com.erumpay.notification.client.push.dto.PushSendRequest;
import com.erumpay.notification.client.push.dto.PushSendResult;

public interface PushClient {

    PushSendResult send(PushSendRequest request);
}
