# MVP — Adoção Consciente

## Objetivo
Permitir que um responsável publique animais e analise solicitações de adoção, enquanto o adotante encontra animais, conhece suas necessidades e solicita uma adoção de maneira consciente. O produto apoia a decisão humana; uma solicitação não garante a adoção.

## Público e recorte
- Visitante: consulta animais disponíveis e orientações.
- Adotante: cria conta, envia questionário e acompanha ou cancela seus pedidos pendentes.
- Responsável (ONG, protetor ou lar temporário): cria conta, cadastra animais e decide sobre os pedidos de seus próprios animais.

No MVP, a conta de responsável é autodeclarada e não significa ONG verificada. A validação acadêmica deve usar dados fictícios e participantes convidados.

## Requisitos e critérios de aceite
| ID | Funcionalidade | Critério de aceite |
| --- | --- | --- |
| RF01 | Cadastro e acesso | E-mail único, senha protegida por hash, sessão e saída; perfis separados. |
| RF02 | Catálogo | Filtrar animais disponíveis por espécie, porte e cidade/nome; mostrar estado sem resultados. |
| RF03 | Perfil do animal | Nome, espécie, porte, idade em meses, cidade, descrição, cuidados, vacinação, castração e fotografia opcional. |
| RF04 | Gestão dos animais | Responsável cadastra e edita somente seus animais. |
| RF05 | Solicitação responsável | Adotante informa moradia, experiência, rotina e compromisso com os cuidados; não pode duplicar pedido ativo. |
| RF06 | Acompanhamento | Adotante vê seus pedidos e pode cancelar os pendentes. |
| RF07 | Análise | Responsável acessa questionários de seus animais, aprova ou recusa com justificativa opcional. |
| RF08 | Conclusão | Um pedido aprovado coloca o animal em processo; somente o responsável confirma a adoção. Recusar/cancelar o aprovado libera o animal. Concluir encerra outros pedidos pendentes. |
| RF09 | Orientação | Conteúdo com preparação do lar, custos, adaptação e compromisso de longo prazo. |

## Regras de negócio
1. Um animal admite vários pedidos pendentes, mas apenas um aprovado.
2. Animais em processo ou adotados não aceitam novas solicitações.
3. Decisões concorrentes são serializadas no banco para evitar duas aprovações.
4. Contatos e questionários ficam restritos ao adotante e ao responsável pelo animal.
5. A aprovação é uma etapa de avaliação. A conclusão representa a entrega/adoção confirmada pelo responsável após avaliação fora da plataforma.
6. O sistema não verifica automaticamente pessoas, moradias ou condições de bem-estar.

## Estados
Animal: DISPONIVEL → EM_PROCESSO → ADOTADO. Ao encerrar uma aprovação sem adoção, retorna a DISPONIVEL.

Solicitação: PENDENTE → APROVADA → CONCLUIDA. Pedidos pendentes podem ser RECUSADOS pelo responsável ou CANCELADOS pelo adotante. Uma aprovação pode ser RECUSADA pelo responsável para reabrir o processo.

## Fora do MVP
Chat, pagamentos/doações, IA e recomendações automáticas, geolocalização, rede social, aplicativo mobile, notificações por e-mail, recuperação de senha, verificação documental de ONGs, assinatura digital e acompanhamento pós-adoção.

## Arquitetura
Angular no frontend; API REST Java/Spring Boot organizada por responsabilidades; PostgreSQL para uso normal. Um monólito modular evita infraestrutura de microsserviços que não é necessária para validar o fluxo. Perfil local com H2 permite demonstração sem instalar PostgreSQL; não substitui a validação no PostgreSQL.

## Validação acadêmica proposta
Executar cenários de cadastro, busca, pedido, recusa, aprovação e conclusão. Convidar usuários representativos a realizar tarefas e registrar conclusão, dificuldades e sugestões. Não apresentar resultados ou impactos sociais como comprovados antes de coletar evidências. Atualizar a metodologia do TCC para descrever a arquitetura realmente implementada.
