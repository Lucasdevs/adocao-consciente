package br.edu.unifacisa.adocao;

import com.fasterxml.jackson.databind.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.*;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password="})
@AutoConfigureMockMvc
class AdoptionFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Users users;
    @Autowired Animals animals;
    @Autowired Requests requests;
    @Autowired AdoptionService service;
    MockHttpSession owner,adopter;
    Long animal;

    @BeforeEach void setup() throws Exception {
        requests.deleteAll(); animals.deleteAll(); users.deleteAll();
        owner=register("owner@test.local","RESPONSAVEL");
        adopter=register("adopter@test.local","ADOTANTE");
        animal=create(owner);
    }
    MockHttpSession register(String email,String role) throws Exception {
        MvcResult r=mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
          .content(json.writeValueAsString(Map.of("name","Pessoa de teste","email",email,"password","SenhaTeste2026!","role",role))))
          .andExpect(status().isCreated()).andReturn();
        return (MockHttpSession)r.getRequest().getSession(false);
    }
    String animalBody() throws Exception { return json.writeValueAsString(Map.ofEntries(
      Map.entry("name","Bento"),Map.entry("species","CAO"),Map.entry("size","MEDIO"),Map.entry("ageMonths",12),
      Map.entry("city","Campina Grande / PB"),Map.entry("description","Animal fictício"),Map.entry("care","Passeios diários"),
      Map.entry("photoUrl",""),Map.entry("vaccinated",true),Map.entry("neutered",false))); }
    Long create(MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/animals").session(session).with(csrf()).contentType("application/json").content(animalBody()))
          .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    String questionnaire(boolean commitment) throws Exception {
        return json.writeValueAsString(Map.of("housing","Casa com portões","routine","Companhia diária","experience","Primeira adoção com preparação","commitment",commitment));
    }
    Long apply(MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(post("/api/animals/"+animal+"/requests").session(session).with(csrf()).contentType("application/json").content(questionnaire(true)))
          .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    ResultActions decide(MockHttpSession session,Long id,String state) throws Exception {
        return mvc.perform(patch("/api/requests/"+id).session(session).with(csrf()).contentType("application/json").content(json.writeValueAsString(Map.of("status",state,"note","Avaliação registrada"))));
    }
    @Test void publicCatalogueFiltersAndHidesPrivateData() throws Exception {
        mvc.perform(get("/api/animals").param("q","Campina").param("species","CAO")).andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].ownerName").exists())
          .andExpect(jsonPath("$[0].email").doesNotExist()).andExpect(jsonPath("$[0].owner.email").doesNotExist())
          .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mvc.perform(get("/api/animals").param("species","GATO")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/animals/99999999")).andExpect(status().isNotFound());
    }
    @Test void authenticationLogoutAndCsrfAreEnforced() throws Exception {
        mvc.perform(get("/api/requests")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/animals").session(owner).contentType("application/json").content(animalBody())).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(owner).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{\"email\":\"owner@test.local\",\"password\":\"wrong\"}"))
          .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{\"email\":\"OWNER@test.local\",\"password\":\"SenhaTeste2026!\"}"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("RESPONSAVEL"));
        assertThat(users.findByEmail("owner@test.local").orElseThrow().passwordHash).startsWith("$2").doesNotContain("SenhaTeste");
    }
    @Test void rolesAndOwnershipProtectWritesAndQuestionnaires() throws Exception {
        Long id=apply(adopter);
        MockHttpSession otherOwner=register("other-owner@test.local","RESPONSAVEL");
        mvc.perform(put("/api/animals/"+animal).session(otherOwner).with(csrf()).contentType("application/json").content(animalBody())).andExpect(status().isForbidden());
        mvc.perform(post("/api/animals").session(adopter).with(csrf()).contentType("application/json").content(animalBody())).andExpect(status().isForbidden());
        decide(otherOwner,id,"APROVADA").andExpect(status().isForbidden());
        decide(adopter,id,"APROVADA").andExpect(status().isForbidden());
        mvc.perform(get("/api/requests").session(otherOwner)).andExpect(jsonPath("$.length()").value(0));
        MockHttpSession otherAdopter=register("other-adopter@test.local","ADOTANTE");
        mvc.perform(get("/api/requests").session(otherAdopter)).andExpect(jsonPath("$.length()").value(0));
        decide(otherAdopter,id,"CANCELADA").andExpect(status().isForbidden());
    }
    @Test void duplicateAndIncompleteApplicationsAreRejected() throws Exception {
        mvc.perform(post("/api/animals/"+animal+"/requests").session(adopter).with(csrf()).contentType("application/json").content(questionnaire(false))).andExpect(status().isBadRequest());
        apply(adopter);
        mvc.perform(post("/api/animals/"+animal+"/requests").session(adopter).with(csrf()).contentType("application/json").content(questionnaire(true))).andExpect(status().isConflict());
        assertThat(requests.count()).isEqualTo(1);
    }
    @Test void completionClosesOtherPendingRequestsAndRemovesAnimalFromCatalogue() throws Exception {
        Long first=apply(adopter);
        MockHttpSession secondAdopter=register("second@test.local","ADOTANTE");
        Long second=apply(secondAdopter);
        decide(owner,first,"CONCLUIDA").andExpect(status().isConflict());
        decide(owner,first,"APROVADA").andExpect(status().isOk());
        decide(owner,second,"APROVADA").andExpect(status().isConflict());
        mvc.perform(get("/api/animals")).andExpect(jsonPath("$.length()").value(0));
        decide(owner,first,"CONCLUIDA").andExpect(status().isOk());
        assertThat(animals.findById(animal).orElseThrow().status).isEqualTo(AnimalStatus.ADOTADO);
        assertThat(requests.findById(second).orElseThrow().status).isEqualTo(RequestStatus.RECUSADA);
        decide(owner,first,"RECUSADA").andExpect(status().isConflict());
    }
    @Test void cancellationAndRefusalReopenOnlyValidStates() throws Exception {
        Long first=apply(adopter);
        decide(adopter,first,"CANCELADA").andExpect(status().isOk());
        decide(adopter,first,"CANCELADA").andExpect(status().isConflict());
        Long next=apply(adopter);
        decide(owner,next,"APROVADA").andExpect(status().isOk());
        decide(adopter,next,"CANCELADA").andExpect(status().isConflict());
        decide(owner,next,"RECUSADA").andExpect(status().isOk());
        assertThat(animals.findById(animal).orElseThrow().status).isEqualTo(AnimalStatus.DISPONIVEL);
        apply(adopter);
    }
    @Test void registrationRejectsDuplicateEmailAndWeakPassword() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{\"name\":\"Outro\",\"email\":\"OWNER@test.local\",\"password\":\"SenhaTeste2026!\",\"role\":\"ADOTANTE\"}"))
          .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{\"name\":\"Outro\",\"email\":\"new@test.local\",\"password\":\"123\",\"role\":\"ADOTANTE\"}"))
          .andExpect(status().isBadRequest());
    }
    @Test void concurrentApprovalsAllowOnlyOneWinner() throws Exception {
        Long first=apply(adopter);
        Long second=apply(register("second@test.local","ADOTANTE"));
        AppUser responsible=users.findByEmail("owner@test.local").orElseThrow();
        CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> futures=new ArrayList<>();
            for(Long id:List.of(first,second)) futures.add(pool.submit(()->{
                ready.countDown(); start.await();
                try { service.decide(responsible,id,new DecisionInput(RequestStatus.APROVADA,"")); return true; }
                catch(ResponseStatusException e) { if(e.getStatusCode().value()!=409) throw e; return false; }
            }));
            assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue(); start.countDown();
            int successes=0; for(Future<Boolean> future:futures) if(future.get(15,TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
        }
        assertThat(requests.findByAnimalId(animal).stream().filter(r->r.status==RequestStatus.APROVADA).count()).isEqualTo(1);
    }
}
