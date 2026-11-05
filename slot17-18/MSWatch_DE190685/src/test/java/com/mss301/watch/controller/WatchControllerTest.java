package com.mss301.watch.controller;

import com.mss301.watch.service.WatchService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class WatchControllerTest {

    @Test
    void rejectsRequestsWithoutAdministratorRole() {
        WatchController controller = new WatchController(mock(WatchService.class));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> controller.delete(null, null, 1));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void acceptsAdministratorRole() {
        WatchController.checkAdminRole("1");
    }
}
