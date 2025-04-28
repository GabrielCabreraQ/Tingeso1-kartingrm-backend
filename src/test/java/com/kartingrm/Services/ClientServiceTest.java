package com.kartingrm.Services;

import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientEntity client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
                client = new ClientEntity(1L, "12345678-9", "John Doe", null, "john.doe@example.com");
    }

    @Test
    void testGetClients() {
        List<ClientEntity> clients = new ArrayList<>(List.of(client)); // Crear un ArrayList explícitamente
        when(clientRepository.findAll()).thenReturn(clients);

        ArrayList<ClientEntity> result = clientService.getClients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void testGetClientById() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientEntity result = clientService.getClientById(1L);

        assertNotNull(result);
        assertEquals(client, result);
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetClientByRut() {
        when(clientRepository.findByRut("12345678-9")).thenReturn(Optional.of(client));

        Optional<ClientEntity> result = clientService.getClientByRut("12345678-9");

        assertTrue(result.isPresent());
        assertEquals(client, result.get());
        verify(clientRepository, times(1)).findByRut("12345678-9");
    }

    @Test
    void testSaveClient() {
        when(clientRepository.save(client)).thenReturn(client);

        ClientEntity result = clientService.saveClient(client);

        assertNotNull(result);
        assertEquals(client, result);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void testUpdateClient() {
        when(clientRepository.save(client)).thenReturn(client);

        ClientEntity result = clientService.updateClient(client);

        assertNotNull(result);
        assertEquals(client, result);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void testDeleteClient() throws Exception {
        doNothing().when(clientRepository).deleteById(1L);

        boolean result = clientService.deleteClient(1L);

        assertTrue(result);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteClientThrowsException() {
        doThrow(new RuntimeException("Error deleting client")).when(clientRepository).deleteById(1L);

        Exception exception = assertThrows(Exception.class, () -> clientService.deleteClient(1L));

        assertEquals("Error deleting client", exception.getMessage());
        verify(clientRepository, times(1)).deleteById(1L);
    }
}