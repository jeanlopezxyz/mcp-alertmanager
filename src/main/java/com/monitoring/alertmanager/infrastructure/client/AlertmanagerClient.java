package com.monitoring.alertmanager.infrastructure.client;

import com.monitoring.alertmanager.infrastructure.dto.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/api/v2")
@RegisterRestClient(configKey = "alertmanager-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AlertmanagerClient {

    /**
     * Get all alerts.
     */
    @GET
    @Path("/alerts")
    List<AlertDto> getAlerts(
        @QueryParam("active") Boolean active,
        @QueryParam("silenced") Boolean silenced,
        @QueryParam("inhibited") Boolean inhibited,
        @QueryParam("unprocessed") Boolean unprocessed,
        @QueryParam("filter") List<String> filter,
        @QueryParam("receiver") String receiver
    );

    /**
     * Get alert groups.
     */
    @GET
    @Path("/alerts/groups")
    List<AlertGroupDto> getAlertGroups(
        @QueryParam("active") Boolean active,
        @QueryParam("silenced") Boolean silenced,
        @QueryParam("inhibited") Boolean inhibited,
        @QueryParam("filter") List<String> filter,
        @QueryParam("receiver") String receiver
    );

    /**
     * Get all silences.
     */
    @GET
    @Path("/silences")
    List<SilenceDto> getSilences(@QueryParam("filter") List<String> filter);

    /**
     * Get a specific silence by ID.
     */
    @GET
    @Path("/silence/{silenceId}")
    SilenceDto getSilence(@PathParam("silenceId") String silenceId);

    /**
     * Create a new silence.
     */
    @POST
    @Path("/silences")
    SilenceDto createSilence(CreateSilenceDto silence);

    /**
     * Delete a silence.
     */
    @DELETE
    @Path("/silence/{silenceId}")
    void deleteSilence(@PathParam("silenceId") String silenceId);

    /**
     * Get Alertmanager status.
     */
    @GET
    @Path("/status")
    Object getStatus();

    /**
     * Get receivers.
     */
    @GET
    @Path("/receivers")
    List<Object> getReceivers();
}
