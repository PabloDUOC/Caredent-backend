package cl.caredent.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de integracion: arranca toda la aplicacion con H2 y llama a la API como lo haria el frontend. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private JsonNode login(String usuario, String clave) throws Exception {
        String body = "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}";
        String res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    private String bearer(String usuario, String clave) throws Exception {
        return "Bearer " + login(usuario, clave).get("accessToken").asText();
    }

    @Test
    void loginCorrectoDevuelveTokensYRol() throws Exception {
        JsonNode r = login("profesional", "profesional123");
        org.junit.jupiter.api.Assertions.assertEquals("profesional", r.get("user").get("rol").asText());
        org.junit.jupiter.api.Assertions.assertFalse(r.get("refreshToken").asText().isBlank());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenLosEndpointsProtegidosDevuelven401() throws Exception {
        mvc.perform(get("/api/pacientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInventadoDevuelve401() throws Exception {
        mvc.perform(get("/api/pacientes").header("Authorization", "Bearer abc.def.ghi")).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenNoSirveComoAccessToken() throws Exception {
        String refresh = login("profesional", "profesional123").get("refreshToken").asText();
        mvc.perform(get("/api/pacientes").header("Authorization", "Bearer " + refresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEntregaNuevoAccessToken() throws Exception {
        String refresh = login("admin", "admin123").get("refreshToken").asText();
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void recepcionistaNoPuedeVerPacientesPeroSiElResumen() throws Exception {
        String t = bearer("recepcion", "recepcion123");
        mvc.perform(get("/api/pacientes").header("Authorization", t)).andExpect(status().isForbidden());
        mvc.perform(get("/api/pacientes/resumen").header("Authorization", t)).andExpect(status().isOk());
    }

    @Test
    void profesionalNoPuedeAdministrarUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", bearer("profesional", "profesional123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void profesionalListaPacientesDeLaBaseDeDatos() throws Exception {
        mvc.perform(get("/api/pacientes").header("Authorization", bearer("profesional", "profesional123")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].antecedentes.alergias").exists());
    }

    @Test
    void crudDePacienteConValidacionDeRut() throws Exception {
        String t = bearer("profesional", "profesional123");
        String malRut = "{\"nombre\":\"Ana\",\"apellidos\":\"Gil\",\"rut\":\"11.111.111-2\",\"fechaNacimiento\":\"1990-01-01\","
                + "\"telefono\":\"+56 9 1111 2222\",\"correo\":\"ana@correo.cl\"}";
        mvc.perform(post("/api/pacientes").header("Authorization", t).contentType(MediaType.APPLICATION_JSON).content(malRut))
                .andExpect(status().isBadRequest());

        String ok = malRut.replace("11.111.111-2", "11.111.111-1");
        String creado = mvc.perform(post("/api/pacientes").header("Authorization", t).contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("pendiente"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(creado).get("id").asLong();

        mvc.perform(post("/api/pacientes").header("Authorization", t).contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/pacientes/" + id + "/control").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2030-01-10\",\"motivo\":\"Control\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/pacientes/" + id).header("Authorization", t))
                .andExpect(status().isOk()).andExpect(jsonPath("$.proximoControl.motivo").value("Control"));

        mvc.perform(delete("/api/pacientes/" + id).header("Authorization", t)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pacientes/" + id).header("Authorization", t)).andExpect(status().isNotFound());
    }

    @Test
    void adminCreaUsuarioYNoPuedeEliminarAdministradores() throws Exception {
        String t = bearer("admin", "admin123");
        mvc.perform(post("/api/usuarios").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Dr. Test\",\"usuario\":\"drtest\",\"clave\":\"clave123\",\"rol\":\"profesional\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.clave").doesNotExist());
        mvc.perform(delete("/api/usuarios/1").header("Authorization", t)).andExpect(status().isForbidden());
    }

    @Test
    void recepcionistaAgendaHoraYSeRechazaElChoqueDeHorario() throws Exception {
        String t = bearer("recepcion", "recepcion123");
        String body = "{\"pacienteId\":1,\"profesionalId\":3,\"fecha\":\"2030-05-05\",\"hora\":\"10:00\",\"motivo\":\"Control\"}";
        mvc.perform(post("/api/horas").header("Authorization", t).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/horas").header("Authorization", t).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void swaggerEstaDisponibleSinToken() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
