import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;

public class TestesLoginEUsuarios {

    private String userId;

    @BeforeAll
    public static void setup(){
        baseURI = "https://serverest.dev";
    }

    @Test
    public void RealizarLoginComSucesso(){

        given()
                .contentType("application/json")
                .body("{\"email\": \"fulano@qa.com\", \"password\": \"teste\"}")
            .when()
                .post("/login")
            .then()
                .log().all()
                .statusCode(200)
                .body("message", is("Login realizado com sucesso"))
                .body("authorization", is(notNullValue()));
    }

    @Test
    public void RealizarLoginSemSucesso(){

        given()
                .contentType("application/json")
                .body("{\"email\": \"fulano@qa.com\", \"password\": \"test\"}")
            .when()
                .post("/login")
            .then()
                .log().all()
                .statusCode(401)
                .body("message", is("Email e/ou senha inválidos"));
    }

    @Test
    public void ListarTodosOsUsuariosCadastrados(){

        given()
            .when()
                .get("/usuarios")
            .then()
                .log().all()
                .statusCode(200)
                .body("usuarios", is(notNullValue()))
                .body("usuarios.size()", is(greaterThan(0)));
    }

    @Test
    public void CadastroDeUsuarioComSucesso(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Felipe");
        body.put("email", "felipe@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");

         userId = given()
                .contentType("application/json")
                .header("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImZ1bGFub0BxYS5jb20iLCJwYXNzd29yZCI6InRlc3RlIiwiaWF0IjoxNzYwNzM3MTM0LCJleHAiOjE3NjA3Mzc3MzR9.iIF25I6AzFCx7yj4vJ3BXDtiKX0rx5Zpiyfwa66Xv0M")
                .body(body)
            .when()
                .post("/usuarios")
            .then()
                .log().all()
                .statusCode(201)
                .body("message", is("Cadastro realizado com sucesso"))
                .body("_id", is(notNullValue()))
                .extract()
                .path("_id");
    }
    @AfterEach
    void tearDown(){
        if (userId != null) {
            given()
                .when()
                    .delete("/usuarios/" + userId)
                .then()
                    .statusCode(200);
        }
    }

    @Test
    public void tentarCadastrarUmUsuarioJaCadastrado(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Fulano da Silva");
        body.put("email", "beltrano@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");


        given()
                .contentType("application/json")
                .header("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImZ1bGFub0BxYS5jb20iLCJwYXNzd29yZCI6InRlc3RlIiwiaWF0IjoxNzYwNzM3MTM0LCJleHAiOjE3NjA3Mzc3MzR9.iIF25I6AzFCx7yj4vJ3BXDtiKX0rx5Zpiyfwa66Xv0M")
                .body(body)
                .when()
                .post("/usuarios")
                .then()
                .log().all()
                .statusCode(400)
                .body("message", is("Este email já está sendo usado"));
    }

    @Test
    public void BuscarUsuarioPorIdComSucesso(){

        String id = "0uxuPY0cbmQhpEz1";

        given()
            .when()
                .get("/usuarios/" + id)
            .then()
                .log().all()
                .statusCode(200)
                .body("nome", is("Fulano da Silva"),
                "email", is("beltranoo@qa.com.br"),
                        "password", is("teste"),
                        "administrador", is("true"),
                        "_id", is("0uxuPY0cbmQhpEz1"));
    }

    @Test
    public void BuscarUsuarioPorIdSemSucesso() {

        String id = "0uxuPY0cbmQhpEz2";

        given()
            .when()
                .get("/usuarios/" + id)
            .then()
                .statusCode(400)
                .body("message", is("Usuário não encontrado"));
    }

    @Test
    public void DeletarUmUsuarioComCarrinhoCadastrado(){

        String id = "0uxuPY0cbmQhpEz1";

        given()
            .when()
                .delete("/usuarios/" + id)
            .then()
                .log().all()
                .statusCode(400)
                .body("message", is("Não é permitido excluir usuário com carrinho cadastrado"),
                        "idCarrinho", is(notNullValue()));
    }

    @Test
    public void DeletarUmUsuarioPorIdcomSucesso(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Felipe");
        body.put("email", "felipe@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");

        String usuarioId =
                given()
                        .contentType("application/json")
                        .body(body)
                    .when()
                        .post("/usuarios")
                    .then()
                        .statusCode(201)
                        .extract()
                        .path("_id");

        given()
            .when()
                .delete("/usuarios/" + usuarioId)
            .then()
                .log().all()
                .statusCode(200)
                .body("message", is("Registro excluído com sucesso"));

    }

    @Test
    public void atualizarDadosDoUsuario(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Fulano da Silva");
        body.put("email", "beltranoo@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");

        String id = "0uxuPY0cbmQhpEz1";

        given()
                .contentType(ContentType.JSON)
                .body(body)
            .when()
                .put("/usuarios/" + id)
            .then()
                .log().all()
                .statusCode(200)
                .body("message", is("Registro alterado com sucesso"));
    }

    @Test
    public void CadastrarUsuarioComPut(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Felipe");
        body.put("email", "felipe@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");

        String id = "jogfODIlXsqxNFS2";

        userId = given()
                .contentType("application/json")
                .header("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImZ1bGFub0BxYS5jb20iLCJwYXNzd29yZCI6InRlc3RlIiwiaWF0IjoxNzYwNzM3MTM0LCJleHAiOjE3NjA3Mzc3MzR9.iIF25I6AzFCx7yj4vJ3BXDtiKX0rx5Zpiyfwa66Xv0M")
                .body(body)
            .when()
                .put("/usuarios/" + id)
            .then()
                .log().all()
                .statusCode(201)
                .body("message", is("Cadastro realizado com sucesso"))
                .body("_id", is(notNullValue()))
                .extract()
                .path("_id");
    }
    @AfterEach
    void deleteput(){
        if (userId != null) {
            given()
                    .when()
                    .delete("/usuarios/" + userId)
                    .then()
                    .statusCode(200);
        }
    }

    @Test
    public void cadastrarComPutUmUsuarioJaExistente(){

        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Fulano da Silva");
        body.put("email", "beltrano@qa.com.br");
        body.put("password", "teste");
        body.put("administrador", "true");

        String id = "0uxuPY0cbmQhpEz1";

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/usuarios/" + id)
                .then()
                .log().all()
                .statusCode(400)
                .body("message", is("Este email já está sendo usado"));
    }
}



