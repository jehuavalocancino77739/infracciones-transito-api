package edu.pe.cibertec.infracciones;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class InfractorServiceImplTest {

    @Mock
    private MultaRepository multaRepository;

    @InjectMocks
    private InfractorServiceImpl infractorService;

    public InfractorServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void calcularDeuda_debeRetornar545_cuandoTienePendienteYVencida() {

        Long infractorId = 1L;

        Multa pendiente = new Multa();
        pendiente.setMonto(200.00);
        pendiente.setEstado(EstadoMulta.PENDIENTE);

        Multa vencida = new Multa();
        vencida.setMonto(300.00);
        vencida.setEstado(EstadoMulta.VENCIDA);

        when(multaRepository.findByInfractor_Id(infractorId))
                .thenReturn(List.of(pendiente, vencida));

        Double deuda = infractorService.calcularDeuda(infractorId);

        assertEquals(545.00, deuda);
    }
}