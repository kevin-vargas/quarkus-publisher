package org.agoncal.quarkus.starting;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class MessageEvent {
    @NotBlank(message = "topic may not be blank")
    public String topic;
    @NotNull(message = "message may not be blank")
    public Object message;
}
