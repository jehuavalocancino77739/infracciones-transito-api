package edu.pe.cibertec.infracciones.service;

import edu.pe.cibertec.infracciones.exception.InfractorBloqueadoException;
import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.Vehiculo;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.service.impl.MultaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultaServiceImplTest {

    @InjectMocks
    private MultaServiceImpl multaService;

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private InfractorRepository infractorRepository;

    private Multa multaPendiente;
    private Infractor infractorA;
    private Infractor infractorB;
    private Vehiculo vehiculo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vehiculo = new Vehiculo();
        vehiculo.setId(100L);
        vehiculo.setPlaca("ABC-123");

        infractorA = new Infractor();
        infractorA.setId(1L);
        infractorA.setNombre("Juan");
        infractorA.setApellido("Perez");
        infractorA.setBloqueado(false);
        infractorA.setVehiculos(Collections.singletonList(vehiculo));

        infractorB = new Infractor();
        infractorB.setId(2L);
        infractorB.setNombre("Maria");
        infractorB.setApellido("Gomez");
        infractorB.setBloqueado(false);
        infractorB.setVehiculos(Collections.singletonList(vehiculo));

        multaPendiente = new Multa();
        multaPendiente.setId(1L);
        multaPendiente.setEstado(EstadoMulta.PENDIENTE);
        multaPendiente.setInfractor(infractorA);
        multaPendiente.setVehiculo(vehiculo);
    }

    @Test
    void testTransferirMulta_Satisfactorio() {
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multaPendiente));
        when(infractorRepository.findById(2L)).thenReturn(Optional.of(infractorB));
        when(multaRepository.save(any(Multa.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> multaService.transferirMulta(1L, 2L));


        ArgumentCaptor<Multa> multaCaptor = ArgumentCaptor.forClass(Multa.class);
        verify(multaRepository, times(1)).save(multaCaptor.capture());

        Multa multaGuardada = multaCaptor.getValue();
        assertEquals(infractorB, multaGuardada.getInfractor());
    }

    @Test
    void testTransferirMulta_MultaNoPendiente() {
        multaPendiente.setEstado(EstadoMulta.PAGADA);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multaPendiente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> multaService.transferirMulta(1L, 2L));

        assertEquals("Solo se pueden transferir multas pendientes", ex.getMessage());
    }

    @Test
    void testTransferirMulta_VehiculoDiferente() {
        Vehiculo otroVehiculo = new Vehiculo();
        otroVehiculo.setId(999L);
        infractorB.setVehiculos(Collections.singletonList(otroVehiculo));

        when(multaRepository.findById(1L)).thenReturn(Optional.of(multaPendiente));
        when(infractorRepository.findById(2L)).thenReturn(Optional.of(infractorB));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> multaService.transferirMulta(1L, 2L));

        assertEquals("El nuevo infractor no tiene asignado el vehículo de la multa", ex.getMessage());
    }

    @Test
    void testTransferirMulta_InfractorNoExiste() {
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multaPendiente));
        when(infractorRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> multaService.transferirMulta(1L, 99L));

        assertEquals("Infractor no encontrado con id: 99", ex.getMessage());
    }

    @Test
    void testTransferirMulta_aInfractorBloqueado_Avanzado() {
        Multa multa1 = new Multa();
        multa1.setId(1L);
        multa1.setEstado(EstadoMulta.PENDIENTE);
        multa1.setVehiculo(vehiculo);
        multa1.setInfractor(infractorA);

        Infractor infractorBloqueado = new Infractor();
        infractorBloqueado.setId(2L);
        infractorBloqueado.setNombre("Juan Perez");
        infractorBloqueado.setBloqueado(true);

        when(infractorRepository.findById(2L)).thenReturn(Optional.of(infractorBloqueado));
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa1));

        assertThrows(InfractorBloqueadoException.class, () -> {
            multaService.transferirMulta(1L, 2L);
        });


        verify(multaRepository, never()).save(any(Multa.class));
    }
}