package br.edu.unifacisa.adocao;

import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
class AdoptionService {
    private final Animals animals;
    private final Requests requests;
    AdoptionService(Animals animals,Requests requests) { this.animals=animals; this.requests=requests; }
    private ResponseStatusException error(HttpStatus status,String message) { return new ResponseStatusException(status,message); }
    private void role(AppUser user,Role role) { if(user.role!=role) throw error(HttpStatus.FORBIDDEN,"Esta ação não está disponível para seu perfil."); }
    private void owner(AppUser user,Animal animal) {
        role(user,Role.RESPONSAVEL);
        if(!animal.owner.id.equals(user.id)) throw error(HttpStatus.FORBIDDEN,"Você só pode gerenciar seus próprios animais.");
    }
    private Animal locked(Long id) { return animals.lockById(id).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Animal não encontrado.")); }
    @Transactional(readOnly=true)
    List<AnimalView> catalogue(String q,Species species,AnimalSize size) {
        String query=q==null?"":q.trim().toLowerCase(Locale.ROOT);
        return animals.findByStatusOrderByIdDesc(AnimalStatus.DISPONIVEL).stream()
          .filter(a->species==null||a.species==species).filter(a->size==null||a.size==size)
          .filter(a->(a.name+" "+a.city).toLowerCase(Locale.ROOT).contains(query)).map(AnimalView::of).toList();
    }
    @Transactional(readOnly=true) AnimalView detail(Long id) {
        return AnimalView.of(animals.findById(id).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Animal não encontrado.")));
    }
    @Transactional(readOnly=true) List<AnimalView> mine(AppUser user) {
        role(user,Role.RESPONSAVEL); return animals.findByOwnerIdOrderByIdDesc(user.id).stream().map(AnimalView::of).toList();
    }
    AnimalView save(AppUser user,Long id,AnimalInput input) {
        role(user,Role.RESPONSAVEL);
        Animal a;
        if(id==null) { a=new Animal(); a.owner=user; a.status=AnimalStatus.DISPONIVEL; }
        else { a=locked(id); owner(user,a); }
        a.name=input.name().trim(); a.species=input.species(); a.size=input.size(); a.ageMonths=input.ageMonths();
        a.city=input.city().trim(); a.description=input.description().trim(); a.care=input.care().trim();
        a.photoUrl=input.photoUrl().trim(); a.vaccinated=input.vaccinated(); a.neutered=input.neutered();
        return AnimalView.of(animals.save(a));
    }
    RequestView apply(AppUser user,Long animalId,RequestInput input) {
        role(user,Role.ADOTANTE); Animal a=locked(animalId);
        if(a.status!=AnimalStatus.DISPONIVEL) throw error(HttpStatus.CONFLICT,"Este animal não está disponível para novos pedidos.");
        if(requests.existsByAnimalIdAndAdopterIdAndStatusIn(a.id,user.id,List.of(RequestStatus.PENDENTE,RequestStatus.APROVADA,RequestStatus.CONCLUIDA)))
            throw error(HttpStatus.CONFLICT,"Você já tem uma solicitação ativa para este animal.");
        AdoptionRequest r=new AdoptionRequest(); r.animal=a; r.adopter=user;
        r.housing=input.housing().trim(); r.routine=input.routine().trim(); r.experience=input.experience().trim();
        r.commitment=input.commitment(); r.status=RequestStatus.PENDENTE; r.decisionNote=""; r.createdAt=Instant.now();
        return RequestView.of(requests.save(r));
    }
    @Transactional(readOnly=true) List<RequestView> inbox(AppUser user) {
        return (user.role==Role.RESPONSAVEL?requests.findByAnimalOwnerIdOrderByCreatedAtDesc(user.id):requests.findByAdopterIdOrderByCreatedAtDesc(user.id))
          .stream().map(RequestView::of).toList();
    }
    RequestView decide(AppUser user,Long id,DecisionInput input) {
        // Every transition locks the animal first, serializing decisions across its requests.
        AdoptionRequest initial=requests.findById(id).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Solicitação não encontrada."));
        Animal a=locked(initial.animal.id);
        // Both entities may have been loaded before a concurrent transaction completed.
        entityManager.refresh(a);
        entityManager.refresh(initial);
        AdoptionRequest r=initial;
        if(input.status()==RequestStatus.CANCELADA) {
            role(user,Role.ADOTANTE);
            if(!r.adopter.id.equals(user.id)) throw error(HttpStatus.FORBIDDEN,"Solicitação de outro adotante.");
            if(r.status!=RequestStatus.PENDENTE) throw error(HttpStatus.CONFLICT,"Somente pedidos pendentes podem ser cancelados aqui.");
        } else {
            owner(user,a);
            switch(input.status()) {
                case APROVADA -> {
                    if(r.status!=RequestStatus.PENDENTE||a.status!=AnimalStatus.DISPONIVEL)
                        throw error(HttpStatus.CONFLICT,"O animal ou o pedido não está disponível para aprovação.");
                    a.status=AnimalStatus.EM_PROCESSO;
                }
                case RECUSADA -> {
                    if(r.status!=RequestStatus.PENDENTE&&r.status!=RequestStatus.APROVADA)
                        throw error(HttpStatus.CONFLICT,"Este pedido já foi encerrado.");
                    if(r.status==RequestStatus.APROVADA) a.status=AnimalStatus.DISPONIVEL;
                }
                case CONCLUIDA -> {
                    if(r.status!=RequestStatus.APROVADA||a.status!=AnimalStatus.EM_PROCESSO)
                        throw error(HttpStatus.CONFLICT,"Apenas uma solicitação aprovada pode ser concluída.");
                    a.status=AnimalStatus.ADOTADO;
                    requests.findByAnimalId(a.id).stream().filter(other->!other.id.equals(r.id)&&other.status==RequestStatus.PENDENTE)
                      .forEach(other->{ other.status=RequestStatus.RECUSADA; other.decisionNote="Adoção concluída por outra solicitação."; });
                }
                default -> throw error(HttpStatus.BAD_REQUEST,"Transição de status inválida.");
            }
        }
        r.status=input.status(); r.decisionNote=input.note().trim(); return RequestView.of(r);
    }
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
}
