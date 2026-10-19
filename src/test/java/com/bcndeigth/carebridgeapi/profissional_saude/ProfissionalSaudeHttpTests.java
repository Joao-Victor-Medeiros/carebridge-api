package com.bcndeigth.carebridgeapi.profissional_saude;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfissionalSaudeHttpTests {

    @Value("${local.server.port}")
    private int port;

    @Test
    void deveRetornar400QuandoParteDadosEstaAusente() throws Exception {
        var response = enviarFormulario("campo_incorreto", "text/plain", "teste");

        assertEquals(400, response.statusCode());
    }

    @Test
    void deveRetornar415QuandoDadosSaoEnviadosComoTexto() throws Exception {
        var response = enviarFormulario("dados", "text/plain", "{}");

        assertEquals(415, response.statusCode());
    }

    @Test
    void deveManterRotasPrivadasProtegidas() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/rota-privada"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try (var client = HttpClient.newHttpClient()) {
            assertEquals(401, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }

    private HttpResponse<String> enviarFormulario(String nome, String contentType, String valor) throws Exception {
        var boundary = "carebridge-test-boundary";
        var body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + nome + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n"
                + valor + "\r\n--" + boundary + "--\r\n";
        var request = HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + port + "/profissional-saude/cadastro"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try (var client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}