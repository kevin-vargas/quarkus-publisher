package org.agoncal.quarkus.starting;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.executable.ValidateOnExecution;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.smallrye.reactive.messaging.kafka.Record;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/publish")
public class MessageResource {
    @Channel("message-t")
    MutinyEmitter<MessageEvent> messageEmitter;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> publish(@Valid MessageEvent request) {
        var metadata = OutgoingKafkaRecordMetadata
                .builder()
                .withTopic(request.topic)
                .build();
        var msg = Message
                .of(request)
                .addMetadata(metadata);
        return messageEmitter
                .sendMessage(msg)
                // pasar a static
                .map(unused -> Response.status(201).entity("ok").build())
                .onFailure()
                .retry()
                .withBackOff(Duration.ofSeconds(1))
                .atMost(10)
                .onFailure()
                .recoverWithItem(Response.serverError().build());
    }

}

/*
@Consumes(MediaType.APPLICATION_JSON)
@Path("/publish")
public class MessageResource {
    @Channel("message-t")
    Emitter<MessageEvent> messageEmitter;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response publish(@Valid MessageEvent request) {
        var metadata = OutgoingKafkaRecordMetadata.builder().withTopic(request.topic);
        var message = Message.of(request).addMetadata(metadata);
        messageEmitter.send(message);
        return Response.status(201).entity("ok").build();
    }

}
*/