package com.example.demo.service;

import com.example.demo.domain.Favorito;
import com.example.demo.domain.Lista;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.ContextoSeguridad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ContextoSeguridad contexto;

    @InjectMocks
    private ListaServiceImpl service;

    private static final Long ANA = 1L;
    private static final Long BOB = 2L;

    @Test
    void crear_asignaUsuarioAutenticadoComoDueno() {
        when(contexto.usuarioActualId()).thenReturn(ANA);
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocation -> {
            Lista l = invocation.getArgument(0);
            return new Lista(10L, l.nombre(), l.usuarioId());
        });

        ListaResponse response = service.crear(new ListaRequest("Regalos"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());
        assertThat(captor.getValue().usuarioId()).isEqualTo(ANA);
        assertThat(response.usuarioId()).isEqualTo(ANA);
    }

    @Test
    void obtener_listaPropia_devuelveLaLista() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA)));
        when(contexto.usuarioActualId()).thenReturn(ANA);

        ListaResponse response = service.obtener(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.nombre()).isEqualTo("De Ana");
        assertThat(response.usuarioId()).isEqualTo(ANA);
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA)));
        when(contexto.usuarioActualId()).thenReturn(BOB);
        when(contexto.esAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.obtener(7L))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("no te pertenece");
    }

    @Test
    void obtener_listaAjena_admin_devuelveLaLista() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA)));
        when(contexto.usuarioActualId()).thenReturn(BOB);
        when(contexto.esAdmin()).thenReturn(true);

        ListaResponse response = service.obtener(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.usuarioId()).isEqualTo(ANA);
    }

    @Test
    void listar_usuarioComun_devuelveSoloListasPropias() {
        when(contexto.esAdmin()).thenReturn(false);
        when(contexto.usuarioActualId()).thenReturn(ANA);
        when(listaRepository.findByUsuarioId(ANA)).thenReturn(List.of(
                new Lista(1L, "Lista 1 de Ana", ANA),
                new Lista(2L, "Lista 2 de Ana", ANA)
        ));

        List<ListaResponse> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(l -> l.usuarioId().equals(ANA));
    }

    @Test
    void listar_admin_devuelveTodasLasListas() {
        when(contexto.esAdmin()).thenReturn(true);
        when(listaRepository.findAll()).thenReturn(List.of(
                new Lista(1L, "De Ana", ANA),
                new Lista(2L, "De Bob", BOB)
        ));

        List<ListaResponse> resultado = service.listar();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void eliminar_listaConFavoritos_lanzaListaNoVaciaException() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA)));
        when(contexto.usuarioActualId()).thenReturn(ANA);
        when(favoritoRepository.findByListaId(7L)).thenReturn(List.of(
                new Favorito(1L, 100L, "Nota", LocalDateTime.now(), 7L)
        ));

        assertThatThrownBy(() -> service.eliminar(7L))
                .isInstanceOf(ListaNoVaciaException.class)
                .hasMessageContaining("todavía tiene favoritos");
    }
}
