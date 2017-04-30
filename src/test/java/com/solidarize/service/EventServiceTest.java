package com.solidarize.service;

import com.solidarize.model.Event;
import com.solidarize.repository.EventRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    private EventRepository repository;
    private EventService service;

    @Before
    public void setUp() throws Exception {
        service = new EventService(repository);
    }

    @Test
    public void shouldBeAbleToGetEvent() throws Exception {
        int id = 1;
        Event event = new Event("name", "owner");
        when(repository.findEventById(eq(id))).thenReturn(event);
        Event response = service.getEventById(id);
        assertEquals(event, response);
    }

    @Test
    public void shouldBeAbleToSaveAnEvent() throws Exception {
        Event event = new Event("name", "owner");
        when(repository.save(eq(event))).thenReturn(event);
        Event response = service.createEvent(event);
        assertEquals(event, response);
    }
}