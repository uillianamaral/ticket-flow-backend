package com.ticketflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketflowApplicationUnitTest {

    @Test
    @DisplayName("Garante que a classe de inicialização da aplicação pode ser instanciada")
    void applicationClassInstantiationTest() {
        TicketflowApplication app = new TicketflowApplication();
        assertThat(app).isNotNull();
    }
}
