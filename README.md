# API de cursos

## Criar um curso

`POST /course` com `Content-Type: application/json`:

```json
{"name": "Java", "category": "Programação"}
```

A categoria é criada automaticamente quando não existir. O nome do curso aceita
até 150 caracteres e a categoria até 100; ambos são obrigatórios.
O cadastro recebe somente o nome do curso e o nome da categoria.

Respostas: `201` para criação, `400` para dados inválidos e `409` para um curso
com o mesmo nome na mesma categoria. Nomes são comparados exatamente como enviados.

## Banco existente

A aplicação usa PostgreSQL e `ddl-auto=update`. Antes de atualizar uma instalação
com dados, identifique duplicatas:

```sql
SELECT name, category_id, COUNT(*)
FROM courses
GROUP BY name, category_id
HAVING COUNT(*) > 1;
```

Resolva as duplicatas preservando os registros necessários e aplique a restrição
explicitamente no banco existente (uma única vez):

```sql
ALTER TABLE courses ADD CONSTRAINT uk_course_name_category UNIQUE (name, category_id);
```

Em um banco novo, o Hibernate cria a restrição pelo mapeamento da entidade.

## Compatibilidade com o esquema antigo

`ddl-auto=update` não remove colunas ou restrições antigas. Caso o banco tenha
`courses.category` ou `nome` obrigatórios, execute
uma vez o script `database/manual/align_legacy_courses.sql` antes de usar a API:

```sh
docker exec -i api_cursos_postgres sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < database/manual/align_legacy_courses.sql
```

O script é transacional e pode ser executado novamente. Ele torna essas colunas
legadas opcionais e remove somente a chave estrangeira de `category_id` para a
tabela antiga `category`. A relação atual com `categories` é preservada, assim
como todos os dados, colunas e tabelas antigos. Ele não migra os dados das colunas
legadas para as atuais e não é executado automaticamente pela aplicação.

## Remover colunas antigas

Após a compatibilização, execute `database/manual/drop_legacy_course_columns.sql`
para remover de `courses` as colunas `category` e `nome`:

```sh
docker exec -i api_cursos_postgres sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < database/manual/drop_legacy_course_columns.sql
```

Essa remoção é definitiva para os valores dessas duas colunas. Os campos atuais
`name` e `category_id`, os cursos e a tabela `categories` são preservados.

## Consultar um curso

`GET /course/{id}` retorna `200` com os dados do curso. Se o UUID não existir,
o serviço lança `CourseNotFoundException`, tratada pelo `GlobalExceptionHandler`
com HTTP `404` e corpo `{"error":"Curso não encontrado: <id>"}`.

No cadastro, `CourseAlreadyExistsException` é lançada tanto na verificação prévia
quanto no conflito da restrição única do banco, com HTTP `409`. Dados inválidos
são tratados pelo mesmo handler com HTTP `400` e mensagens por campo.

## Atualizar um curso

`PUT /course/up/{id}`, usando o UUID do curso e `Content-Type: application/json`:

```json
{"name": "Spring Boot", "categoryId": "UUID de uma categoria existente", "active": false}
```

Envie apenas os campos que deseja alterar. Campos omitidos ou `null` preservam
o valor atual. O nome deve ter de 1 a 150 caracteres e não pode conter apenas
espaços. `categoryId` deve ser um UUID válido de uma categoria já cadastrada.

Respostas: `200` com o curso atualizado, `400` para dados inválidos, `404` quando
o curso ou a categoria não existir e `409` para nome e categoria já utilizados
por outro curso. Reenviar os valores atuais do próprio curso é permitido.

## Excluir um curso

`DELETE /course/{id}`, usando o UUID do curso, sem corpo na requisição.

Respostas: `204` sem corpo após a exclusão, `400` para UUID inválido e `404`
quando o curso não existir, com `{"error":"Curso não encontrado: <id>"}`.
A exclusão remove o curso do banco e preserva sua categoria.
# Desafio2_Java_RockSeat
