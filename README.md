# Adoção Consciente

MVP acadêmico com Angular, Java/Spring Boot e persistência em banco. O fluxo conecta responsáveis e adotantes, da publicação do animal à confirmação da adoção.

Veja o escopo e os critérios de aceite em [docs/MVP.md](docs/MVP.md).

## Executar neste Windows

As ferramentas portáteis usadas neste ambiente ficam em `.tools` e não alteram as instalações do Windows. Abra dois terminais PowerShell na pasta do projeto.

Terminal 1 — backend:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1
```

Terminal 2 — frontend:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-frontend.ps1
```

Abra **http://127.0.0.1:4200**. Para parar, pressione `Ctrl+C` nos dois terminais.

O modo padrão usa H2 persistente em `backend/data` e o perfil `demo`. As contas e animais de exemplo são criados somente se o banco não tiver usuários. Contas de demonstração:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Responsável | responsavel@demo.local | AdocaoDemo2026! |
| Adotante | adotante@demo.local | AdocaoDemo2026! |

Também é possível criar contas pela interface. As senhas de exemplo são apenas para demonstração local com dados fictícios. Para iniciar sem criar exemplos, use `start-backend.ps1 -WithoutDemo` em um banco novo. Isso não apaga exemplos que já existem.

## Em outra máquina

Instale Java 21, Maven 3.9+ e Node.js 22.12+ e execute `npm ci` em `frontend`. Os scripts Windows reconhecem essas ferramentas no PATH. Alternativa manual:

```text
backend:  mvn spring-boot:run -Dspring-boot.run.profiles=local,demo
frontend: npm start
```

Os metadados de compatibilidade estão nas documentações oficiais de [Angular](https://angular.dev/reference/versions) e [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

## PostgreSQL

1. Instale Docker com Compose, ou use um PostgreSQL existente.
2. Copie `.env.example` para `.env` e altere a senha.
3. Execute `docker compose up -d db` na raiz.
4. No terminal do backend, defina `$env:DB_PASSWORD = 'a-mesma-senha-do-env'` e execute `powershell -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1 -Postgres`.
5. Inicie o frontend normalmente.

O perfil padrão usa PostgreSQL e aplica as migrações Flyway. `DB_URL`, `DB_USER` e `DB_PASSWORD` podem apontar para outra instância. Não há dados de demonstração no perfil PostgreSQL por padrão. O arquivo `.env` é lido pelo Docker Compose; o backend recebe variáveis do processo, não lê esse arquivo automaticamente.

## Verificar

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
```

Os testes cobrem autenticação, proteção CSRF, restrições de perfil e propriedade, validação, duplicidade, cancelamento, reabertura, conclusão e aprovações concorrentes. Eles usam H2 em modo PostgreSQL. A execução em PostgreSQL deve ser validada no ambiente de destino.

## Arquitetura e segurança

- API REST: `backend/src/main/java/br/edu/unifacisa/adocao`.
- Angular standalone: `frontend/src`.
- Esquema versionado: `backend/src/main/resources/db/migration`.
- Sessão HTTP com cookie HttpOnly, SameSite Strict e expiração em 60 minutos; senhas BCrypt; CSRF nas alterações.
- O Angular usa `/api` pelo proxy local, sem armazenar credenciais no navegador.
- Os questionários e contatos só são retornados para seus adotantes e responsáveis.
- O bloqueio transacional do animal serializa decisões concorrentes.

## Limites desta versão

- Fotografias por link HTTPS, sem upload. As duas imagens locais são exemplos licenciados.
- Contato externo por e-mail; não há chat, envio de e-mail automático ou recuperação de senha.
- Perfis de responsáveis autodeclarados, sem verificação documental.
- Catálogo carregado em memória para filtragem, adequado ao recorte pequeno de demonstração; paginação e busca no banco ficam para crescimento do volume.
- Servidores vinculados a `127.0.0.1`. Não há publicação online incluída.
- Antes de uso público: configurar HTTPS/cookie Secure, política de acesso e retenção de dados, recuperação de conta, verificação de e-mail, limitação de tentativas, backup e validação de infraestrutura. Esta entrega serve à implementação e validação acadêmica local.

## Roteiro da demonstração

1. Entre como responsável e cadastre um animal.
2. Saia e entre como adotante (ou crie uma conta).
3. Busque o animal, leia o perfil e envie o questionário.
4. Confira o pedido em “Minhas solicitações”.
5. Volte à conta responsável, leia as respostas e aprove para avaliação.
6. Simule o encontro fora da plataforma; confirme a adoção somente após essa etapa.
7. Confira o status concluído e a saída do animal do catálogo.

Use dados fictícios. Não invente resultados de usabilidade: registre as evidências obtidas com os participantes.

## Créditos

Imagens de demonstração sob a [licença Pexels](https://www.pexels.com/license/): [Cute dog on leash — Ja Kubislav](https://www.pexels.com/photo/cute-dog-on-leash-14585236/) e [Cute little cat — KATRIN BOLOVTSOVA](https://www.pexels.com/photo/cute-little-cat-5263848/). A interface aplica recorte visual com `object-fit: cover`. Não representam animais realmente disponíveis para adoção.
