package br.edu.unifacisa.adocao;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile("demo")
class DemoData implements CommandLineRunner {
    private final Users users; private final Animals animals; private final PasswordEncoder encoder;
    DemoData(Users users,Animals animals,PasswordEncoder encoder) { this.users=users; this.animals=animals; this.encoder=encoder; }
    @Override @Transactional public void run(String... args) {
        if(users.count()!=0) return;
        AppUser owner=user("Lar Amigo (demonstração)","responsavel@demo.local",Role.RESPONSAVEL);
        user("Adotante de demonstração","adotante@demo.local",Role.ADOTANTE);
        Animal dog=new Animal(); dog.owner=owner; dog.name="Bento"; dog.species=Species.CAO; dog.size=AnimalSize.GRANDE;
        dog.ageMonths=24; dog.city="Campina Grande / PB"; dog.description="Um companheiro curioso e cheio de energia. Gosta de companhia e de explorar os passeios com calma. Perfil fictício para demonstração.";
        dog.care="Precisa de passeios diários e de um ambiente seguro. Converse sobre a adaptação à rotina da família.";
        dog.photoUrl="/images/dog.jpg"; dog.vaccinated=true; dog.neutered=true; dog.status=AnimalStatus.DISPONIVEL; animals.save(dog);
        Animal cat=new Animal(); cat.owner=owner; cat.name="Amora"; cat.species=Species.GATO; cat.size=AnimalSize.PEQUENO;
        cat.ageMonths=18; cat.city="Campina Grande / PB"; cat.description="Observadora e tranquila, gosta de um cantinho ensolarado e de carinho no seu tempo. Perfil fictício para demonstração.";
        cat.care="Adoção para ambiente protegido, com telas nas janelas e sem acesso à rua. Apresentação gradual a outros animais.";
        cat.photoUrl="/images/cat.jpg"; cat.vaccinated=true; cat.neutered=true; cat.status=AnimalStatus.DISPONIVEL; animals.save(cat);
    }
    private AppUser user(String name,String email,Role role) {
        AppUser u=new AppUser(); u.name=name; u.email=email; u.role=role; u.passwordHash=encoder.encode("AdocaoDemo2026!"); return users.save(u);
    }
}
