package com.tuankhai.travelassistants.webservice.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuankhai.travelassistants.webservice.main.RequestService;

/**
 * Created by tuank on 27/10/2017.
 */

public final class DeleteScheduleDTO {
    public final String status;
    public final String[] result;
    public final String message;

    @JsonCreator
    public DeleteScheduleDTO(
            @JsonProperty("status") String status,
            @JsonProperty("result") String result[],
            @JsonProperty("message") String message) {
        this.status = status;
        this.result = result;
        this.message = message;
    }

    public boolean isSuccess() {
        if (status.equals(RequestService.RESULT_OK)) {
            return true;
        }
        return false;
    }
}