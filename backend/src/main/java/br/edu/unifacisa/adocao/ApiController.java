package br.edu.unifacisa.adocao;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
class ApiController {
    private final AdoptionService service;
    private final AuthController auth;
    ApiController(AdoptionService service,AuthController auth) { this.service=service; this.auth=auth; }
    @GetMapping("/animals") List<AnimalView> animals(@RequestParam(required=false) String q,
      @RequestParam(required=false) Species species,@RequestParam(required=false) AnimalSize size) { return service.catalogue(q,species,size); }
    @GetMapping("/animals/{id}") AnimalView animal(@PathVariable Long id) { return service.detail(id); }
    @GetMapping("/my/animals") List<AnimalView> mine(Authentication a) { return service.mine(auth.current(a)); }
    @PostMapping("/animals") @ResponseStatus(HttpStatus.CREATED)
    AnimalView create(Authentication a,@Valid @RequestBody AnimalInput input) { return service.save(auth.current(a),null,input); }
    @PutMapping("/animals/{id}") AnimalView edit(Authentication a,@PathVariable Long id,@Valid @RequestBody AnimalInput input) { return service.save(auth.current(a),id,input); }
    @PostMapping("/animals/{id}/requests") @ResponseStatus(HttpStatus.CREATED)
    RequestView apply(Authentication a,@PathVariable Long id,@Valid @RequestBody RequestInput input) { return service.apply(auth.current(a),id,input); }
    @GetMapping("/requests") List<RequestView> requests(Authentication a) { return service.inbox(auth.current(a)); }
    @PatchMapping("/requests/{id}") RequestView decision(Authentication a,@PathVariable Long id,@Valid @RequestBody DecisionInput input) { return service.decide(auth.current(a),id,input); }
}
