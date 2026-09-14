# Registro de validação técnica

Data: 14/09/2026.

## Verificações executadas

- `scripts/check.ps1`: execução concluída com sucesso após os últimos ajustes.
- Backend: 8 testes de integração, 0 falhas, 0 erros e 0 testes ignorados.
- Frontend: build Angular de produção concluído; pacote inicial de aproximadamente 235 kB antes da compressão de transferência.
- HTTP pelo endereço do frontend: página inicial retornou 200; catálogo retornou os 2 animais fictícios; login com token CSRF estabeleceu sessão de adotante; consulta da sessão autenticada funcionou; logout retornou 204.
- Fotografias locais de demonstração inspecionadas após o download.

## Cobertura dos testes de integração

1. Catálogo público, filtros, animal inexistente e ausência de dados privados na resposta pública.
2. Autenticação, logout, senha incorreta, hash da senha e rejeição de escrita sem CSRF.
3. Perfis, propriedade dos animais e isolamento dos questionários.
4. Compromisso obrigatório e rejeição de solicitações duplicadas.
5. Aprovação, conclusão, encerramento dos outros pedidos e retirada do catálogo.
6. Cancelamento, recusa e reabertura somente em estados permitidos.
7. E-mail duplicado e senha curta no cadastro.
8. Duas aprovações concorrentes para um mesmo animal, com apenas uma vencedora.

## Limites da evidência

Os testes usaram H2 em modo PostgreSQL, não uma instância PostgreSQL real. Não houve teste automatizado de interação no navegador nem avaliação de usabilidade com participantes. Compilação e respostas HTTP não comprovam acessibilidade visual ou facilidade de uso. Esses resultados são evidência técnica inicial, não comprovação de redução do abandono ou de impacto social.
